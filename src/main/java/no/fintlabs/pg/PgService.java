package no.fintlabs.pg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Objects;

@Slf4j
@Component
public class PgService {

    private String currentDatabase;
    private final Environment environment;
    private final JdbcTemplate jdbcTemplate;

    public PgService(JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        try {
            currentDatabase = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection().getCatalog();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.environment = environment;
    }

    public void deleteSchema(String dbName, String username) throws DataAccessException {
        changeDatabase(dbName);
        jdbcTemplate.execute(String.format("DROP SCHEMA %s CASCADE", username));
        log.info("Schema deleted: " + username);
    }

    private void changeDatabase(String databaseName) {
        if (!currentDatabase.equalsIgnoreCase(databaseName)) {
            DataSource dataSource = DataSourceBuilder.create()
                    .url(environment.getProperty("fint.data.pool-base-url") + databaseName.toLowerCase() + "?sslmode=require&prepareThreshold=0")
                    .username(environment.getProperty("spring.datasource.username"))
                    .password(environment.getProperty("spring.datasource.password"))
                    .build();
            jdbcTemplate.setDataSource(dataSource);
            currentDatabase = databaseName;
            log.info("Database changed to: " + databaseName);
        }
    }

    public void createSchema(String databaseName, String username) throws DataAccessException {
        changeDatabase(databaseName);
        jdbcTemplate.execute(String.format("CREATE SCHEMA IF NOT EXISTS %s", username));
        log.info("Schema created: " + username);
    }


    public void grantUsageAndCreateOnSchema(String databaseName, String username) throws DataAccessException {
        changeDatabase(databaseName);

        jdbcTemplate.execute(String.format("grant usage on schema %s to %s", username, username));
        jdbcTemplate.execute(String.format("grant create on schema %s to %s", username, username));
        log.info("Usage and create granted on schema {} for user {}", username, username);
    }
}