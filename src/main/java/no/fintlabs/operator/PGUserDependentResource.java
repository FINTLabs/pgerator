package no.fintlabs.operator;


import io.javaoperatorsdk.operator.api.reconciler.Context;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisExternalDependentResource;
import no.fintlabs.aiven.AivenService;
import no.fintlabs.aiven.FailedToCreateAivenObjectException;
import no.fintlabs.pg.PgService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

import static no.fintlabs.operator.NameFactory.createUsername;

@Slf4j
@Component
public class PGUserDependentResource extends FlaisExternalDependentResource<PGUser, PGUserCRD, PGUserSpec> {

    public static final String ANNOTATION_PG_DATABASE_NAME = "fintlabs.no/database-username";
    private final AivenService aivenService;
    private final PgService pgService;

    public PGUserDependentResource(PGUserWorkflow workflow, AivenService aivenService, PgService pgService) {
        super(PGUser.class, workflow);
        this.aivenService = aivenService;
        this.pgService = pgService;
        setPollingPeriod(Duration.ofMinutes(10).toMillis());
    }

    @Override
    protected PGUser desired(PGUserCRD primary, Context<PGUserCRD> context) {
        log.debug("Desired PGSchemaAndUser for '{}'", primary.getMetadata().getName());

        return context.getSecondaryResource(PGUser.class)
                .map(o ->
                        PGUser.builder()
                                .database(o.getDatabase())
                                .username(o.getUsername())
                                .build()
                )
                .orElseGet(() -> {
                            String databaseName = createUsername(primary);
                            return PGUser.builder()
                                    .database(primary.getSpec().getDatabase())
                                    .username(databaseName)
                                    .build();
                        }
                );


    }

    @Override
    public void delete(PGUserCRD primary, Context<PGUserCRD> context) {
        context.getSecondaryResource(PGUser.class)
                .ifPresent(pgUser -> {
                    aivenService.deleteUserForService(pgUser.getUsername());
                    pgService.deleteSchema(pgUser.getDatabase(), pgUser.getUsername());
                });
    }

    @Override
    public PGUser create(PGUser desired, PGUserCRD primary, Context<PGUserCRD> context) {

        return context.getSecondaryResource(PGUser.class)
                .orElseGet(() -> {
                    try {
                        PGUser user = createUser(desired, primary);
                        pgService.createSchema(user.getDatabase(), user.getUsername());
                        pgService.grantUsageAndCreateOnSchema(user.getDatabase(), user.getUsername());

                        return user;

                    } catch (FailedToCreateAivenObjectException | DataAccessException e) {
                        aivenService.deleteUserForService(desired.getUsername());
                        pgService.deleteSchema(desired.getDatabase(), desired.getUsername());
                        throw new RuntimeException(e);
                    }
                });
    }

    private PGUser createUser(PGUser desired, PGUserCRD primary) throws FailedToCreateAivenObjectException {
        if (StringUtils.isEmpty(primary.getMetadata().getAnnotations().get(ANNOTATION_PG_DATABASE_NAME))) {
            log.info("No user attached to CRD. Creating database in Aiven");
            desired.setPassword(RandomStringUtils.randomAlphanumeric(32));
            aivenService.createUserForService(desired);

            primary.getMetadata().getAnnotations().put(ANNOTATION_PG_DATABASE_NAME, desired.getUsername());

        } else {
            log.info("Database {} is attached to CRD. Skipping creating", primary.getMetadata().getAnnotations().get(ANNOTATION_PG_DATABASE_NAME));
        }
        return desired;
    }

    @Override
    public Set<PGUser> fetchResources(PGUserCRD primaryResource) {

        return aivenService.getPgUser(primaryResource);

    }
}

