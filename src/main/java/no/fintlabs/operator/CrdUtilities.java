package no.fintlabs.operator;

import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.Optional;

public class CrdUtilities {

    public static Optional<String> getValueFromAnnotationByKey(HasMetadata crd, String key) {
        return Optional.ofNullable(crd.getMetadata().getAnnotations().get(key));
    }
}
