package no.fintlabs.operator.pg.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class PostgreSqlDataAccessServiceTest extends Specification {

    @Autowired
    public PostgreSqlDataAccessService postgreSqlDataAccessService

    def "Creating a database with a given name, throws no exceptions"() {
        given:
        String dbName = "testcreatedb"

        when:
        postgreSqlDataAccessService.createDb(dbName)

        then:
        noExceptionThrown()
    }

    def "Creating a schema with a given name throws no exceptions"() {
        given:
        String databaseName = "schematestdb"
        String schemaName = "testcreateschema"

        when:
        postgreSqlDataAccessService.createSchema(databaseName, schemaName)

        then:
        noExceptionThrown()
    }

    def "Create a database user with a given username and password throws no exceptions"() {
        given:
        String databaseName = "usertestdb"
        String username = "testcreatedbuser"
        String password = "testpw"

        when:
        postgreSqlDataAccessService.createDbUser(databaseName, username, password)

        then:
        noExceptionThrown()
    }

    def "Create schema and user, and set privileges. Then check if privileges were granted for the user"() {
        given:
        String databaseName = "privilegetestdb"
        String username = "testgrantprivilegetouser"
        String password = "testpw"
        String schemaName = "testgrantprivilegetouserschema"
        String privilege = "SELECT, INSERT"

        when:
        postgreSqlDataAccessService.createDbUser(databaseName, username, password)
        postgreSqlDataAccessService.createSchema(databaseName, schemaName)
        postgreSqlDataAccessService.grantPrivilegeToUser(databaseName, schemaName, username, privilege)
        String privileges = postgreSqlDataAccessService.getUserPrivilegesOnSchema(databaseName, schemaName, username)

        then:
        noExceptionThrown()
        privileges == "INSERT,SELECT"
    }

    def "Creating a user and schema with privileges throws no exceptions and sets privileges"() {
        given:
        String databaseName = "privilegetestdb"
        String schemaName = "testschema"
        String username = "testuser"
        String password = "testpw"
        String privileges = "SELECT, INSERT"

        when:
        postgreSqlDataAccessService.createSchemaUserAndSetPrivileges(databaseName, schemaName, username, password, privileges)
        String getPrivileges = postgreSqlDataAccessService.getUserPrivilegesOnSchema(databaseName, schemaName, username)

        then:
        noExceptionThrown()
        getPrivileges == "INSERT,SELECT"
    }
}
