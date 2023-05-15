package no.fintlabs.operator;


import io.javaoperatorsdk.operator.api.reconciler.Context;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisExternalDependentResource;
import no.fintlabs.aiven.AivenService;
import no.fintlabs.aiven.FailedToCreateAivenObjectException;
import no.fintlabs.pg.PgService;
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
                        //PGUser user = createUser(desired, primary);

                        aivenService.createUserForService(desired);
                        pgService.ensureSchema(desired.getDatabase(), desired.getUsername());
                        pgService.ensureUsageAndCreateOnSchema(desired.getDatabase(), desired.getUsername());

                        return desired;

                    } catch (FailedToCreateAivenObjectException | DataAccessException e) {
                        aivenService.deleteUserForService(desired.getUsername());
                        pgService.deleteSchema(desired.getDatabase(), desired.getUsername());
                        throw new RuntimeException(e);
                    }
                });
    }

//    private PGUser createUser(PGUser desired, PGUserCRD primary) throws FailedToCreateAivenObjectException {
//        //desired.setPassword(RandomStringUtils.randomAlphanumeric(32));
//        aivenService.createUserForService(desired);
//
//        return desired;
//    }

    @Override
    public Set<PGUser> fetchResources(PGUserCRD primaryResource) {

        return aivenService.getPgUser(primaryResource);

    }
}

