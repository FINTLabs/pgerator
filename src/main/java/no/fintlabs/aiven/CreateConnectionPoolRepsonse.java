
package no.fintlabs.aiven;

import lombok.Data;

import java.util.List;

@Data
public class CreateConnectionPoolRepsonse {

    private List<Error> errors;
    private String message;

    @Data
    public static class Error {

        private String message;
        private Long status;
    }
}
