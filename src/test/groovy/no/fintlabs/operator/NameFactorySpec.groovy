package no.fintlabs.operator

import spock.lang.Specification

import static no.fintlabs.operator.NameFactory.createUsername

class NameFactorySpec extends Specification {

    def "Name should end with a 7 charater random string"() {
        given:
        def crd = new PGUserCRD()
        crd.getMetadata().getLabels().put("fintlabs.no/org-id", "flais.io")
        crd.getMetadata().getLabels().put("fintlabs.no/team", "flais")

        when:
        def name = createUsername(crd)
        def split = name.split("_")

        then:
        name
        split[3].length() == 7
    }

    def "Name should contain orgId and team name"() {
        given:
        def crd = new PGUserCRD()
        crd.getMetadata().getLabels().put("fintlabs.no/org-id", "flais.io")
        crd.getMetadata().getLabels().put("fintlabs.no/team", "flais")

        when:
        def name = createUsername(crd)
        def split = name.split("_")

        then:
        name
        split[0] == "flais"
        split[1] == "io"
        split[2] == "flais"

    }

    def "If name is more than 40 charaters long an exception should be thrown"() {
        given:
        def crd = new PGUserCRD()
        crd.getMetadata().getLabels().put("fintlabs.no/org-id", "flais.io")
        crd.getMetadata().getLabels().put("fintlabs.no/team", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")

        when:
        createUsername(crd)

        then:

        thrown(IllegalArgumentException)
    }
}
