package no.fintlabs.operator;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisKubernetesDependentResource;
import no.fintlabs.FlaisWorkflow;
import no.fintlabs.OperatorProperties;
import no.fintlabs.postgresql.PostgreSqlDataAccessService;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;

@Component
@Slf4j
public class PGSchemaAndUserSecretDependentResource extends FlaisKubernetesDependentResource<Secret, PGSchemaAndUserCRD, PGSchemaAndUserSpec> {

    private final OperatorProperties properties;
    private final PostgreSqlDataAccessService postgreSqlDataAccessService;

    public PGSchemaAndUserSecretDependentResource(FlaisWorkflow<PGSchemaAndUserCRD, PGSchemaAndUserSpec> workflow, KubernetesClient kubernetesClient, PGSchemaAndUserDependentResource dependentResource, OperatorProperties properties, PostgreSqlDataAccessService postgreSqlDataAccessService) {
        super(Secret.class, workflow, kubernetesClient);
        this.properties = properties;
        this.postgreSqlDataAccessService = postgreSqlDataAccessService;
        dependsOn(dependentResource);
    }

    @Override
    protected Secret desired(PGSchemaAndUserCRD primary, Context<PGSchemaAndUserCRD> context) {
        log.debug("Desired secret for {}", primary.getMetadata().getName());

        return context.getSecondaryResource(Secret.class)
                .orElse(generateSecret(primary, context));
    }

    private Secret generateSecret(PGSchemaAndUserCRD primary, Context<PGSchemaAndUserCRD> context) {
        PGSchemaAndUser pgSchemaAndUser = context.getSecondaryResource(PGSchemaAndUser.class).orElseThrow();
        if (Objects.isNull(pgSchemaAndUser.getPassword())) {
            pgSchemaAndUser.setPassword(postgreSqlDataAccessService.resetUserPassword(pgSchemaAndUser.getUsername()));
        }
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
                .addToData("fint.database.url", encode(properties.getBaseUrl()
                        + pgSchemaAndUser.getDatabase().toLowerCase()
                        + "?sslmode=require"))
                .build();
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value.getBytes()));
    }
}
