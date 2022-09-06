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
        String sql = "CREATE SCHEMA " + schemaName;
        try {
            jdbcTemplate.execute(sql);
            logger.log(Level.INFO, "Schema created");
            return "Schema created";
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in createSchema: " + e.getMessage());
        }
        return null;
    }
}
