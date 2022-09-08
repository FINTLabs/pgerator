package no.fintlabs.fintpgoperator.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class PostgreSqlDataAccessService {
    private JdbcTemplate jdbcTemplate;
    private Logger logger;

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
        this.logger = Logger.getLogger(PostgreSqlDataAccessService.class.getName());
    }

    public String createPostgresDb(String dbName) {
        String sql = "CREATE DATABASE " + dbName;
        try {
            jdbcTemplate.execute(sql);
            //
            return "Database " + dbName + " created";
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            return "Database " + dbName + " not created due to error: " + e.getMessage();
        }
    }

    public String createSchema(String schemaName) {
        String sqlCreateSchema = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
        try {
            jdbcTemplate.execute(sqlCreateSchema);
            logger.log(Level.INFO, "Schema " + schemaName + " created");
            return "Schema " + schemaName + " created";
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in createSchema: " + e.getMessage());
        }
        return null;
    }

    public String createDbUser(String username, String password) {
        String sqlCreateDbUser = "CREATE USER " + username + " WITH PASSWORD '" + password + "'";
        try {
            jdbcTemplate.execute(sqlCreateDbUser);
            logger.log(Level.INFO, "User " + username + " created");
            return "User " + username + " created";
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in createDbUser: " + e.getMessage());
        }
        return null;
    }


    public String grantPrivilegeToUser(String schemaName, String username, String privilege) {
        String sqlGrantPrivilege = "GRANT " + privilege + " ON ALL TABLES IN SCHEMA " + schemaName + " TO " + username;
        String sqlGrantDefaultPrivileges = "ALTER DEFAULT PRIVILEGES IN SCHEMA " + schemaName + " GRANT " + privilege + " ON TABLES TO " + username;
        try {
            jdbcTemplate.execute(sqlGrantPrivilege);
            jdbcTemplate.execute(sqlGrantDefaultPrivileges);
            logger.log(Level.INFO, "Privilege " + privilege + " granted to " + username + " on schema " + schemaName);
            return "Privilege " + privilege + " granted to " + username + " on schema " + schemaName;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in grantPrivilegeToUser: " + e.getMessage());
            return "Error in grantPrivilegeToUser: " + e.getMessage();
        }
    }

    public String checkUserPrivilegesOnSchema(String schemaName, String username) {
        String sqlGetPrivileges = "SELECT grantee, table_schema, privilege_type FROM information_schema.role_table_grants WHERE grantee = '" + username + "' AND table_schema = '" + schemaName + "'";
        String sqlCreateTestTable = "CREATE TABLE " + schemaName + ".testtable (id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL)";
        String sqlDropTestTable = "DROP TABLE " + schemaName + ".testtable";
        try {
            logger.log(Level.INFO, "Creating test table");
            jdbcTemplate.execute(sqlCreateTestTable);
            List<String> privileges = jdbcTemplate.query(sqlGetPrivileges, (rs, rowNum) -> rs.getString("privilege_type"));
            logger.log(Level.INFO, "Privileges for " + username + " on schema " + schemaName + ": " + privileges);
            logger.log(Level.INFO, "Dropping test table");
            jdbcTemplate.execute(sqlDropTestTable);
            return "Privileges for " + username + " on schema " + schemaName + ": " + privileges;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in checkRolePrivilegesOnSchema: " + e.getMessage());
            return "Error in checkUserPrivilegesOnSchema: " + e.getMessage();
        }
    }

    public String createSchemaUserAndSetPrivileges(String schemaName, String username, String password) {
        createSchema(schemaName);
        createDbUser(username, password);
        grantPrivilegeToUser(schemaName, username, Privilege.ALL.toString());
        return checkUserPrivilegesOnSchema(schemaName, username);
    }
}
