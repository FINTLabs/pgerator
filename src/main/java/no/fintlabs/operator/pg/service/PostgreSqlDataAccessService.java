package no.fintlabs.operator.pg.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class PostgreSqlDataAccessService {

    @Autowired
    private Environment environment;
    private final JdbcTemplate jdbcTemplate;

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

    public boolean databaseExists(String dbName) throws DataAccessException {
        List<String> results = jdbcTemplate.query("SELECT datname FROM pg_catalog.pg_database WHERE lower(datname) = '" + dbName.toLowerCase() + "'", (rs, rowNum) -> rs.getString("datname"));
        return results.size() > 0;
    }

    public boolean schemaExists(String dbName, String schemaName) throws DataAccessException {
        changeDatabase(dbName);
        List<String> results = jdbcTemplate.query("SELECT schema_name FROM information_schema.schemata WHERE lower(schema_name) = '" + schemaName.toLowerCase() + "'", (rs, rowNum) -> rs.getString("schema_name"));
        return results.size() > 0;
    }

    public boolean userExists(String dbName, String username) throws DataAccessException {
        changeDatabase(dbName);
        List<String> results = jdbcTemplate.query("SELECT usename FROM pg_catalog.pg_user WHERE lower(usename) = '" + username.toLowerCase() + "'", (rs, rowNum) -> rs.getString("usename"));
        return results.size() > 0;
    }

    public void createDb(String dbName) throws DataAccessException {
        jdbcTemplate.execute("CREATE DATABASE " + dbName);
        log.info("Database created: " + dbName);
    }

    public void deleteSchema(String dbName, String schemaName) throws DataAccessException {
        changeDatabase(dbName);
        jdbcTemplate.execute("DROP SCHEMA " + schemaName + " CASCADE");
        log.info("Schema deleted: " + schemaName);
    }

    public void deleteUser(String dbName, String username) throws DataAccessException {
        changeDatabase(dbName);
        jdbcTemplate.execute("REASSIGN OWNED BY \"" + username + "\" TO postgres;");
        jdbcTemplate.execute("DROP OWNED BY \"" + username + "\";");
        jdbcTemplate.execute( "DROP USER \"" + username + "\";");
        log.info("User deleted:" + username);
    }

    private void changeDatabase(String databaseName) {
        if (!databaseExists(databaseName)) {
            createDb(databaseName);
        }
        try {
            if (!Objects.equals(jdbcTemplate.getDataSource().getConnection().getCatalog().toLowerCase(), databaseName.toLowerCase())) {
                DataSource dataSource = DataSourceBuilder.create()
                        .url(environment.getProperty("spring.datasource.base-url") + databaseName.toLowerCase())
                        .username(environment.getProperty("spring.datasource.username"))
                        .password(environment.getProperty("spring.datasource.password"))
                        .build();
                jdbcTemplate.setDataSource(dataSource);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createSchema(String databaseName, String schemaName) throws DataAccessException {
        changeDatabase(databaseName);
        String sqlCreateSchema = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
        jdbcTemplate.execute(sqlCreateSchema);
        log.info("Schema created: " + schemaName);
    }

    public void createDbUser(String databaseName, String username, String password) throws DataAccessException {
        changeDatabase(databaseName);
        String sqlCreateDbUser = "CREATE USER \"" + username + "\" WITH PASSWORD '" + password + "'";
        jdbcTemplate.execute(sqlCreateDbUser);
        log.info("User " + username + " created");
    }


    public void grantPrivilegeToUser(String databaseName, String schemaName, String username, String privilege) throws DataAccessException {
        changeDatabase(databaseName);
        String sqlGrantPrivilege = "GRANT " + privilege + " ON ALL TABLES IN SCHEMA " + schemaName + " TO \"" + username + "\"";
        String sqlGrantDefaultPrivileges = "ALTER DEFAULT PRIVILEGES IN SCHEMA " + schemaName + " GRANT " + privilege + " ON TABLES TO \"" + username + "\"";
        jdbcTemplate.execute(sqlGrantPrivilege);
        jdbcTemplate.execute(sqlGrantDefaultPrivileges);
        log.info("Privilege " + privilege + " granted to " + username + " on schema " + schemaName);
    }

    public String getUserPrivilegesOnSchema(String databaseName, String schemaName, String username) throws DataAccessException {
        changeDatabase(databaseName);
        String sqlGetPrivileges = "SELECT grantee, table_schema, privilege_type FROM information_schema.role_table_grants WHERE grantee = \"" + username + "\" AND table_schema = '" + schemaName + "'";
        String sqlCreateTestTable = "CREATE TABLE " + schemaName + ".testtable (id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL)";
        String sqlDropTestTable = "DROP TABLE " + schemaName + ".testtable";

        log.info("Creating test table");
        jdbcTemplate.execute(sqlCreateTestTable);

        List<String> privileges = jdbcTemplate.query(sqlGetPrivileges, (rs, rowNum) -> rs.getString("privilege_type"));
        log.info("Privileges for \"" + username + "\" on schema " + schemaName + ": " + privileges);

        log.info("Dropping test table");
        jdbcTemplate.execute(sqlDropTestTable);

        return String.join(",", privileges);
    }

    public void createSchemaUserAndSetPrivileges(String databaseName, String schemaName, String username, String password, List<String> privileges) throws DataAccessException {
        createSchema(databaseName, schemaName);
        createDbUser(databaseName, username, password);
        for (String privilege : privileges) {
            if (Arrays.stream(Privilege.class.getEnumConstants()).anyMatch(e -> e.name().equals(privilege.toUpperCase().trim()))) {
                grantPrivilegeToUser(databaseName, schemaName, username, privilege);
            }
        }
    }
}
