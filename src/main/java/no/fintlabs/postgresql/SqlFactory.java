package no.fintlabs.postgresql;

import org.springframework.stereotype.Service;

@Service
public class SqlFactory {

    public static String databaseExistsSql(String databaseName) {
        return String.format(
                "SELECT datname FROM pg_catalog.pg_database WHERE lower(datname) = '%s'",
                databaseName.toLowerCase()
        );
    }

    public static String schemaExistsSql(String schemaName) {
        return String.format(
                "SELECT schema_name FROM information_schema.schemata WHERE lower(schema_name) like '%s';",
                schemaName.toLowerCase()
        );
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

    public static String createDatabaseSql(String dbName) {

        return String.format("CREATE DATABASE %s;", dbName);
    }

    public static String revokeDefaultPrivilegesSql(String username, String schema) {
        return String.format(
                "ALTER DEFAULT PRIVILEGES IN SCHEMA \"%s\" REVOKE ALL ON TABLES FROM \"%s\";",
                schema,
                username
        );
    }

    public static String revokeAllPriviligesSql(String username, String schema) {
        return String.format(
                "REVOKE ALL ON ALL TABLES IN SCHEMA \"%s\" FROM \"%s\";",
                schema,
                username
        );
    }

    public static String schemaCreateIfNotExistSql(String schemaName) {
        return String.format(
                "CREATE SCHEMA IF NOT EXISTS \"%s\";",
                schemaName
        );
    }

    public static String schemaRenameSql(String oldName, String newName) {
        return String.format(
                "ALTER SCHEMA \"%s\" RENAME TO \"%s\";",
                oldName,
                newName
        );
    }

    public static String grantPrivilegeOnSchemaSql(String schemaName, String privilege, String username) {
        return String.format(
                "GRANT %s ON SCHEMA \"%s\" TO \"%s\";",
                privilege,
                schemaName,
                username
        );
    }

    public static String grantPrivilegeOnAllTablesInSchemaSql(String schemaName, String privilege, String username) {
        return String.format(
                "GRANT %s ON ALL TABLES IN SCHEMA \"%s\" TO \"%s\";",
                privilege,
                schemaName,
                username
        );
    }

    public static String grantDefaultPrivilegesSql(String schemaName, String privilege, String username) {
        return String.format(
                "ALTER DEFAULT PRIVILEGES IN SCHEMA \"%s\" GRANT %s ON TABLES TO \"%s\";",
                schemaName,
                privilege,
                username
        );
    }

    public static String deleteSchemaSql(String schemaName) {
        return String.format("DROP SCHEMA \"%s\";", schemaName);
    }
}
