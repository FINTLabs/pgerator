package no.fintlabs.service;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.model.PGSchemaAndUser;
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
import java.util.Set;

@Slf4j
@Component
public class PostgreSqlDataAccessService {

    private String currentDatabase;

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
        try {
            currentDatabase = jdbcTemplate.getDataSource().getConnection().getCatalog();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean databaseExists(String dbName) throws DataAccessException {
        List<String> results = jdbcTemplate.query(SqlQueryFactory.generateDatabaseExistsSql(dbName), (rs, rowNum) -> rs.getString("datname"));
        return results.size() > 0;
    }

    public boolean schemaExists(String dbName, String schemaName) throws DataAccessException {
        changeDatabase(dbName);
        List<String> results = jdbcTemplate.query(SqlQueryFactory.generateSchemaExistsSql(schemaName), (rs, rowNum) -> rs.getString("schema_name"));
        return results.size() > 0;
    }

    public boolean userExists(String dbName, String username) throws DataAccessException {
        changeDatabase(dbName);
        List<String> results = jdbcTemplate.query(SqlQueryFactory.generateUserExistsSql(username), (rs, rowNum) -> rs.getString("usename"));
        return results.size() > 0;
    }

    public String getUser(String dbName, String username) throws DataAccessException {
        changeDatabase(dbName);
        List<String> results = jdbcTemplate.query(SqlQueryFactory.generateUserExistsSql(username), (rs, rowNum) -> rs.getString("usename"));
        return results.get(0);
    }

    public String getSchema(String dbName, String schemaName) throws DataAccessException {
        changeDatabase(dbName);
        List<String> results = jdbcTemplate.query(SqlQueryFactory.generateSchemaExistsSql(schemaName), (rs, rowNum) -> rs.getString("schema_name"));
        return results.get(0);
    }

    public Set<PGSchemaAndUser> getSchemaAndUser(String dbName, String schemaName, String username) {
        changeDatabase(dbName);
        List<String> userResults = jdbcTemplate.query(SqlQueryFactory.generateUserExistsSql(username), (rs, rowNum) -> rs.getString("usename"));
        List<String> schemaResults = jdbcTemplate.query(SqlQueryFactory.generateSchemaExistsSql(schemaName), (rs, rowNum) -> rs.getString("schema_name"));
        if (userResults.size() > 0 && schemaResults.size() > 0) {
            return Set.of(PGSchemaAndUser.builder()
                    .username(userResults.get(0))
                    .schemaName(schemaResults.get(0))
                    .build());
        }
        return Set.of();
    }

    public void createDb(String dbName) throws DataAccessException {
        jdbcTemplate.execute(SqlQueryFactory.generateCreateDatabaseSql(dbName));
        log.info("Database created: " + dbName);
    }

    public void deleteSchema(String dbName, String schemaName) throws DataAccessException {
        changeDatabase(dbName);
        jdbcTemplate.execute(SqlQueryFactory.generateDeleteSchemaSql(schemaName));
        log.info("Schema deleted: " + schemaName);
    }

    public void deleteUser(String dbName, String username) throws DataAccessException {
        changeDatabase(dbName);
        jdbcTemplate.execute(SqlQueryFactory.generateReassignOwnedFromUserToUserSql(username, "postgres"));
        jdbcTemplate.execute(SqlQueryFactory.generateDropOwnedByUserSql(username));
        jdbcTemplate.execute(SqlQueryFactory.generateDropUserSql(username));
        log.info("User deleted:" + username);
    }

    private void changeDatabase(String databaseName) {
        if (!databaseExists(databaseName)) {
            createDb(databaseName);
        }
        if (!currentDatabase.equalsIgnoreCase(databaseName)) {
            DataSource dataSource = DataSourceBuilder.create()
                    .url(environment.getProperty("spring.datasource.base-url") + databaseName.toLowerCase())
                    .username(environment.getProperty("spring.datasource.username"))
                    .password(environment.getProperty("spring.datasource.password"))
                    .build();
            jdbcTemplate.setDataSource(dataSource);
            currentDatabase = databaseName;
            log.info("Database changed to: " + databaseName);
        }
    }

    public void createSchema(String databaseName, String schemaName) throws DataAccessException {
        changeDatabase(databaseName);
        jdbcTemplate.execute(SqlQueryFactory.generateCreateSchemaSql(schemaName));
        log.info("Schema created: " + schemaName);
    }

    public void createDbUser(String databaseName, String username, String password) throws DataAccessException {
        changeDatabase(databaseName);
        jdbcTemplate.execute(SqlQueryFactory.generateCreateDatabaseUserSql(username, password));
        log.info("User " + username + " created");
    }


    public void grantPrivilegeToUser(String databaseName, String schemaName, String username, String privilege) throws DataAccessException {
        changeDatabase(databaseName);
        jdbcTemplate.execute(SqlQueryFactory.generateGrantPrivilegeSql(schemaName, privilege, username));
        jdbcTemplate.execute(SqlQueryFactory.generateGrantDefaultPrivilegesSql(schemaName, privilege, username));
        log.info("Privilege " + privilege + " granted to " + username + " on schema " + schemaName);
    }

    public String getUserPrivilegesOnSchema(String databaseName, String schemaName, String username) throws DataAccessException {
        changeDatabase(databaseName);

        log.info("Creating test table");
        jdbcTemplate.execute(SqlQueryFactory.generateCreateTestTable(schemaName));

        List<String> privileges = jdbcTemplate.query(SqlQueryFactory.generateGetPrivileges(schemaName, username), (rs, rowNum) -> rs.getString("privilege_type"));
        log.info("Privileges for \"" + username + "\" on schema " + schemaName + ": " + privileges);

        log.info("Dropping test table");
        jdbcTemplate.execute(SqlQueryFactory.generateDropTestTableSql(schemaName));

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
