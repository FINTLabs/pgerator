
package no.fintlabs.aiven;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AivenServiceUser {
    @JsonProperty("user")
    private AivenPGServiceUser aivenPGServiceUser;
}
