package no.fintlabs.aiven;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionPool {

    private String database;

    @Builder.Default
    @JsonProperty("pool_mode")
    private String poolMode = "transaction";

    @JsonProperty("pool_name")
    private String poolName;

    @Builder.Default
    @JsonProperty("pool_size")
    private long poolSize = 10;

    private String username;
}
