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

    @GetMapping("/api/allEntries")
    public String getAllEntries() {
        return dataAccessService.getAllEntries().toString();
    }

    @PostMapping("/api/createSchema")
    public String createSchema(String schemaName) {
        return dataAccessService.createSchema(schemaName);
    }

    @PostMapping("/api/createDbUser")
    public String createDbUser(String userName, String password) {
        return dataAccessService.createDbUser(userName, password);
    }
}
