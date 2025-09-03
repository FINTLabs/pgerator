package no.fintlabs.operator;

public class NameFactory {

    public static int MAX_USERNAME_LENGTH = 63;

    public static String createUsername(PGUserCRD crd) {
        String metadataLabels = crd.getMetadata().getLabels().get("fintlabs.no/org-id")
                .replaceAll("\\.", "_")
                .replaceAll("-", "_");

        String metadataName = crd.getMetadata().getName()
                .replaceAll("-", "_");

        String name = String.format("%s_%s", metadataLabels, metadataName).toLowerCase();

        if (name.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException("Username too long. It can be max " + MAX_USERNAME_LENGTH + " characters.");
        }

        return name.toLowerCase();
    }
}
