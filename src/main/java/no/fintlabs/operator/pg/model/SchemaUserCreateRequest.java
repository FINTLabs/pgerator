package no.fintlabs.operator.pg.model;

import lombok.Data;

@Data
public class SchemaUserCreateRequest { ;
    private String password;
    private String privileges;
}
