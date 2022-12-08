package no.fintlabs.operator;

public class NameFactory {

    public static String createDatabaseName(PGDatabaseAndUserCRD crd) {
        return String.format(
                "%s_%s_%s",
                crd.getMetadata().getLabels().get("fintlabs.no/org-id").replaceAll("\\.", "-"),
                crd.getMetadata().getLabels().get("fintlabs.no/team"),
                crd.getMetadata().getName()
        );
    }

    public static String createDatabaseUserName(PGDatabaseAndUserCRD crd) {
        return createDatabaseName(crd);
    }
}
