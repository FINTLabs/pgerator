package no.fintlabs.operator.pg.operator;


import io.javaoperatorsdk.operator.api.reconciler.Context;
import no.fintlabs.FlaisExternalDependentResource;
import no.fintlabs.operator.pg.model.PGSchemaAndUser;
import no.fintlabs.operator.pg.service.PostgreSqlDataAccessService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Component
public class PGSchemaAndUserDependentResource extends FlaisExternalDependentResource<PGSchemaAndUser, PGSchemaAndUserCRD, PGSchemaAndUserSpec> {
    private final PostgreSqlDataAccessService pgService;

    public PGSchemaAndUserDependentResource(PGSchemaAndUserWorkflow workflow, PostgreSqlDataAccessService pgService) {
        super(PGSchemaAndUser.class, workflow);
        this.pgService = pgService;
        setPollingPeriod(Duration.ofMinutes(10).toMillis());
    }

    @Override
    protected PGSchemaAndUser desired(PGSchemaAndUserCRD primary, Context<PGSchemaAndUserCRD> context) {
        return PGSchemaAndUser.builder()
                .schemaName(primary.getSpec().getSchemaName())
                .username(primary.getMetadata().getName())
                .password("")
                .build();
    }

    @Override
    public void delete(PGSchemaAndUserCRD primary, Context<PGSchemaAndUserCRD> context) {
        String dbName = primary.getSpec().getDatabaseName();
        String schemaName = primary.getSpec().getSchemaName();
        String username = primary.getMetadata().getName();
        pgService.deleteSchema(dbName, schemaName);
        pgService.deleteUser(dbName, username);
    }

    @Override
    public PGSchemaAndUser create(PGSchemaAndUser desired, PGSchemaAndUserCRD primary, Context<PGSchemaAndUserCRD> context) {
        String dbName = primary.getSpec().getDatabaseName();
        String schemaName = primary.getSpec().getSchemaName();
        String username = primary.getMetadata().getName();
        String password = RandomStringUtils.randomAlphanumeric(16);
        pgService.createSchema(dbName, schemaName);
        pgService.createDbUser(dbName, username, password);
        pgService.grantPrivilegeToUser(dbName, schemaName, username, "all");
        return PGSchemaAndUser.builder()
                .schemaName(schemaName)
                .username(username)
                .password(password)
                .build();
    }

    @Override
    public Set<PGSchemaAndUser> fetchResources(PGSchemaAndUserCRD primaryResource) {
        String dbName = primaryResource.getSpec().getDatabaseName();
        String schemaName = primaryResource.getSpec().getSchemaName();
        String username = primaryResource.getMetadata().getName();

        return pgService.getSchemaAndUser(dbName, schemaName, username);
    }
}

