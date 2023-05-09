package no.fintlabs.operator;


import io.javaoperatorsdk.operator.api.reconciler.Context;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisExternalDependentResource;
import no.fintlabs.aiven.AivenService;
import no.fintlabs.aiven.FailedToCreateAivenObjectException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

import static no.fintlabs.operator.CrdUtilities.getValueFromAnnotationByKey;
import static no.fintlabs.operator.NameFactory.createDatabaseAndUserName;

@Slf4j
@Component
public class PGDatabaseAndUserDependentResource extends FlaisExternalDependentResource<PGDatabaseAndUser, PGDatabaseAndUserCRD, PGDatabaseAndUserSpec> {

    private static final String ANNOTATION_PG_DATABASE_NAME = "fintlabs.no/database-and-username";
    private final AivenService aivenService;

    public PGDatabaseAndUserDependentResource(PGDatabaseAndUserWorkflow workflow, AivenService aivenService) {
        super(PGDatabaseAndUser.class, workflow);
        this.aivenService = aivenService;
        setPollingPeriod(Duration.ofMinutes(10).toMillis());
    }

    @Override
    protected PGDatabaseAndUser desired(PGDatabaseAndUserCRD primary, Context<PGDatabaseAndUserCRD> context) {
        log.debug("Desired PGSchemaAndUser for '{}'", primary.getMetadata().getName());

        return context.getSecondaryResource(PGDatabaseAndUser.class)
                .map(o ->
                        PGDatabaseAndUser.builder()
                                .database(o.getDatabase())
                                .username(o.getUsername())
                                .build()
                )
                .orElseGet(() -> {
                            String databaseName = createDatabaseAndUserName(primary);
                            return PGDatabaseAndUser.builder()
                                    .database(databaseName)
                                    .username(databaseName)
                                    .build();
                        }
                );


    }

    @Override
    public void delete(PGDatabaseAndUserCRD primary, Context<PGDatabaseAndUserCRD> context) {
        context.getSecondaryResource(PGDatabaseAndUser.class)
                .ifPresent(pgDatabaseAndUser -> {
                    aivenService.deleteConnectionPool(pgDatabaseAndUser);
                    aivenService.deleteUserForService(pgDatabaseAndUser.getUsername());
                });
    }

    @Override
    public PGDatabaseAndUser create(PGDatabaseAndUser desired, PGDatabaseAndUserCRD primary, Context<PGDatabaseAndUserCRD> context) {

        return context.getSecondaryResource(PGDatabaseAndUser.class)
                .orElseGet(() -> {
                    try {
                        return createIfNoDatabaseAttached(desired, primary);
                    } catch (FailedToCreateAivenObjectException e) {
                        cleanupDatabaseResources(desired);
                        throw new RuntimeException(e);
                    }
                });
    }

    private void cleanupDatabaseResources(PGDatabaseAndUser desired) {
        log.error("An error occured while creating the database {}. Trying to clean up resources...", desired.getDatabase());
        log.info("Removing connection pool...");
        aivenService.deleteConnectionPool(desired);
        log.info("Removing db user...");
        aivenService.deleteUserForService(desired.getUsername());
        log.info("Removing database...");
        aivenService.deleteDatabase(desired.getDatabase());
    }

    private PGDatabaseAndUser createIfNoDatabaseAttached(PGDatabaseAndUser desired, PGDatabaseAndUserCRD primary) throws FailedToCreateAivenObjectException {
        if (StringUtils.isEmpty(primary.getMetadata().getAnnotations().get(ANNOTATION_PG_DATABASE_NAME))) {
            log.info("No database attached to CRD. Creating database in Aiven");
            desired.setPassword(RandomStringUtils.randomAlphanumeric(32));
            aivenService.createDatabaseForService(desired);
            aivenService.createUserForService(desired);
            aivenService.createConnectionPool(desired);

            primary.getMetadata().getAnnotations().put(ANNOTATION_PG_DATABASE_NAME, desired.getDatabase());

        } else {
            log.info("Database {} is attached to CRD. Skipping creating", primary.getMetadata().getAnnotations().get(ANNOTATION_PG_DATABASE_NAME));
        }
        return desired;
    }

    @Override
    public Set<PGDatabaseAndUser> fetchResources(PGDatabaseAndUserCRD primaryResource) {

        return aivenService.getUserAndDatabase(getValueFromAnnotationByKey(primaryResource, ANNOTATION_PG_DATABASE_NAME));

    }
}

