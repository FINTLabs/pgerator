package no.fintlabs.fintpgoperator.controller;

import no.fintlabs.fintpgoperator.Operator;
import no.fintlabs.fintpgoperator.service.PostgreSqlDataAccessService;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OperatorController {

    private PostgreSqlDataAccessService dataAccessService;

    public OperatorController(PostgreSqlDataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
    }

    @GetMapping("/")
    public String index(){
        return "init spring boot";
    }

    @GetMapping("/api/allEntries")
    public String getAllEntries(){
        return dataAccessService.getAllEntries().toString();
    }
}
