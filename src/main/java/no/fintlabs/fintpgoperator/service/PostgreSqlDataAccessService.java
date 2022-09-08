package no.fintlabs.fintpgoperator.service;

import no.fintlabs.fintpgoperator.Operator;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
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

    public List<Operator> getAllEntries() {
        String sql = "SELECT * FROM operatortest";
        try {
            logger.log(Level.WARNING, "Operators retrieved");
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Operator.class));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in getAllEntries: " + e.getMessage());
        }
        return null;
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

    public String createRole(String roleName, String password) {
        String sqlCreateRole = "CREATE ROLE " + roleName + " WITH LOGIN PASSWORD '" + password + "'";
        try {
            jdbcTemplate.execute(sqlCreateRole);
            logger.log(Level.INFO, "Role created");
            return "Role created";
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in createRole: " + e.getMessage());
        }
        return null;
    }

    public String grantRoleToUser(String roleName, String username) {
        String sqlGrantRoleToUser = "GRANT " + roleName + " TO " + username;
        try {
            jdbcTemplate.execute(sqlGrantRoleToUser);
            logger.log(Level.INFO, "Role " + roleName + " granted to user " + username);
            return "Role " + roleName + " granted to user " + username;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in grantRoleToUser: " + e.getMessage());
        }
        return null;
    }


    public String grantPrivilegeToRole(String schemaName, String roleName, String privilege) {
        String sqlGrantPrivilege = "GRANT " + privilege + " ON ALL TABLES IN SCHEMA " + schemaName + " TO " + roleName;
        String sqlGrantDefaultPrivileges = "ALTER DEFAULT PRIVILEGES IN SCHEMA " + schemaName + " GRANT " + privilege + " ON TABLES TO " + roleName;
        try {
            jdbcTemplate.execute(sqlGrantPrivilege);
            jdbcTemplate.execute(sqlGrantDefaultPrivileges);
            logger.log(Level.INFO, "Privilege " + privilege + " granted to role " + roleName + " on schema " + schemaName);
            return "Privilege " + privilege + " granted to role " + roleName + " on schema " + schemaName;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in grantPrivilegeToRole: " + e.getMessage());
            return "Error in grantPrivilegeToRole: " + e.getMessage();
        }
    }

    public String checkRolePrivilegesOnSchema(String schemaName, String roleName) {
        String sqlGetPrivileges = "SELECT grantee, table_schema, privilege_type FROM information_schema.role_table_grants WHERE grantee = '" + roleName + "' AND table_schema = '" + schemaName + "'";
        String sqlCreateTestTable = "CREATE TABLE " + schemaName + ".testtable (id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL)";
        String sqlDropTestTable = "DROP TABLE " + schemaName + ".testtable";
        try {
            logger.log(Level.INFO, "Creating test table");
            jdbcTemplate.execute(sqlCreateTestTable);
            List<String> privileges = jdbcTemplate.query(sqlGetPrivileges, (rs, rowNum) -> rs.getString("privilege_type"));
            logger.log(Level.INFO, "Privileges for role " + roleName + " on schema " + schemaName + ": " + privileges);
            logger.log(Level.INFO, "Dropping test table");
            jdbcTemplate.execute(sqlDropTestTable);
            return "Privileges for role " + roleName + " on schema " + schemaName + ": " + privileges;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in checkRolePrivilegesOnSchema: " + e.getMessage());
            return "Error in checkRolePrivilegesOnSchema: " + e.getMessage();
        }
    }

    public String createSchemaUserAndRole(String schemaName, String username, String password, String roleName) {
        createSchema(schemaName);
        createDbUser(username, password);
        createRole(roleName, password);
        grantPrivilegeToRole(schemaName, roleName, Privilege.ALL.toString());
        grantRoleToUser(roleName, username);
        return checkRolePrivilegesOnSchema(schemaName, roleName);
    }
}
