package no.fintlabs.operator;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

public class NameFactory {

    public static String createDatabaseName(PGDatabaseAndUserCRD crd) {
        String name = String.format(
                "%s_%s_%s",
                crd.getMetadata().getLabels().get("fintlabs.no/org-id").replaceAll("\\.", "-"),
                crd.getMetadata().getLabels().get("fintlabs.no/team"),
                crd.getMetadata().getName()
        );

        return StringUtils.substring(name, 0, 40);
    }

    public static String createDatabaseUserName(PGDatabaseAndUserCRD crd) {
        return createDatabaseName(crd);
    }
}
