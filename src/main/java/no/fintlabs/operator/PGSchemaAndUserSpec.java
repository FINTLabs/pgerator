package no.fintlabs.operator;

import lombok.*;
import no.fintlabs.FlaisSpec;

@Data
public class PGSchemaAndUserSpec implements FlaisSpec {
    private String databaseName;
    private String schemaName;
    private boolean deleteOnCleanup;

}