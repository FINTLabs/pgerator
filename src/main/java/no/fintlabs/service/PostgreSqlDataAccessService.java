package no.fintlabs.service;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.model.PGSchemaAndUser;
import no.fintlabs.operator.SchemaNameFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component
public class PostgreSqlDataAccessService {

    private String currentDatabase;

    private final Environment environment;
    private final JdbcTemplate jdbcTemplate;

//    public enum Privilege {
//        SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER, CREATE, CONNECT, TEMPORARY, EXECUTE, USAGE, ALL
//    }

    public PostgreSqlDataAccessService(JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        try {
            currentDatabase = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection().getCatalog();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.environment = environment;
    }

    private boolean databaseExists(String dbName) throws DataAccessException {
        List<String> results = jdbcTemplate.query(SqlQueryFactory.generateDatabaseExistsSql(dbName), (rs, rowNum) -> rs.getString("datname"));
        return results.size() > 0;
    }

//    public boolean schemaExists(String dbName, String schemaName) throws DataAccessException {
//        useDatabase(dbName);
//        List<String> results = jdbcTemplate.query(SqlQueryFactory.schemaExistsSql(schemaName), (rs, rowNum) -> rs.getString("schema_name"));
//        return results.size() > 0;
//    }

    private boolean userExists(PGSchemaAndUser desired/*String dbName, String username*/) throws DataAccessException {
        useDatabase(desired.getDatabase());
        List<String> results = jdbcTemplate.query(SqlQueryFactory.generateUserExistsSql(desired.getUsername()), (rs, rowNum) -> rs.getString("usename"));
        return results.size() > 0;
    }

    public Set<PGSchemaAndUser> getSchemaAndUser(String dbName, String schemaName, String username) {
        useDatabase(dbName);
        List<String> userResults = jdbcTemplate.query(SqlQueryFactory.generateUserExistsSql(username), (rs, rowNum) -> rs.getString("usename"));
        List<String> schemaResults = jdbcTemplate.query(SqlQueryFactory.schemaExistsSql(schemaName), (rs, rowNum) -> rs.getString("schema_name"));
        if (userResults.size() > 0 && schemaResults.size() > 0) {
            return Set.of(
                    PGSchemaAndUser
                            .builder()
                            .database(dbName)
                            .username(userResults.get(0))
                            .schemaName(schemaResults.get(0))
                            .build());
        }
        return Collections.emptySet();
    }

    private void createDb(String dbName) throws DataAccessException {
        jdbcTemplate.execute(SqlQueryFactory.generateCreateDatabaseSql(dbName));
        log.info("Database created: " + dbName);
    }

    public void makeSchemaOrphan(PGSchemaAndUser pgSchemaAndUser) throws DataAccessException {
        useDatabase(pgSchemaAndUser.getDatabase());

        jdbcTemplate.execute(SqlQueryFactory.schemaRenameSql(pgSchemaAndUser.getSchemaName(), SchemaNameFactory.orphanSchemaNameFromName(pgSchemaAndUser.getSchemaName())));
        log.info("Schema deleted: " + pgSchemaAndUser.getSchemaName());
    }

    public void deleteUser(PGSchemaAndUser pgSchemaAndUser) throws DataAccessException {
        useDatabase(pgSchemaAndUser.getDatabase());
        jdbcTemplate.execute(SqlQueryFactory.revokeDefaultPrivilegesSql(pgSchemaAndUser.getUsername(), pgSchemaAndUser.getSchemaName()));
        jdbcTemplate.execute(SqlQueryFactory.revokeAllPriviligesSql(pgSchemaAndUser.getUsername(), pgSchemaAndUser.getSchemaName()));
        jdbcTemplate.execute(SqlQueryFactory.generateDropUserSql(pgSchemaAndUser.getUsername()));
        log.info("User deleted:" + pgSchemaAndUser.getUsername());
    }

    private void useDatabase(String database) {
        ensureDatabase(database);
        if (!currentDatabase.equalsIgnoreCase(database)) {
            try {
                DataSourceUtils.doCloseConnection(DataSourceUtils.getConnection(Objects.requireNonNull(jdbcTemplate.getDataSource())), jdbcTemplate.getDataSource());
                DataSource dataSource = DataSourceBuilder.create()
                        .url(environment.getProperty("spring.datasource.base-url")
                                + database.toLowerCase()
                                + "?sslmode=require")
                        .username(environment.getProperty("spring.datasource.username"))
                        .password(environment.getProperty("spring.datasource.password"))
                        .build();

                jdbcTemplate.setDataSource(dataSource);

                currentDatabase = database;
                log.info("Database changed to: " + database);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void ensureDatabase(String database) {
        if (!databaseExists(database)) {
            createDb(database);
        }
    }

    public void ensureSchema(PGSchemaAndUser desired) throws DataAccessException {
        useDatabase(desired.getDatabase());

        hasOrphanSchema(desired)
                .ifPresentOrElse(
                        oldSchemaName -> renameSchema(desired, oldSchemaName),
                        () -> createSchemaIfNotExits(desired)
                );


        log.info("Schema ensured: " + desired.getSchemaName());
    }

    private void createSchemaIfNotExits(PGSchemaAndUser pgSchemaAndUser) {
        useDatabase(pgSchemaAndUser.getDatabase());
        jdbcTemplate.execute(SqlQueryFactory.schemaCreateIfNotExistSql(pgSchemaAndUser.getSchemaName()));
        log.debug("Created schema with name {} ", pgSchemaAndUser.getSchemaName());
    }

    private void renameSchema(PGSchemaAndUser pgSchemaAndUser, String oldName) {
        useDatabase(pgSchemaAndUser.getDatabase());
        jdbcTemplate.execute(SqlQueryFactory.schemaRenameSql(oldName, pgSchemaAndUser.getSchemaName()));
    }

    private Optional<String> hasOrphanSchema(PGSchemaAndUser pgSchemaAndUser) {
        useDatabase(pgSchemaAndUser.getDatabase());
        List<String> results = jdbcTemplate.query(SqlQueryFactory.schemaExistsSql(pgSchemaAndUser.getSchemaName() + "_orphan_%"), (rs, rowNum) -> rs.getString("schema_name"));

        if (results.size() == 1) {
            return Optional.of(results.get(0));
        }

        return Optional.empty();

    }

    public void ensureUser(PGSchemaAndUser desired) throws DataAccessException {
        useDatabase(desired.getDatabase());
        if (!userExists(desired)) {
            jdbcTemplate.execute(SqlQueryFactory.databaseUserCreateSql(desired.getUsername(), desired.getPassword()));
            log.debug("User " + desired.getUsername() + " created");
        } else {
            log.debug("User {} already exists", desired.getUsername());
        }
        grantPrivilegeToUser(desired);
    }


    private void grantPrivilegeToUser(PGSchemaAndUser pgSchemaAndUser) throws DataAccessException {
        useDatabase(pgSchemaAndUser.getDatabase());
        jdbcTemplate.execute(SqlQueryFactory.generateGrantPrivilegeSql(pgSchemaAndUser.getSchemaName(), "all", pgSchemaAndUser.getUsername()));
        jdbcTemplate.execute(SqlQueryFactory.generateGrantDefaultPrivilegesSql(pgSchemaAndUser.getSchemaName(), "all", pgSchemaAndUser.getUsername()));
        log.info("Privilege " + "all" + " granted to " + pgSchemaAndUser.getUsername() + " on schema " + pgSchemaAndUser.getSchemaName());
    }

//    public String getUserPrivilegesOnSchema(String databaseName, String schemaName, String username) throws DataAccessException {
//        useDatabase(databaseName);
//
//        log.info("Creating test table");
//        jdbcTemplate.execute(SqlQueryFactory.generateCreateTestTable(schemaName));
//
//        List<String> privileges = jdbcTemplate.query(SqlQueryFactory.generateGetPrivileges(schemaName, username), (rs, rowNum) -> rs.getString("privilege_type"));
//        log.info("Privileges for \"" + username + "\" on schema " + schemaName + ": " + privileges);
//
//        log.info("Dropping test table");
//        jdbcTemplate.execute(SqlQueryFactory.generateDropTestTableSql(schemaName));
//
//        return String.join(",", privileges);
//    }

//    public void createSchemaUserAndSetPrivileges(String databaseName, String schemaName, String username, String password, List<String> privileges) throws DataAccessException {
//        ensureSchema(databaseName, schemaName);
//        ensureDatabaseUser(databaseName, username, password);
//        for (String privilege : privileges) {
//            if (Arrays.stream(Privilege.class.getEnumConstants()).anyMatch(e -> e.name().equals(privilege.toUpperCase().trim()))) {
//                grantPrivilegeToUser(databaseName, schemaName, username, privilege);
//            }
//        }
//    }
}
