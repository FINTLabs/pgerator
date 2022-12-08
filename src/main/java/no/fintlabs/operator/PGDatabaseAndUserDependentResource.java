package no.fintlabs.operator;


import io.javaoperatorsdk.operator.api.reconciler.Context;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisExternalDependentResource;
import no.fintlabs.aiven.AivenService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Component
public class PGDatabaseAndUserDependentResource extends FlaisExternalDependentResource<PGDatabaseAndUser, PGDatabaseAndUserCRD, PGDatabaseAndUserSpec> {

    private final AivenService aivenService;

    public PGDatabaseAndUserDependentResource(PGDatabaseAndUserWorkflow workflow, AivenService aivenService) {
        super(PGDatabaseAndUser.class, workflow);
        this.aivenService = aivenService;
        setPollingPeriod(Duration.ofMinutes(10).toMillis());
    }

    @Override
    protected PGDatabaseAndUser desired(PGDatabaseAndUserCRD primary, Context<PGDatabaseAndUserCRD> context) {
        log.debug("Desired PGSchemaAndUser for '{}'", primary.getMetadata().getName());

        return PGDatabaseAndUser.builder()
                .database(NameFactory.createDatabaseName(primary))
                .username(NameFactory.createDatabaseUserName(primary))
                .build();
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

        desired.setPassword(RandomStringUtils.randomAlphanumeric(32));

        aivenService.createDatabaseForService(desired);
        aivenService.createUserForService(desired);
        aivenService.createConnectionPool(desired);

        return desired;
    }

    @Override
    public Set<PGDatabaseAndUser> fetchResources(PGDatabaseAndUserCRD primaryResource) {

        return aivenService.getUserAndDatabase(
                NameFactory.createDatabaseUserName(primaryResource),
                NameFactory.createDatabaseUserName(primaryResource)
        );

    }
}

