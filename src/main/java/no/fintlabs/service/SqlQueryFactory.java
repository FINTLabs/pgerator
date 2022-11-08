package no.fintlabs.service;

import org.springframework.stereotype.Service;

@Service
public class SqlQueryFactory {

    public static String generateDatabaseExistsSql(String databaseName) {
        return "SELECT datname FROM pg_catalog.pg_database WHERE lower(datname) = '" + databaseName.toLowerCase() + "'";
    }

    public static String generateSchemaExistsSql(String schemaName) {
        return "SELECT schema_name FROM information_schema.schemata WHERE lower(schema_name) = '" + schemaName.toLowerCase() + "'";
    }

    public static String generateUserExistsSql(String username) {
        return "SELECT usename FROM pg_catalog.pg_user WHERE lower(usename) = '" + username.toLowerCase() + "'";
    }

    public static String generateCreateDatabaseSql(String dbName) {
            return "CREATE DATABASE " + dbName;
    }

    public static String generateDeleteSchemaSql(String schemaName) {
        return "DROP SCHEMA " + schemaName + " CASCADE";
    }

    public static String generateReassignOwnedFromUserToUserSql(String username, String ReassignToUsername) {
        return "REASSIGN OWNED BY \"" + username + "\" TO \"" + ReassignToUsername + "\";";
    }

    public static String generateDropOwnedByUserSql(String username) {
        return "DROP OWNED BY \"" + username + "\";";
    }

    public static String generateDropUserSql(String username) {
        return "DROP USER \"" + username + "\";";
    }

    public static String generateCreateSchemaSql(String schemaName) {
        return "CREATE SCHEMA IF NOT EXISTS " + schemaName;
    }

    public static String generateCreateDatabaseUserSql(String username, String password) {
        return "CREATE USER \"" + username + "\" WITH PASSWORD '" + password + "'";
    }

    public static String generateGrantPrivilegeSql(String schemaName, String privilege, String username) {
        return "GRANT " + privilege + " ON ALL TABLES IN SCHEMA " + schemaName + " TO \"" + username + "\"";
    }

    public static String generateGrantDefaultPrivilegesSql(String schemaName, String privilege, String username) {
        return "ALTER DEFAULT PRIVILEGES IN SCHEMA " + schemaName + " GRANT " + privilege + " ON TABLES TO \"" + username + "\"";
    }

    public static String generateCreateTestTable(String schemaName) {
        return "CREATE TABLE " + schemaName + ".testtable (id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL)";
    }

    public static String generateGetPrivileges(String schemaName, String username) {
        return "SELECT grantee, table_schema, privilege_type FROM information_schema.role_table_grants WHERE grantee = '" + username + "' AND table_schema = '" + schemaName + "'";
    }

    public static String generateDropTestTableSql(String schemaName) {
        return "DROP TABLE " + schemaName + ".testtable";
    }
}
