package no.fintlabs.operator;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisKubernetesDependentResource;
import no.fintlabs.FlaisWorkflow;
import no.fintlabs.OperatorProperties;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;

@Component
@Slf4j
@KubernetesDependent(
        labelSelector = "app.kubernetes.io/managed-by=pgerator"
)
public class PGSchemaAndUserSecretDependentResource extends FlaisKubernetesDependentResource<Secret, PGSchemaAndUserCRD, PGSchemaAndUserSpec> {

    private final OperatorProperties properties;

    public PGSchemaAndUserSecretDependentResource(FlaisWorkflow<PGSchemaAndUserCRD, PGSchemaAndUserSpec> workflow, KubernetesClient kubernetesClient, PGSchemaAndUserDependentResource dependentResource, OperatorProperties properties) {
        super(Secret.class, workflow, kubernetesClient);
        this.properties = properties;
        dependsOn(dependentResource);
    }

    @Override
    protected Secret desired(PGSchemaAndUserCRD primary, Context<PGSchemaAndUserCRD> context) {
        log.debug("Desired secret for {}", primary.getMetadata().getName());

        return generateSecret(primary, context);
    }

    private Secret generateSecret(PGSchemaAndUserCRD primary, Context<PGSchemaAndUserCRD> context) {
        PGSchemaAndUser pgSchemaAndUser = context.getSecondaryResource(PGSchemaAndUser.class).orElseThrow();
        HashMap<String, String> labels = new HashMap<>(primary.getMetadata().getLabels());
        labels.put("app.kubernetes.io/managed-by", "pgerator");

        return new SecretBuilder()
                .withNewMetadata()
                .withName(primary.getMetadata().getName()).withNamespace(primary.getMetadata().getNamespace())
                .withLabels(labels)
                .endMetadata()
                .withType("Opaque")
                .addToData("fint.database.schema", encode(pgSchemaAndUser.getSchemaName()))
                .addToData("fint.database.username", encode(pgSchemaAndUser.getUsername()))
                .addToData("fint.database.password", encode(pgSchemaAndUser.getPassword()))
                .addToData("fint.database.url", encode(properties.getPoolBaseUrl()
                        + pgSchemaAndUser.getSchemaName().toLowerCase()
                        + "?sslmode=require"))
                .build();
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    @SuppressWarnings("unused")
    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value.getBytes()));
    }
}
