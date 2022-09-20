package no.fintlabs.operator.pg.operator;

import io.javaoperatorsdk.operator.api.ObservedGenerationAwareStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PGSchemaAndUserStatus extends ObservedGenerationAwareStatus {
    private String errorMessage;
}
