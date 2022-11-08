package no.fintlabs.model;

import lombok.Data;

import java.util.List;

@Data
public class SchemaUserCreateRequest {
    private String password;
    private List<String> privileges;
}
