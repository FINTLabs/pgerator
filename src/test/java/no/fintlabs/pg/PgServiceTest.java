package no.fintlabs.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PgServiceTest {
    String currentDatabase = "currentDB";
    String databaseName = "testDb";
    String username = "testuser";

    Environment environment = mock(Environment.class);


    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);

        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getURL()).thenReturn(currentDatabase);

        when(dataSource.getConnection()).thenReturn(connection);
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
    }

    @Test
    public void testEnsureUsageAndCreateOnSchema() throws DataAccessException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        PgService pgService = new PgService(jdbcTemplate, environment);

        Field field = PgService.class.getDeclaredField("currentDatabase");
        field.setAccessible(true);
        field.set(pgService, currentDatabase);

        Method privateMethod = PgService.class.getDeclaredMethod("changeDatabase", String.class);
        privateMethod.setAccessible(true);
        privateMethod.invoke(pgService, currentDatabase);

        when(environment.getProperty("fint.data.pool-base-url")).thenReturn("jdbc:mysql://localhost:3306/mockdb");

        doNothing().when(jdbcTemplate).execute(anyString());

        pgService.ensureUsageAndCreateOnSchema(databaseName, username);

        verify(jdbcTemplate, times(4)).execute(anyString());
    }
}
