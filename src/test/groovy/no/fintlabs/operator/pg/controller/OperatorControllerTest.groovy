package no.fintlabs.operator.pg.controller

import no.fintlabs.operator.pg.model.DatabaseCreateRequest
import no.fintlabs.operator.pg.model.SchemaUserCreateRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

@SpringBootTest
class OperatorControllerTest extends Specification {

    @Autowired
    private OperatorController operatorController

    def "OperatorController is created"() {
        expect:
        operatorController
    }

    def "OperatorController create database should return a 201 created"() {
        given:
        DatabaseCreateRequest databaseCreateRequest = new DatabaseCreateRequest(databaseName: "spocktestdb")

        when:
        ResponseEntity response = operatorController.createDb(databaseCreateRequest)

        then:
        response.getStatusCode().is2xxSuccessful()
        response.getStatusCode() == HttpStatus.CREATED
    }

    def "OperatorController create schema, user and set privileges should return a 201 created when successfull"() {
        given:
        String databaseName = "spocktestdb"
        String schemaName = "spocktestschema"
        String userName = "spocktestuser"
        SchemaUserCreateRequest request= new SchemaUserCreateRequest(password: "spocktestpassword", privileges: "select, insert")

        when:
        ResponseEntity response = operatorController.createSchemaUserAndSetPrivileges(databaseName, schemaName, userName, request)

        then:
        response.getStatusCode().is2xxSuccessful()
        response.getStatusCode() == HttpStatus.CREATED
    }

}
