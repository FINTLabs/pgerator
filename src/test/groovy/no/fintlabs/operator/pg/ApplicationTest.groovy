package no.fintlabs.operator.pg

import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTest extends Specification {
    def "Application is created"() {
        when:
        def app = Application

        then:
        app
    }
}
