package no.fintlabs.operator.pg.controller;

import no.fintlabs.operator.pg.model.*;
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
    public ResponseEntity<Void> createSchema(@PathVariable String databaseName, @RequestBody SchemaCreateRequest schemaCreateRequest) {
        dataAccessService.createSchema(schemaCreateRequest.getSchemaName());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/api/db/{databaseName}/user")
    public ResponseEntity<Void> createDbUser(@PathVariable String databaseName, @RequestBody UserCreateRequest userCreateRequest) {
        dataAccessService.createDbUser(userCreateRequest.getUsername(), userCreateRequest.getPassword());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/api/db/{databaseName}/schema/{schemaName}/user/{username}/privilege")
    public ResponseEntity<Void> grantPrivilegeToUser(@PathVariable String databaseName, @PathVariable String schemaName, @PathVariable String username, @RequestBody PrivilegeRequest privilegeRequest) {
        dataAccessService.grantPrivilegeToUser(schemaName, username, privilegeRequest.getPrivilege());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ResponseBody
    @GetMapping("/api/db/{databaseName}/schema/{schemaName}/user/{username}/privileges")
    public String getUserPrivilegesOnSchema(@PathVariable String databaseName, @PathVariable String schemaName, @PathVariable String username) {
        return dataAccessService.getUserPrivilegesOnSchema(schemaName, username);
    }

    @PostMapping("/api/db/{databaseName}/schema/{schemaName}/user/{username}/privileges")
    public ResponseEntity<Void> createSchemaUserAndSetPrivileges(@PathVariable String databaseName, @RequestBody SchemaUserCreateRequest request) {
        dataAccessService.createSchemaUserAndSetPrivileges(request.getSchemaName(), request.getUsername(), request.getPassword(), request.getPrivileges());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}
