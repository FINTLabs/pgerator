package no.fintlabs.operator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NameFactoryTest {
    PGUserCRD crd = new PGUserCRD();
    @Test
    public void nameShouldBeInTheFormatOrgIdPlusCrdName() {
        crd.getMetadata().setName("test_test");
        crd.getMetadata().getLabels().put("fintlabs.no/org-id", "flais.io");

        String name = NameFactory.createUsername(crd);

        assertEquals("flais_io_test_test", name);
    }

    @Test
    public void nameShouldContainOrgIdAndCrdName() {
        crd.getMetadata().setName("test");
        crd.getMetadata().getLabels().put("fintlabs.no/org-id", "flais.io");

        String name = NameFactory.createUsername(crd);
        String[] split = name.split("_");

        assertEquals("flais", split[0]);
        assertEquals("io", split[1]);
        assertEquals("test", split[2]);
    }

    @Test
    public void nameShouldNotBeTooLong() {
        crd.getMetadata().setName("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        crd.getMetadata().getLabels().put("fintlabs.no/org-id", "flais.io");

        assertThrows(IllegalArgumentException.class, () -> NameFactory.createUsername(crd));
    }

    @Test
    public void hyphenInOrgIdIsReplacedWithUnderscore() {
        crd.getMetadata().setName("test-test");
        crd.getMetadata().getLabels().put("fintlabs.no/org-id", "flais.io");

        String name = NameFactory.createUsername(crd);

        assertEquals("flais_io_test_test", name);
        assertFalse(name.contains("-"), "Username should not contain hyphen");
    }

    @Test
    public void usernameIsAlwaysLowerCase() {
        crd.getMetadata().setName("TestingTesting");
        crd.getMetadata().getLabels().put("fintlabs.no/org-id", "Flais.IO");

        String name = NameFactory.createUsername(crd);

        assertEquals("flais_io_testingtesting", name);
    }
}
