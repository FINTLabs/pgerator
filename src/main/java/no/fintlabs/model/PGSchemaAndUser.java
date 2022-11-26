package no.fintlabs.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PGSchemaAndUser {
    private String database;
    private String schemaName;
    private String username;
    private String password;
}
