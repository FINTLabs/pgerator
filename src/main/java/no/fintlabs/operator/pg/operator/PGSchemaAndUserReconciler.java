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

        // TODO: get username and pw
        String username = resource.getMetadata().getName() + "_" + uniqId();
        String pw = RandomStringUtils.randomAlphanumeric(32);
        String privilege = "all";

        secretService.createSecretIfNeeded(context, resource, username, pw);

//        dataAccessService.createDb(resource.getSpec().getDatabaseName());
//        dataAccessService.createSchema(resource.getSpec().getDatabaseName(), resource.getSpec().getSchemaName());
//        dataAccessService.createDbUser(resource.getSpec().getDatabaseName(), username, pw);
//        dataAccessService.grantPrivilegeToUser(resource.getSpec().getDatabaseName(), resource.getSpec().getSchemaName(), username, privilege);


        return UpdateControl.updateStatus(resource);
    }
    private String uniqId() {
        return RandomStringUtils.randomAlphanumeric(6);
    }

    @Override
    public DeleteControl cleanup(PGSchemaAndUserResource resource, Context<PGSchemaAndUserResource> context) {
        return null;
    }

    @Override
    public ErrorStatusUpdateControl<PGSchemaAndUserResource> updateErrorStatus(PGSchemaAndUserResource resource, Context<PGSchemaAndUserResource> context, Exception e) {
        return null;
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

