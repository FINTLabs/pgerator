package no.fintlabs.operator;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PGDatabaseAndUser {
    private String database;
    private String username;
    private String password;
}
