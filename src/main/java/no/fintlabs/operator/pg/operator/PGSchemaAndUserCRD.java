package no.fintlabs.operator.pg.operator;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("fintlabs.no")
@Version("v1alpha1")
@Kind("PGSchemaAndUser")
public class PGSchemaAndUserCRD extends CustomResource<PGSchemaAndUserSpec, PGSchemaAndUserStatus> implements Namespaced {
    @Override
    protected PGSchemaAndUserStatus initStatus() {
        return new PGSchemaAndUserStatus();
    }
}

