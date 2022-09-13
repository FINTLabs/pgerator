package no.fintlabs.operator.pg.controller;

import no.fintlabs.operator.pg.model.DatabaseCreateRequest;
import no.fintlabs.operator.pg.model.SchemaCreateRequest;
import no.fintlabs.operator.pg.service.PostgreSqlDataAccessService;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/")
@RestController
public class OperatorController {

    private final PostgreSqlDataAccessService dataAccessService;

    public OperatorController(PostgreSqlDataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
    }

    @GetMapping
    public String index() {
        return "init spring boot";
    }

    @PostMapping("/api/db/")
    public ResponseEntity<Void> createDb(@RequestBody DatabaseCreateRequest databaseCreateRequest) {
         dataAccessService.createDb(databaseCreateRequest.getDatabaseName());

         return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/api/db/{databaseName}/schema")
    public String createSchema(@PathVariable String databaseName, @RequestBody SchemaCreateRequest schemaCreateRequest) {
        return dataAccessService.createSchema(schemaCreateRequest.getSchemaName());
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

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}
