package no.fintlabs.aiven;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.operator.PGSchemaAndUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Component
public class AivenService {

    private final WebClient webClient;
    private final AivenProperties aivenProperties;


    public AivenService(WebClient webClient, AivenProperties aivenProperties) {
        this.webClient = webClient;
        this.aivenProperties = aivenProperties;
    }

    public void createUserForService(PGSchemaAndUser desired) {
        log.debug("Creating user {} for service {}", desired.getUsername(), aivenProperties.getService());

        AivenServiceUser aivenServiceUser = Optional.ofNullable(webClient.post()
                        .uri("/project/{project_name}/service/{service_name}/user", aivenProperties.getProject(), aivenProperties.getService())
                        .body(BodyInserters.fromValue(Collections.singletonMap("username", desired.getUsername())))
                        .retrieve()
                        .bodyToMono(AivenServiceUser.class)
                        .block())
                .orElseThrow();

        log.debug("Created Aiven PG service user");

        desired.setPassword(aivenServiceUser.getAivenPGServiceUser().getPassword());

    }

    public void deleteUserForService(String username) {
        log.debug("Deleting user {} from service {}", username, aivenProperties.getService());

        webClient
                .delete()
                .uri("/project/{project_name}/service/{service_name}/user/{username}", aivenProperties.getProject(), aivenProperties.getService(), username)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public Optional<AivenServiceUser> getServiceUser(String username) {
        try {
            // TODO: 22/11/2022 Create separate models for GET
            AivenServiceUser createKafkaUserResponse = webClient.get()
                    .uri("/project/{project_name}/service/{service_name}/user/{username}", aivenProperties.getProject(), aivenProperties.getService(), username)
                    .retrieve()
                    .bodyToMono(AivenServiceUser.class)
                    .block();

            return Optional.ofNullable(createKafkaUserResponse);


        } catch (WebClientResponseException e) {
            log.debug("Could not find  user {}. Proceeding to create...", username);
            return Optional.empty();
        }
    }

    public void createConnectionPool(PGSchemaAndUser pgSchemaAndUser) {
        log.debug("Creating connection pool for user {} ", pgSchemaAndUser.getUsername());

        String block = webClient
                .post()
                .uri("/project/{project_name}/service/{service_name}/connection_pool", aivenProperties.getProject(), aivenProperties.getService())
                .body(BodyInserters.fromValue(ConnectionPool
                        .builder()
                        .username(pgSchemaAndUser.getUsername())
                        .database(pgSchemaAndUser.getDatabase())
                        .poolName(pgSchemaAndUser.getUsername())
                        .build()))
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> clientResponse.bodyToMono(String.class).map(Exception::new))
                .bodyToMono(String.class)
                .block();
        log.debug("Creating connection pool response: {}", block);
    }

    public void deleteConnectionPool(PGSchemaAndUser pgSchemaAndUser) {
        log.debug("Removing connection pool for user {} ", pgSchemaAndUser.getUsername());

        webClient
                .delete()
                .uri("/project/{project_name}/service/{service_name}/connection_pool/{pool_name}", aivenProperties.getProject(), aivenProperties.getService(), pgSchemaAndUser.getUsername())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

}