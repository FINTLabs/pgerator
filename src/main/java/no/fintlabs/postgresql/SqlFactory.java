package no.fintlabs.postgresql;

import org.springframework.stereotype.Service;

@Service
public class SqlFactory {

    public static String generateDatabaseExistsSql(String databaseName) {
        return "SELECT datname FROM pg_catalog.pg_database WHERE lower(datname) = '" + databaseName.toLowerCase() + "'";
    }

    public static String schemaExistsSql(String schemaName) {
        return "SELECT schema_name FROM information_schema.schemata WHERE lower(schema_name) like '" + schemaName.toLowerCase() + "'";
    }

    public static String schemaHasTablesSql(String schema) {
        return String.format("SELECT EXISTS (" +
                        "    SELECT FROM" +
                        "        pg_tables" +
                        "    WHERE" +
                        "        schemaname = '%s'" +
                        "    );",
                schema);
    }

    public static String generateUserExistsSql(String username) {
        return "SELECT usename FROM pg_catalog.pg_user WHERE lower(usename) = '" + username.toLowerCase() + "'";
    }

    public static String generateCreateDatabaseSql(String dbName) {
        return "CREATE DATABASE " + dbName;
    }

    public static String revokeDefaultPrivilegesSql(String username, String schema) {
        return "ALTER DEFAULT PRIVILEGES IN SCHEMA \"" + schema + "\" REVOKE ALL ON TABLES FROM \"" + username + "\";";
    }

    public static String revokeAllPriviligesSql(String username, String schema) {
        return "REVOKE ALL ON ALL TABLES IN SCHEMA \"" + schema + "\" FROM \"" + username + "\";";
    }

    public static String generateDropUserSql(String username) {
        return "DROP USER \"" + username + "\";";
    }

    public static String schemaCreateIfNotExistSql(String schemaName) {
        return "CREATE SCHEMA IF NOT EXISTS \"" + schemaName + "\";";
    }

    public static String schemaRenameSql(String oldName, String newName) {
        return "ALTER SCHEMA \"" + oldName + "\" RENAME TO \"" + newName + "\";";
    }

    public static String userCreateSql(String username, String password) {
        return "CREATE USER \"" + username + "\" WITH PASSWORD '" + password + "'";
    }

    public static String resetUserPasswordSql(String username, String password) {
        return "ALTER USER \"" + username + "\" WITH PASSWORD '" + password + "';";
    }

    public static String generateGrantPrivilegeSql(String schemaName, String privilege, String username) {
        return "GRANT " + privilege + " ON ALL TABLES IN SCHEMA \"" + schemaName + "\" TO \"" + username + "\"";
    }

    public static String generateGrantDefaultPrivilegesSql(String schemaName, String privilege, String username) {
        return "ALTER DEFAULT PRIVILEGES IN SCHEMA \"" + schemaName + "\" GRANT " + privilege + " ON TABLES TO \"" + username + "\"";
    }

    public static String deleteSchemaSql(String schemaName) {
        return String.format("DROP SCHEMA \"%s\"", schemaName);
    }
}
