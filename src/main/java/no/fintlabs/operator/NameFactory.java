package no.fintlabs.operator;

public class NameFactory {

    public static int MAX_USERNAME_LENGTH = 63;

    public static String createUsername(PGUserCRD crd) {
        String name = String.format(
                "%s_%s",
                crd.getMetadata().getLabels().get("fintlabs.no/org-id").replaceAll("\\.", "_"),
                crd.getMetadata().getName().replaceAll("-", "_")
        );

        if (name.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException("Username too long. It can be max " + MAX_USERNAME_LENGTH + " characters.");
        }

        return name.toLowerCase();
    }


}
