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
}

