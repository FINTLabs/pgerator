package no.fintlabs.operator.pg.operator;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CrdValidator {

    public static void validate(PGSchemaAndUserCRD crd) {
        log.info("Validating CRD");
        List<String> missingSpecs = new ArrayList<>();
        if (crd.getSpec().getDatabaseName() == null) {
            missingSpecs.add("databaseName");
        }
        if (crd.getSpec().getSchemaName() == null) {
            missingSpecs.add("schemaName");
        }
        if (!missingSpecs.isEmpty()) {
            throw new IllegalArgumentException("Missing mandatory specs: " + missingSpecs);
        }
    }
}