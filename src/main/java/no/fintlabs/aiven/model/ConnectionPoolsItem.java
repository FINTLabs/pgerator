package no.fintlabs.aiven.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ConnectionPoolsItem {
    @JsonProperty("connection_uri")
    private String connectionUri;
    private String database;
    @JsonProperty("pool_mode")
    private String poolMode;
    @JsonProperty("pool_size")
    private int poolSize;
    @JsonProperty("poolName")
    private String poolName;
    private String username;
}