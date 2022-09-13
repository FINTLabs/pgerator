package no.fintlabs.operator.pg.model;

import lombok.Data;

@Data
public class SchemaUserCreateRequest { ;
    private String schemaName;
    private String username;
    private String password;
    private String privileges;
}
