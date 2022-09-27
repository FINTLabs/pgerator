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

            stringData.put(resource.getMetadata().getName() + ".db.username", username);
            stringData.put(resource.getMetadata().getName() + ".db.password", password);

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


    public String getSecretIfExists(Context<PGSchemaAndUserResource> context, PGSchemaAndUserResource resource, String key) {
        if (context.getSecondaryResource(Secret.class).isPresent()) {
            log.debug("Secret exists for resource {}", resource.getMetadata().getName());
            String encodedUsername = context.getSecondaryResource(Secret.class).get().getData().get(key);
            return new String(java.util.Base64.getDecoder().decode(encodedUsername));
        }
        return null;
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
