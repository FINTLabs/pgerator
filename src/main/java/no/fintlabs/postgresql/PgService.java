package no.fintlabs.postgresql;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.OperatorProperties;
import no.fintlabs.aiven.AivenServiceUser;
import no.fintlabs.aiven.AivenService;
import no.fintlabs.operator.PGSchemaAndUser;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component
public class PgService {

    private String currentDatabase;


    private final PgProperties pgProperties;
    private final OperatorProperties operatorProperties;
    private final JdbcTemplate jdbcTemplate;

    private final AivenService aivenService;

    public PgService(JdbcTemplate jdbcTemplate, PgProperties pgProperties, OperatorProperties operatorProperties, AivenService aivenService) {
        this.jdbcTemplate = jdbcTemplate;
        this.pgProperties = pgProperties;
        this.operatorProperties = operatorProperties;
        this.aivenService = aivenService;
        try {
            currentDatabase = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection().getCatalog();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean databaseExists(String dbName) throws DataAccessException {
        log.debug("Check if database '{}' exists", dbName);
        List<String> results = jdbcTemplate.query(SqlFactory.databaseExistsSql(dbName), (rs, rowNum) -> rs.getString("datname"));
        return results.size() > 0;
    }

    private boolean userExists(PGSchemaAndUser desired) throws DataAccessException {
        useDatabase(desired.getDatabase());
        List<String> results = jdbcTemplate.query(SqlFactory.userExistsSql(desired.getUsername()), (rs, rowNum) -> rs.getString("usename"));
        return results.size() > 0;
    }

    public Set<PGSchemaAndUser> getSchemaAndUser(String dbName, String schemaName, String username) {
        useDatabase(dbName);
        //List<String> userResults = jdbcTemplate.query(SqlFactory.generateUserExistsSql(username), (rs, rowNum) -> rs.getString("usename"));
        Optional<AivenServiceUser> serviceUser = aivenService.getServiceUser(username);
        List<String> schemaResults = jdbcTemplate.query(SqlFactory.schemaExistsSql(schemaName), (rs, rowNum) -> rs.getString("schema_name"));
        if (serviceUser.isPresent() && schemaResults.size() > 0) {
            return Set.of(
                    PGSchemaAndUser
                            .builder()
                            .database(dbName)
                            .username(serviceUser.get().getAivenPGServiceUser().getUsername())
                            .password(serviceUser.get().getAivenPGServiceUser().getPassword())
                            .schemaName(schemaResults.get(0))
                            .build());
        }
        return Collections.emptySet();
    }

    private void createDb(String dbName) throws DataAccessException {
        jdbcTemplate.execute(SqlFactory.createDatabaseSql(dbName));
        log.info("Database {} created!", dbName);
    }

    public void makeSchemaOrphanOrDelete(PGSchemaAndUser pgSchemaAndUser) throws DataAccessException {
        useDatabase(pgSchemaAndUser.getDatabase());

        if (schemaHasTables(pgSchemaAndUser)) {
            log.debug("Schema {} has tables. Make it orphan", pgSchemaAndUser.getSchemaName());
            jdbcTemplate.execute(SqlFactory.schemaRenameSql(pgSchemaAndUser.getSchemaName(), SchemaNameFactory.orphanSchemaNameFromName(pgSchemaAndUser.getSchemaName())));
        } else {
            log.debug("Schema {} does not have tables. Removing it.", pgSchemaAndUser.getSchemaName());
            jdbcTemplate.execute(SqlFactory.deleteSchemaSql(pgSchemaAndUser.getSchemaName()));
        }
    }

    private boolean schemaHasTables(PGSchemaAndUser pgSchemaAndUser) {
        return jdbcTemplate.query(SqlFactory.schemaHasTablesSql(pgSchemaAndUser.getSchemaName()), (rs, rowNum) -> rs.getBoolean("exists")).get(0);
    }

    public void deleteUser(PGSchemaAndUser pgSchemaAndUser) throws DataAccessException {
        useDatabase(pgSchemaAndUser.getDatabase());
        jdbcTemplate.execute(SqlFactory.revokeDefaultPrivilegesSql(pgSchemaAndUser.getUsername(), pgSchemaAndUser.getSchemaName()));
        jdbcTemplate.execute(SqlFactory.revokeAllPriviligesSql(pgSchemaAndUser.getUsername(), pgSchemaAndUser.getSchemaName()));
        //jdbcTemplate.execute(SqlFactory.generateDropUserSql(pgSchemaAndUser.getUsername()));
        aivenService.deleteConnectionPool(pgSchemaAndUser);
        aivenService.deleteUserForService(pgSchemaAndUser.getUsername());
        log.info("User '{}' deleted", pgSchemaAndUser.getUsername());
    }

    private void useDatabase(String database) {
        ensureDatabase(database);
        if (!currentDatabase.equalsIgnoreCase(database)) {
            try {
                DataSourceUtils.doCloseConnection(DataSourceUtils.getConnection(Objects.requireNonNull(jdbcTemplate.getDataSource())), jdbcTemplate.getDataSource());
                DataSource dataSource = DataSourceBuilder.create()
                        .url(operatorProperties.getBaseUrl()
                                + database.toLowerCase()
                                + "?sslmode=require")
                        .username(pgProperties.getUsername())
                        .password(pgProperties.getPassword())
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
            log.debug("Database '{}' does not exist. Proceeding to create...", database);
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


        log.info("Schema '{}' ensured", desired.getSchemaName());
    }

    private void createSchemaIfNotExits(PGSchemaAndUser pgSchemaAndUser) {
        useDatabase(pgSchemaAndUser.getDatabase());
        jdbcTemplate.execute(SqlFactory.schemaCreateIfNotExistSql(pgSchemaAndUser.getSchemaName()));
        log.debug("Created schema with name '{}' ", pgSchemaAndUser.getSchemaName());
    }

    private void renameSchema(PGSchemaAndUser pgSchemaAndUser, String oldName) {
        useDatabase(pgSchemaAndUser.getDatabase());
        jdbcTemplate.execute(SqlFactory.schemaRenameSql(oldName, pgSchemaAndUser.getSchemaName()));
        log.debug("Renamed schema '{}' to '{}'", pgSchemaAndUser.getSchemaName(), oldName);
    }

    private Optional<String> hasOrphanSchema(PGSchemaAndUser pgSchemaAndUser) {
        useDatabase(pgSchemaAndUser.getDatabase());
        List<String> results = jdbcTemplate.query(SqlFactory.schemaExistsSql(pgSchemaAndUser.getSchemaName() + "_orphan_%"), (rs, rowNum) -> rs.getString("schema_name"));

        if (results.size() == 1) {
            log.debug("Schema '{}' is orphan.", pgSchemaAndUser.getSchemaName());
            return Optional.of(results.get(0));
        }

        return Optional.empty();

    }

    public void ensureUser(PGSchemaAndUser desired) throws DataAccessException {
        useDatabase(desired.getDatabase());
//        if (!userExists(desired)) {
//            jdbcTemplate.execute(SqlFactory.userCreateSql(desired.getUsername(), desired.getPassword()));
//            log.debug("User " + desired.getUsername() + " created");
//        } else {
//            log.debug("User {} already exists", desired.getUsername());
//        }
        aivenService.createUserForService(desired);
        aivenService.createConnectionPool(desired);
        grantPrivilegeToUser(desired);
    }

//    public String resetUserPassword(String username) {
//        String password = RandomStringUtils.randomAlphanumeric(32);
//        jdbcTemplate.execute(SqlFactory.resetUserPasswordSql(username, password));
//        log.debug("Reset password for user {}", username);
//
//        return password;
//    }


    private void grantPrivilegeToUser(PGSchemaAndUser pgSchemaAndUser) throws DataAccessException {
        useDatabase(pgSchemaAndUser.getDatabase());
        jdbcTemplate.execute(SqlFactory.grantPrivilegeOnSchemaSql(pgSchemaAndUser.getSchemaName(), "all", pgSchemaAndUser.getUsername()));
        jdbcTemplate.execute(SqlFactory.grantPrivilegeOnAllTablesInSchemaSql(pgSchemaAndUser.getSchemaName(), "all", pgSchemaAndUser.getUsername()));
        jdbcTemplate.execute(SqlFactory.grantDefaultPrivilegesSql(pgSchemaAndUser.getSchemaName(), "all", pgSchemaAndUser.getUsername()));
        log.info("Privileges granted to '{}' on schema '{}'", pgSchemaAndUser.getUsername(), pgSchemaAndUser.getSchemaName());
    }
}
