package no.fintlabs.operator.pg.operator;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisKubernetesDependentResource;
import no.fintlabs.FlaisWorkflow;
import no.fintlabs.operator.pg.model.PGSchemaAndUser;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
@Slf4j
public class PGSchemaAndUserSecretDependentResource extends FlaisKubernetesDependentResource<Secret, PGSchemaAndUserCRD, PGSchemaAndUserSpec> {

    public PGSchemaAndUserSecretDependentResource(FlaisWorkflow<PGSchemaAndUserCRD, PGSchemaAndUserSpec> workflow, KubernetesClient kubernetesClient, PGSchemaAndUserDependentResource dependentResource) {
        super(Secret.class, workflow, kubernetesClient);
        dependsOn(dependentResource);
    }

    @Override
    protected Secret desired(PGSchemaAndUserCRD primary, Context<PGSchemaAndUserCRD> context) {
        log.debug("Desired secret for {}", primary.getMetadata().getName());

        Optional<PGSchemaAndUser> schemaAndUser = context.getSecondaryResource(PGSchemaAndUser.class);
        PGSchemaAndUser pgSchemaAndUser = schemaAndUser.orElseThrow();

        HashMap<String, String> labels = new HashMap<>(primary.getMetadata().getLabels());

        labels.put("app.kubernetes.io/managed-by", "flaiserator");
        return new SecretBuilder()
                .withNewMetadata()
                    .withName(primary.getMetadata().getName()).withNamespace(primary.getMetadata().getNamespace())
                    .withLabels(labels)
                    .endMetadata()
                .withStringData(new HashMap<>() {{
                    put(primary.getMetadata().getName() + ".pg.schema", pgSchemaAndUser.getSchemaName());
                    put(primary.getMetadata().getName() + ".pg.username", pgSchemaAndUser.getUsername());
                    put(primary.getMetadata().getName() + ".pg.password", pgSchemaAndUser.getPassword());
        }}).build();

    }
}
