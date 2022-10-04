package no.fintlabs.operator.pg.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataAccessException
import spock.lang.Specification

@SpringBootTest
class PostgreSqlDataAccessServiceTest extends Specification {

    @Autowired
    public PostgreSqlDataAccessService postgreSqlDataAccessService

    def "Creating a database with a given name creates a database without throwing exceptions"() {
        given:
        String dbName = "testcreatedb"

        when:
        postgreSqlDataAccessService.createDb(dbName)

        then:
        postgreSqlDataAccessService.databaseExists(dbName)
        noExceptionThrown()
    }

    def "Creating a schema with a given name creates a schema without throwing exceptions"() {
        given:
        String databaseName = "schematestdb"
        String schemaName = "testcreateschema"

        when:
        postgreSqlDataAccessService.createSchema(databaseName, schemaName)

        then:
        postgreSqlDataAccessService.schemaExists(databaseName, schemaName)
        noExceptionThrown()
    }

    def "Create a database user with a given username and password creates a user without throwing exceptions"() {
        given:
        String databaseName = "usertestdb"
        String username = "testcreatedbuser"
        String password = "testpw"

        when:
        postgreSqlDataAccessService.createDbUser(databaseName, username, password)

        then:
        postgreSqlDataAccessService.userExists(databaseName, username)
        noExceptionThrown()
    }

    def "Create schema and user, and set privileges. Then check if privileges were granted for the user"() {
        given:
        String databaseName = "privilegetestdb"
        String username = "testgrantprivilegetouser"
        String password = "testpw"
        String schemaName = "testgrantprivilegetouserschema"
        List<String> privilege = ["SELECT", "INSERT"]

        when:
        postgreSqlDataAccessService.createDbUser(databaseName, username, password)
        postgreSqlDataAccessService.createSchema(databaseName, schemaName)
        postgreSqlDataAccessService.grantPrivilegeToUser(databaseName, schemaName, username, privilege[0])
        postgreSqlDataAccessService.grantPrivilegeToUser(databaseName, schemaName, username, privilege[1])

        then:
        noExceptionThrown()
        postgreSqlDataAccessService.getUserPrivilegesOnSchema(databaseName, schemaName, username) == "INSERT,SELECT"
    }

    def "Creating a user and schema with privileges throws no exceptions and sets privileges"() {
        given:
        String databaseName = "privilegetestdb"
        String schemaName = "testschema"
        String username = "testuser"
        String password = "testpw"
        List<String> privileges = ["SELECT", "INSERT"]

        when:
        postgreSqlDataAccessService.createSchemaUserAndSetPrivileges(databaseName, schemaName, username, password, privileges)

        then:
        noExceptionThrown()
        postgreSqlDataAccessService.getUserPrivilegesOnSchema(databaseName, schemaName, username) == "INSERT,SELECT"
    }

    def "Creating a database that already exists throws DataAccessException"(){
        given:
        String databaseName = "testcreatedb"
        postgreSqlDataAccessService.createDb(databaseName)

        when:
        postgreSqlDataAccessService.createDb(databaseName)

        then:
        thrown(DataAccessException)
    }

    def "Creating a database user that already exists throws DataAccessException"(){
        given:
        String databaseName = "doubleusertestdb"
        String username = "testcreatedoubledbuser"
        String password = "testpw"
        postgreSqlDataAccessService.createDbUser(databaseName, username, password)

        when:
        postgreSqlDataAccessService.createDbUser(databaseName, username, password)

        then:
        thrown(DataAccessException)
    }


}
