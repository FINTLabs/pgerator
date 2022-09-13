package no.fintlabs.operator.pg.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class PostgreSqlDataAccessServiceTest extends Specification {

    @Autowired
    public PostgreSqlDataAccessService postgreSqlDataAccessService

    def "Creating a database with a given name, the database should be created and return a string saying the database is created"() {
        when:
        String dbName = "testcreatedb"
        String dbCreated = postgreSqlDataAccessService.createDb(dbName)

        then:
        dbCreated == "Database " + dbName + " created"
    }

    def "Creating a schema with a given name should return a string saying the schema is created"() {
        when:
        String schemaName = "testcreateschema"
        String schemaCreated = postgreSqlDataAccessService.createSchema(schemaName)
        then:
        schemaCreated == "Schema " + schemaName + " created"
    }

    def "Create a database user with a given username and password"() {
        when:
        String username = "testcreatedbuser"
        String password = "testpw"
        String userCreated = postgreSqlDataAccessService.createDbUser(username, password)

        then:
        userCreated == "User " + username + " created"
    }

    def "Create schema and user, and set privileges. Then check if privileges were granted for the user"() {
        when:
        String username = "testgrantprivilegetouser"
        String password = "testpw"
        String schemaName = "testgrantprivilegetouserschema"
        String userCreated = postgreSqlDataAccessService.createDbUser(username, password)
        String schemaCreated = postgreSqlDataAccessService.createSchema(schemaName)
        String privilege = "SELECT, INSERT"
        String privilegeGranted = postgreSqlDataAccessService.grantPrivilegeToUser(schemaName, username, privilege)
        String privileges = postgreSqlDataAccessService.getUserPrivilegesOnSchema(schemaName, username)

        then:
        userCreated == "User " + username + " created"
        schemaCreated == "Schema " + schemaName + " created"
        privilegeGranted == "Privilege " + privilege + " granted to " + username + " on schema " + schemaName
        privileges == "Privileges for " + username + " on schema " + schemaName + ": [INSERT, SELECT]"
    }

    def "Creating a user and schema with privileges should return a message that privileges were set"() {
        when:
        String schemaName = "testschema"
        String username  ="testuser"
        String password = "testpw"
        String privileges = "SELECT, INSERT"
        String createSchemaUserAndSetPrivileges = postgreSqlDataAccessService.createSchemaUserAndSetPrivileges(schemaName, username, password, privileges)

        then:
        createSchemaUserAndSetPrivileges == "Privileges for " + username + " on schema " + schemaName + ": [INSERT, SELECT]"
    }
}
