
package no.fintlabs.aiven;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AivenPGServiceUser {

    @JsonProperty("access_control")
    private AccessControl accessControl;
    @JsonProperty("password")
    private String password;
    @JsonProperty("type")
    private String type;
    @JsonProperty("username")
    private String username;

}
