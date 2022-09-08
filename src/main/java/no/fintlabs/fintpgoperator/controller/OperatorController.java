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

    @PostMapping("/api/createSchema")
    public String createSchema(String schemaName) {
        return dataAccessService.createSchema(schemaName);
    }

    @PostMapping("/api/createDbUser")
    public String createDbUser(String username, String password) {
        return dataAccessService.createDbUser(username, password);
    }

    @PostMapping("/api/grantPrivilegeToRole")
    public String grantPrivilegeToRole(String schemaName, String roleName, String privilege) {
        return dataAccessService.grantPrivilegeToRole(schemaName, roleName, privilege);
    }

    @PostMapping("/api/createRole")
    public String createRole(String roleName, String password) {
        return dataAccessService.createRole(roleName, password);
    }
    @PostMapping("/api/checkGrants")
    public String checkGrants(String schemaName, String roleName) {
        return dataAccessService.checkRolePrivilegesOnSchema(schemaName, roleName);
    }
    @PostMapping("/api/createSchemaUserAndRole")
    public String createSchemaUserAndRole(String schemaName, String username, String password, String roleName) {
        return dataAccessService.createSchemaUserAndRole(schemaName, username, password, roleName);
    }
}
