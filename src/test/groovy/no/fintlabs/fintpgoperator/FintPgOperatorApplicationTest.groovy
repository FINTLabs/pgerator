package no.fintlabs.fintpgoperator

import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FintPgOperatorApplicationTest extends Specification {
    def "Application is created"() {
        when:
        def app = FintPgOperatorApplication

        then:
        app
    }
}
