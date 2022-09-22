package no.fintlabs.operator.pg.service;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.operator.pg.operator.PGSchemaAndUserResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SecretService {

    @Value("${spring.application.name}")
    private String applicationName;

    private final KubernetesClient kubernetesClient;

    public SecretService(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    public void createSecretIfNeeded(Context<PGSchemaAndUserResource> context, PGSchemaAndUserResource resource, String username, String password) {
        if (context.getSecondaryResource(Secret.class).isEmpty()) {
            log.debug("Secret for {} is missing. Creating secret.", resource.getMetadata().getName());
            Map<String, String> stringData = new HashMap<>();

            // TODO: Fix StringData keys
            stringData.put("fint.db.username", username);
            stringData.put("fint.db.password", password);

            Secret secret = new SecretBuilder()
                    .withNewMetadata()
                    .withAnnotations(Collections.singletonMap("app.kubernetes.io/managed-by", applicationName))
                    .withName(resource.getMetadata().getName())
                    .endMetadata()
                    .withStringData(stringData)
                    .build();

            secret.addOwnerReference(resource);

            kubernetesClient.secrets().inNamespace(resource.getMetadata().getNamespace()).createOrReplace(secret);
        }
    }

    public void deleteSecretIfExists(Context<PGSchemaAndUserResource> context) {
        context
                .getSecondaryResource(Secret.class)
                .ifPresent(secret -> kubernetesClient
                        .secrets()
                        .inNamespace(secret.getMetadata().getNamespace())
                        .withName(secret.getMetadata().getName())
                        .delete());
    }
}
