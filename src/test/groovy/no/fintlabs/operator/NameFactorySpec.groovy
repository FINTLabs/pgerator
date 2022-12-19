package no.fintlabs.operator

import spock.lang.Specification

import static no.fintlabs.operator.NameFactory.createDatabaseAndUserName

class NameFactorySpec extends Specification {

    def "Name should end with a 6 charater random string"() {
        given:
        def crd = new PGDatabaseAndUserCRD()
        crd.getMetadata().getLabels().put("fintlabs.no/org-id", "flais.io")
        crd.getMetadata().getLabels().put("fintlabs.no/team", "flais")

        when:
        def name = createDatabaseAndUserName(crd)
        def split = name.split("_")

        then:
        name
        split[2].length() == 6
    }

    def "Name should contain orgId and team name"() {
        given:
        def crd = new PGDatabaseAndUserCRD()
        crd.getMetadata().getLabels().put("fintlabs.no/org-id", "flais.io")
        crd.getMetadata().getLabels().put("fintlabs.no/team", "flais")

        when:
        def name = createDatabaseAndUserName(crd)
        def split = name.split("_")

        then:
        name
        split[0] == "flais-io"
        split[1] == "flais"

    }

    def "If name is more than 40 charaters long an exception should be thrown"() {
        given:
        def crd = new PGDatabaseAndUserCRD()
        crd.getMetadata().getLabels().put("fintlabs.no/org-id", "flais.io")
        crd.getMetadata().getLabels().put("fintlabs.no/team", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")

        when:
        createDatabaseAndUserName(crd)

        then:

        thrown(IllegalArgumentException)
    }
}
