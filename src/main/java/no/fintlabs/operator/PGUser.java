package no.fintlabs.operator;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PGUser {
    private String database;
    private String username;
    private String password;
}
