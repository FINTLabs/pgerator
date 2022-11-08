package no.fintlabs.operator.pg.operator;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;
import no.fintlabs.FlaisCrd;
import no.fintlabs.FlaisStatus;

@Group("fintlabs.no")
@Version("v1alpha1")
@Kind("PGSchemaAndUser")
public class PGSchemaAndUserCRD extends FlaisCrd<PGSchemaAndUserSpec> implements Namespaced {

    @Override
    protected FlaisStatus initStatus() {
        return new FlaisStatus();
    }
}

