package no.fintlabs.operator.pg.operator;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostgresApplicationSpec {

    private String databaseName;
    private String schemaName;
    private String username;
    private String password;
    private String privilege;

}
