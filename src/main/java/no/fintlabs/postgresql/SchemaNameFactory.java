package no.fintlabs.postgresql;

import io.fabric8.kubernetes.api.model.ObjectMeta;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SchemaNameFactory {

    public static String schemaNameFromMetadata(ObjectMeta metadata) {

        return String.format(
                "%s_%s_%s",
                metadata.getLabels().get("fintlabs.no/org-id").replaceAll("\\.", "-"),
                metadata.getLabels().get("fintlabs.no/team"),
                metadata.getName()
        );
    }

    public static String orphanSchemaNameFromName(String name) {

        return String.format(
                "%s_orphan_%s",
                name,
                new SimpleDateFormat("yyyy-MM-dd").format(new Date())
        );
    }
}
