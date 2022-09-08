package no.fintlabs.fintpgoperator.service;

public interface DataAccessService {
    String createDb(String dbName);
    String createSchema(String schemaName);
    String createDbUser(String username, String password);
    String grantPrivilegeToUser(String schemaName, String username, String privilege);
    String checkUserPrivilegesOnSchema(String schemaName, String username);
    String createSchemaUserAndSetPrivileges(String schemaName, String username, String password, String privileges);
}
