package no.fintlabs.operator.pg.operator;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PGSchemaAndUserSpec {

    private String databaseName;
    private String schemaName;
    private boolean deleteOnCleanup;

}