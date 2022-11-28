
package no.fintlabs.aiven;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AccessControl {
    @JsonProperty("pg_allow_replication")
    private Boolean pgAllowReplication;
}
