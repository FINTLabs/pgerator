package no.fintlabs.operator.pg.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PGSchemaAndUser {
    private String schemaName;
    private String username;
    private String password;
}
