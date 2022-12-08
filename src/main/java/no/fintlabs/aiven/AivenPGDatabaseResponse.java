package no.fintlabs.aiven;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AivenPGDatabaseResponse {

    private List<AivenPGDatabase> databases = new ArrayList<>();
}
