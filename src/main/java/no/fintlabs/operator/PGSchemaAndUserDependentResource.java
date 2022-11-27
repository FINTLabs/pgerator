package no.fintlabs.operator;


import io.javaoperatorsdk.operator.api.reconciler.Context;
import no.fintlabs.FlaisExternalDependentResource;
import no.fintlabs.postgresql.PgService;
import no.fintlabs.postgresql.SchemaNameFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Component
public class PGSchemaAndUserDependentResource extends FlaisExternalDependentResource<PGSchemaAndUser, PGSchemaAndUserCRD, PGSchemaAndUserSpec> {
    private final PgService pgService;

    public PGSchemaAndUserDependentResource(PGSchemaAndUserWorkflow workflow, PgService pgService) {
        super(PGSchemaAndUser.class, workflow);
        this.pgService = pgService;
        setPollingPeriod(Duration.ofMinutes(10).toMillis());
    }

    @Override
    protected PGSchemaAndUser desired(PGSchemaAndUserCRD primary, Context<PGSchemaAndUserCRD> context) {
        return PGSchemaAndUser.builder()
                .database(primary.getSpec().getDatabaseName())
                .schemaName(SchemaNameFactory.schemaNameFromMetadata(primary.getMetadata()))
                .username(SchemaNameFactory.schemaNameFromMetadata(primary.getMetadata()))
                .build();
    }

    @Override
    public void delete(PGSchemaAndUserCRD primary, Context<PGSchemaAndUserCRD> context) {
        context.getSecondaryResource(PGSchemaAndUser.class)
                .ifPresent(pgSchemaAndUser -> {
                    pgService.deleteUser(pgSchemaAndUser);
                    pgService.makeSchemaOrphan(pgSchemaAndUser);
                });
    }

    @Override
    public PGSchemaAndUser create(PGSchemaAndUser desired, PGSchemaAndUserCRD primary, Context<PGSchemaAndUserCRD> context) {

        desired.setPassword(RandomStringUtils.randomAlphanumeric(32));

        pgService.ensureDatabase(desired.getDatabase());
        pgService.ensureSchema(desired);
        pgService.ensureUser(desired);

        return desired;
    }

    @Override
    public Set<PGSchemaAndUser> fetchResources(PGSchemaAndUserCRD primaryResource) {
        String dbName = primaryResource.getSpec().getDatabaseName();
        String schemaName = SchemaNameFactory.schemaNameFromMetadata(primaryResource.getMetadata());
        String username = SchemaNameFactory.schemaNameFromMetadata(primaryResource.getMetadata());

        return pgService.getSchemaAndUser(dbName, schemaName, username);
    }
}

