package no.fintlabs.operator.pg.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class OperatorControllerTest extends Specification {

    @Autowired
    private OperatorController operatorController

    def "OperatorController is created"() {
        expect:
        operatorController
    }
}
