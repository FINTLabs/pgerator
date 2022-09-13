package no.fintlabs.operator.pg.model;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String username;
    private String password;
}
