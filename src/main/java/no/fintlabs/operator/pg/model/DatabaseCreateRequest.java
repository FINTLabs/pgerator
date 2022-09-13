package no.fintlabs.operator.pg.model;

import lombok.Data;

@Data
public class DatabaseCreateRequest {
    private String databaseName;
}
