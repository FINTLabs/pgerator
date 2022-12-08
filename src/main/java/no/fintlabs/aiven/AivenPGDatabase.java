
package no.fintlabs.aiven;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AivenPGDatabase {

    @JsonProperty("database_name")
    private String databaseName;
    @JsonProperty("lc_collate")
    private String lcCollate;
    @JsonProperty("lc_ctype")
    private String lcCtype;
    private String owner;
    @JsonProperty("quoted_owner")
    private String quotedOwner;


}
