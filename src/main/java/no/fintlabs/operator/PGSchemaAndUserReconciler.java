package no.fintlabs.operator;

import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.api.reconciler.dependent.EventSourceProvider;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisReconiler;
import no.fintlabs.FlaisWorkflow;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ControllerConfiguration
public class PGSchemaAndUserReconciler extends FlaisReconiler<PGSchemaAndUserCRD, PGSchemaAndUserSpec> {
    public PGSchemaAndUserReconciler(FlaisWorkflow<PGSchemaAndUserCRD, PGSchemaAndUserSpec> workflow, List<? extends EventSourceProvider<PGSchemaAndUserCRD>> eventSourceProviders, List<? extends Deleter<PGSchemaAndUserCRD>> deleters) {
        super(workflow, eventSourceProviders, deleters);
    }


//    private final PostgreSqlDataAccessService dataAccessService;
//
//    private final SecretService secretService;
//
//    public PGSchemaAndUserReconciler(PostgreSqlDataAccessService dataAccessService, SecretService secretService) {
//        this.dataAccessService = dataAccessService;
//        this.secretService = secretService;
//    }
//
//    @Override
//    public UpdateControl<PGSchemaAndUserCRD> reconcile(PGSchemaAndUserCRD resource, Context<PGSchemaAndUserCRD> context) {
//        log.debug("Reconciling {}", resource.getMetadata().getName());
//        CrdValidator.validate(resource);
//
//        if (context.getSecondaryResource(Secret.class).isPresent()) {
//            log.debug("Secret exists for resource {}", resource.getMetadata().getName());
//            return UpdateControl.noUpdate();
//        }
//
//        String username = resource.getMetadata().getName() + "_" + uniqId();
//        String password = RandomStringUtils.randomAlphanumeric(32);
//        String privilege = "all";
//
//        secretService.createSecretIfNeeded(context, resource, username, password);
//
//        if (!dataAccessService.databaseExists(resource.getSpec().getDatabaseName())) {
//            dataAccessService.createDb(resource.getSpec().getDatabaseName());
//        }
//        if (!dataAccessService.schemaExists(resource.getSpec().getDatabaseName(), resource.getSpec().getSchemaName())) {
//            dataAccessService.createSchema(resource.getSpec().getDatabaseName(), resource.getSpec().getSchemaName());
//        }
//        if (!dataAccessService.userExists(resource.getSpec().getDatabaseName(), username)) {
//            dataAccessService.createDbUser(resource.getSpec().getDatabaseName(), username, password);
//        }
//
//        dataAccessService.grantPrivilegeToUser(resource.getSpec().getDatabaseName(), resource.getSpec().getSchemaName(), username, privilege);
//
//        return UpdateControl.updateResourceAndStatus(resource);
//    }
//
//    private String uniqId() {
//        return RandomStringUtils.randomAlphanumeric(6);
//    }
//
//    @Override
//    public DeleteControl cleanup(PGSchemaAndUserCRD resource, Context<PGSchemaAndUserCRD> context) {
//        if (resource.getSpec().isDeleteOnCleanup()) {
//            String databaseName = resource.getSpec().getDatabaseName();
//            String schemaName = resource.getSpec().getSchemaName();
//            if (dataAccessService.schemaExists(databaseName, schemaName)) {
//                dataAccessService.deleteSchema(databaseName, schemaName);
//            }
//            String username = secretService.getSecretIfExists(context, resource, resource.getMetadata().getName() + ".db.username");
//            if (dataAccessService.userExists(databaseName, username)) {
//                dataAccessService.deleteUser(databaseName, username);
//            }
//            if (dataAccessService.schemaExists(databaseName, schemaName) ||
//                    dataAccessService.userExists(databaseName, username)) {
//                log.error("Failed to delete schema or user");
//            }
//        }
//        secretService.deleteSecretIfExists(context);
//        log.info("Cleanup done for {}", resource.getMetadata().getName());
//        return DeleteControl.defaultDelete();
//    }
//
//    @Override
//    public ErrorStatusUpdateControl<PGSchemaAndUserCRD> updateErrorStatus(PGSchemaAndUserCRD resource, Context<PGSchemaAndUserCRD> context, Exception e) {
//        PGSchemaAndUserStatus resourceStatus = resource.getStatus() ;
//        resourceStatus.setErrorMessage(e.getMessage());
//        resource.setStatus(resourceStatus);
//        return ErrorStatusUpdateControl.updateStatus(resource);
//    }
//
//    @Override
//    public Map<String, EventSource> prepareEventSources(EventSourceContext<PGSchemaAndUserCRD> context) {
//        return EventSourceInitializer
//                .nameEventSources(
//                        new InformerEventSource<>(
//                                InformerConfiguration.from(Secret.class, context)
//                                        .withSecondaryToPrimaryMapper(Mappers.fromOwnerReference())
//                                        .build(),
//                                context)
//                );
//    }
}
