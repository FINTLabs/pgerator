package no.fintlabs.operator.pg.operator;

import io.fabric8.kubernetes.api.model.Secret;
import io.javaoperatorsdk.operator.api.config.informer.InformerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;
import io.javaoperatorsdk.operator.processing.event.source.informer.Mappers;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.operator.pg.service.PostgreSqlDataAccessService;
import no.fintlabs.operator.pg.service.SecretService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@ControllerConfiguration
public class PGSchemaAndUserReconciler implements Reconciler<PGSchemaAndUserResource>,
        EventSourceInitializer<PGSchemaAndUserResource>,
        ErrorStatusHandler<PGSchemaAndUserResource>,
        Cleaner<PGSchemaAndUserResource> {


    private final PostgreSqlDataAccessService dataAccessService;

    private final SecretService secretService;

    public PGSchemaAndUserReconciler(PostgreSqlDataAccessService dataAccessService, SecretService secretService) {
        this.dataAccessService = dataAccessService;
        this.secretService = secretService;
    }

    @Override
    public UpdateControl<PGSchemaAndUserResource> reconcile(PGSchemaAndUserResource resource, Context<PGSchemaAndUserResource> context) {
        log.debug("Reconciling {}", resource.getMetadata().getName());

        if (context.getSecondaryResource(Secret.class).isPresent()) {
            log.debug("Secret exists for resource {}", resource.getMetadata().getName());
            return UpdateControl.noUpdate();
        }

        String username = resource.getMetadata().getName() + "_" + uniqId();
        String password = RandomStringUtils.randomAlphanumeric(32);
        String privilege = "all";

        secretService.createSecretIfNeeded(context, resource, username, password);

        if (!dataAccessService.databaseExists(resource.getSpec().getDatabaseName())) {
            dataAccessService.createDb(resource.getSpec().getDatabaseName());
        }
        if (!dataAccessService.schemaExists(resource.getSpec().getDatabaseName(), resource.getSpec().getSchemaName())) {
            dataAccessService.createSchema(resource.getSpec().getDatabaseName(), resource.getSpec().getSchemaName());
        }
        if (!dataAccessService.userExists(resource.getSpec().getDatabaseName(), username)) {
            dataAccessService.createDbUser(resource.getSpec().getDatabaseName(), username, password);
        }

        dataAccessService.grantPrivilegeToUser(resource.getSpec().getDatabaseName(), resource.getSpec().getSchemaName(), username, privilege);

        return UpdateControl.updateResourceAndStatus(resource);
    }

    private String uniqId() {
        return RandomStringUtils.randomAlphanumeric(6);
    }

    @Override
    public DeleteControl cleanup(PGSchemaAndUserResource resource, Context<PGSchemaAndUserResource> context) {
        if (resource.getSpec().isDeleteOnCleanup()) {
            String databaseName = resource.getSpec().getDatabaseName();
            String schemaName = resource.getSpec().getSchemaName();
            if (dataAccessService.schemaExists(databaseName, schemaName)) {
                dataAccessService.deleteSchema(databaseName, schemaName);
            }
            String username = secretService.getSecretIfExists(context, resource, resource.getMetadata().getName() + ".db.username");
            if (dataAccessService.userExists(databaseName, username)) {
                dataAccessService.deleteUser(databaseName, username);
            }
            if (dataAccessService.schemaExists(databaseName, schemaName) ||
                    dataAccessService.userExists(databaseName, username)) {
                log.error("Failed to delete schema or user");
            }
        }
        secretService.deleteSecretIfExists(context);
        return DeleteControl.defaultDelete();
    }

    @Override
    public ErrorStatusUpdateControl<PGSchemaAndUserResource> updateErrorStatus(PGSchemaAndUserResource resource, Context<PGSchemaAndUserResource> context, Exception e) {
        PGSchemaAndUserStatus resourceStatus = resource.getStatus() ;
        resourceStatus.setErrorMessage(e.getMessage());
        resource.setStatus(resourceStatus);
        return ErrorStatusUpdateControl.updateStatus(resource);
    }

    @Override
    public Map<String, EventSource> prepareEventSources(EventSourceContext<PGSchemaAndUserResource> context) {
        return EventSourceInitializer
                .nameEventSources(
                        new InformerEventSource<>(
                                InformerConfiguration.from(Secret.class, context)
                                        .withSecondaryToPrimaryMapper(Mappers.fromOwnerReference())
                                        .build(),
                                context)
                );
    }
}

