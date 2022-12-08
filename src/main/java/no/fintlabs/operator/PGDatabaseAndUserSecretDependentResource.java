package no.fintlabs.operator;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.Matcher;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResourceConfig;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisKubernetesDependentResource;
import no.fintlabs.FlaisWorkflow;
import no.fintlabs.OperatorProperties;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;

@Component
@Slf4j
public class PGDatabaseAndUserSecretDependentResource extends FlaisKubernetesDependentResource<Secret, PGDatabaseAndUserCRD, PGDatabaseAndUserSpec> {

    private final OperatorProperties properties;

    public PGDatabaseAndUserSecretDependentResource(FlaisWorkflow<PGDatabaseAndUserCRD, PGDatabaseAndUserSpec> workflow, KubernetesClient kubernetesClient, PGDatabaseAndUserDependentResource dependentResource, OperatorProperties properties) {
        super(Secret.class, workflow, kubernetesClient);
        this.properties = properties;
        dependsOn(dependentResource);
        configureWith(
                new KubernetesDependentResourceConfig<Secret>()
                        .setLabelSelector("app.kubernetes.io/managed-by=pgerator")
        );
    }

    @Override
    public Matcher.Result<Secret> match(Secret actualResource, PGDatabaseAndUserCRD primary, Context<PGDatabaseAndUserCRD> context) {
        return super.match(actualResource, primary, context);
    }

    @Override
    protected Secret desired(PGDatabaseAndUserCRD primary, Context<PGDatabaseAndUserCRD> context) {
        log.debug("Desired secret for {}", primary.getMetadata().getName());

        return generateSecret(primary, context);
    }

    private Secret generateSecret(PGDatabaseAndUserCRD primary, Context<PGDatabaseAndUserCRD> context) {
        PGDatabaseAndUser pgDatabaseAndUser = context.getSecondaryResource(PGDatabaseAndUser.class).orElseThrow();
        HashMap<String, String> labels = new HashMap<>(primary.getMetadata().getLabels());
        labels.put("app.kubernetes.io/managed-by", "pgerator");

        return new SecretBuilder()
                .withNewMetadata()
                .withName(primary.getMetadata().getName()).withNamespace(primary.getMetadata().getNamespace())
                .withLabels(labels)
                .endMetadata()
                .withType("Opaque")
                .addToData("fint.database.username", encode(pgDatabaseAndUser.getUsername()))
                .addToData("fint.database.password", encode(pgDatabaseAndUser.getPassword()))
                .addToData("fint.database.url", encode(properties.getPoolBaseUrl()
                        + pgDatabaseAndUser.getDatabase().toLowerCase()
                        + "?sslmode=require&prepareThreshold=0"))
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
