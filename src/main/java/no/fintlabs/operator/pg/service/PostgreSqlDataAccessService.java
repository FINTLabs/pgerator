package no.fintlabs.operator.pg.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class PostgreSqlDataAccessService {
    private JdbcTemplate jdbcTemplate;

    public enum Privilege {
        SELECT,
        INSERT,
        UPDATE,
        DELETE,
        TRUNCATE,
        REFERENCES,
        TRIGGER,
        CREATE,
        CONNECT,
        TEMPORARY,
        EXECUTE,
        USAGE,
        ALL
    }

    public PostgreSqlDataAccessService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createDb(String dbName) throws DataAccessException {
        jdbcTemplate.execute("CREATE DATABASE " + dbName);
    }

    public String createSchema(String schemaName) {
        String sqlCreateSchema = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
        try {
            jdbcTemplate.execute(sqlCreateSchema);
            log.info("Schema " + schemaName + " created");
            return "Schema " + schemaName + " created";
        } catch (Exception e) {
            log.error("Error in createSchema: " + e.getMessage());
        }
        return null;
    }

    public String createDbUser(String username, String password) {
        String sqlCreateDbUser = "CREATE USER " + username + " WITH PASSWORD '" + password + "'";
        try {
            jdbcTemplate.execute(sqlCreateDbUser);
            log.info("User " + username + " created");
            return "User " + username + " created";
        } catch (Exception e) {
            log.error("Error in createDbUser: " + e.getMessage());
        }
        return null;
    }


    public String grantPrivilegeToUser(String schemaName, String username, String privilege) {
        String sqlGrantPrivilege = "GRANT " + privilege + " ON ALL TABLES IN SCHEMA " + schemaName + " TO " + username;
        String sqlGrantDefaultPrivileges = "ALTER DEFAULT PRIVILEGES IN SCHEMA " + schemaName + " GRANT " + privilege + " ON TABLES TO " + username;
        try {
            jdbcTemplate.execute(sqlGrantPrivilege);
            jdbcTemplate.execute(sqlGrantDefaultPrivileges);
            log.info("Privilege " + privilege + " granted to " + username + " on schema " + schemaName);
            return "Privilege " + privilege + " granted to " + username + " on schema " + schemaName;
        } catch (Exception e) {
            log.error("Error in grantPrivilegeToUser: " + e.getMessage());
            return "Error in grantPrivilegeToUser: " + e.getMessage();
        }
    }

    public String checkUserPrivilegesOnSchema(String schemaName, String username) {
        String sqlGetPrivileges = "SELECT grantee, table_schema, privilege_type FROM information_schema.role_table_grants WHERE grantee = '" + username + "' AND table_schema = '" + schemaName + "'";
        String sqlCreateTestTable = "CREATE TABLE " + schemaName + ".testtable (id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL)";
        String sqlDropTestTable = "DROP TABLE " + schemaName + ".testtable";
        try {
            log.info("Creating test table");
            jdbcTemplate.execute(sqlCreateTestTable);
            List<String> privileges = jdbcTemplate.query(sqlGetPrivileges, (rs, rowNum) -> rs.getString("privilege_type"));
            log.info("Privileges for " + username + " on schema " + schemaName + ": " + privileges);
            log.info("Dropping test table");
            jdbcTemplate.execute(sqlDropTestTable);
            return "Privileges for " + username + " on schema " + schemaName + ": " + privileges;
        } catch (Exception e) {
            log.error("Error in checkRolePrivilegesOnSchema: " + e.getMessage());
            return "Error in checkUserPrivilegesOnSchema: " + e.getMessage();
        }
    }

    public String createSchemaUserAndSetPrivileges(String schemaName, String username, String password, String privileges) {
        createSchema(schemaName);
        createDbUser(username, password);
        String[] privilegesArray = privileges.split(",");
        for (String privilege : privilegesArray) {
            if (Arrays.stream(Privilege.class.getEnumConstants()).anyMatch(e -> e.name().equals(privilege.toUpperCase().trim()))) {
                grantPrivilegeToUser(schemaName, username, privilege);
            }
        }
        return checkUserPrivilegesOnSchema(schemaName, username);
    }
}
