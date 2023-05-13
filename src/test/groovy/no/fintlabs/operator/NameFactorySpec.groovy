package no.fintlabs.operator

import spock.lang.Specification

import static no.fintlabs.operator.NameFactory.createUsername

class NameFactorySpec extends Specification {

    def "Name should be in the format orgId + crd name"() {
        given:
        def crd = new PGUserCRD()
        crd.getMetadata().setName("test-test-test")
        crd.getMetadata().getLabels().put("fintlabs.no/org-id", "flais.io")

        when:
        def name = createUsername(crd)

        then:
        name == "flais_io_test_test_test"
    }

    def "Name should contain orgId and crd name"() {
        given:
        def crd = new PGUserCRD()
        crd.getMetadata().setName("test")
        crd.getMetadata().getLabels().put("fintlabs.no/org-id", "flais.io")

        when:
        def name = createUsername(crd)
        def split = name.split("_")

        then:
        name
        split[0] == "flais"
        split[1] == "io"
        split[2] == "test"

    }

    def "If name is more than 63 charaters long an exception should be thrown"() {
        given:
        def crd = new PGUserCRD()
        crd.getMetadata().setName("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
        crd.getMetadata().getLabels().put("fintlabs.no/org-id", "flais.io")

        when:
        createUsername(crd)

        then:
        thrown(IllegalArgumentException)
    }
}
