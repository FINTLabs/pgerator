package no.fintlabs.operator;

import org.apache.commons.lang3.RandomStringUtils;

public class NameFactory {

    public static String createUsername(PGUserCRD crd) {
        String name = String.format(
                "%s_%s_%s",
                crd.getMetadata().getLabels().get("fintlabs.no/org-id").replaceAll("\\.", "-"),
                crd.getMetadata().getLabels().get("fintlabs.no/team"),
                RandomStringUtils.randomAlphabetic(7)
        );

        if (name.length() > 40) {
            throw new IllegalArgumentException("Database name to long. It can be max 40 characters.");
        }

        return name;
    }


}
