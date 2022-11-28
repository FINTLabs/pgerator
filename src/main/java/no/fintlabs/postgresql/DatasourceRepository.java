package no.fintlabs.postgresql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.OperatorProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DatasourceRepository {

    private final Map<String, HikariDataSource> dataSources;
    private final HikariConfig hikariConfig;
    private final OperatorProperties operatorProperties;
    private final PgProperties pgProperties;

    public DatasourceRepository(HikariDataSource hikariDataSource, HikariConfig hikariConfig, OperatorProperties operatorProperties, PgProperties pgProperties) {
        this.hikariConfig = hikariConfig;
        this.operatorProperties = operatorProperties;
        this.pgProperties = pgProperties;
        dataSources = new HashMap<>();

        dataSources.put(hikariDataSource.getCatalog() + "-pool", hikariDataSource);
    }

    public HikariDataSource createDatasource(String database) {
        log.debug("Creating data source for database '{}'", database);

        HikariDataSource dataSource = (HikariDataSource) DataSourceBuilder.create()
                .url(operatorProperties.getBaseUrl()
                        + database.toLowerCase()
                        + "?sslmode=require")
                .username(pgProperties.getUsername())
                .password(pgProperties.getPassword())
                .build();

        dataSource.setMaximumPoolSize(hikariConfig.getMaximumPoolSize());
        dataSource.setMinimumIdle(hikariConfig.getMinimumIdle());
        dataSource.setPoolName(database + "-pool");
        dataSources.put(dataSource.getPoolName(), dataSource);

        return dataSource;
    }

    public HikariDataSource getOrNew(String database) {
        log.debug("Got {} data sources in data source repository", dataSources.size());
        log.debug("Getting data source for database '{}'", database);
        return dataSources.getOrDefault(database + "pool", createDatasource(database));
    }
}
