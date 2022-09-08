package no.fintlabs.fintpgoperator.controller;

import no.fintlabs.fintpgoperator.service.PostgreSqlDataAccessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class OperatorController {

    private PostgreSqlDataAccessService dataAccessService;

    public OperatorController(PostgreSqlDataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
    }

    @GetMapping("/")
    public String index() {
        return "init spring boot";
    }

    @PostMapping("/api/createDb")
    public String createDb(String dbName) {
        return dataAccessService.createPostgresDb(dbName);
    }

    @PostMapping("/api/createSchema")
    public String createSchema(String schemaName) {
        return dataAccessService.createSchema(schemaName);
    }

    @PostMapping("/api/createDbUser")
    public String createDbUser(String username, String password) {
        return dataAccessService.createDbUser(username, password);
    }

    @PostMapping("/api/grantPrivilegeToUser")
    public String grantPrivilegeToUser(String schemaName, String username, String privilege) {
        return dataAccessService.grantPrivilegeToUser(schemaName, username, privilege);
    }

    @PostMapping("/api/checkGrants")
    public String checkGrants(String schemaName, String username) {
        return dataAccessService.checkUserPrivilegesOnSchema(schemaName, username);
    }
    @PostMapping("/api/createSchemaUserAndRole")
    public String createSchemaUserAndSetPrivileges(String schemaName, String username, String password, String privileges) {
        return dataAccessService.createSchemaUserAndSetPrivileges(schemaName, username, password, privileges);
    }
}
