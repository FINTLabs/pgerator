package no.fintlabs.aiven;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.operator.PGDatabaseAndUser;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class AivenService {

    private final WebClient webClient;
    private final AivenProperties aivenProperties;


    public AivenService(WebClient webClient, AivenProperties aivenProperties) {
        this.webClient = webClient;
        this.aivenProperties = aivenProperties;
    }

    public void createDatabaseForService(PGDatabaseAndUser desired) {

        try {
            webClient.post()
                    .uri("/project/{project_name}/service/{service_name}/db", aivenProperties.getProject(), aivenProperties.getService())
                    .body(BodyInserters.fromValue(Collections.singletonMap("database", desired.getDatabase())))
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus.value() == 409, clientResponse -> Mono.empty())
                    .bodyToMono(AivenServiceUser.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.debug("Creating Aiven service db returned error code {} and body {}", e.getStatusText(), e.getResponseBodyAsString());
        }
    }

    public Optional<AivenPGDatabase> getDatabase(String database) {

        try {
            return Objects.requireNonNull(webClient.get()
                            .uri("/project/{project_name}/service/{service_name}/db", aivenProperties.getProject(), aivenProperties.getService())
                            .retrieve()
                            .bodyToMono(AivenPGDatabaseResponse.class)
                            .block())
                    .getDatabases()
                    .stream()
                    .filter(aivenPGDatabase -> aivenPGDatabase.getDatabaseName().equals(database))
                    .findFirst();
        } catch (WebClientResponseException e) {
            log.debug("Could not find  database '{}'.", database);
            return Optional.empty();
        }
    }

    public void createUserForService(PGDatabaseAndUser desired) {

        try {
            AivenServiceUser aivenServiceUser = getServiceUser(desired.getUsername()).orElseGet(() -> {
                log.debug("Creating Aiven PG service user");
                return webClient.post()
                        .uri("/project/{project_name}/service/{service_name}/user", aivenProperties.getProject(), aivenProperties.getService())
                        .body(BodyInserters.fromValue(Collections.singletonMap("username", desired.getUsername())))
                        .retrieve()
                        .onStatus(httpStatus -> httpStatus.value() == 409, clientResponse -> Mono.empty())
                        .bodyToMono(AivenServiceUser.class)
                        .block();
            });


            desired.setPassword(aivenServiceUser.getAivenPGServiceUser().getPassword());
        } catch (WebClientResponseException e) {
            log.debug("Creating Aiven service user returned error code {} and body {}", e.getStatusText(), e.getResponseBodyAsString());
        }
    }

    public void deleteUserForService(String username) {
        log.debug("Deleting user '{}' from service '{}'", username, aivenProperties.getService());

        webClient
                .delete()
                .uri("/project/{project_name}/service/{service_name}/user/{username}", aivenProperties.getProject(), aivenProperties.getService(), username)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }


    public Set<PGDatabaseAndUser> getUserAndDatabase(Optional<String> databaseAndUsername) {

        if (databaseAndUsername.isPresent()) {

            Optional<AivenServiceUser> serviceUser = getServiceUser(databaseAndUsername.get());
            Optional<AivenPGDatabase> database = getDatabase(databaseAndUsername.get());

            if (serviceUser.isPresent() && database.isPresent()) {
                return Collections.singleton(PGDatabaseAndUser
                        .builder()
                        .database(database.get().getDatabaseName())
                        .username(serviceUser.get().getAivenPGServiceUser().getUsername())
                        .password(serviceUser.get().getAivenPGServiceUser().getPassword())
                        .build()
                );
            }
        }

        return Collections.emptySet();
    }

    public Optional<AivenServiceUser> getServiceUser(String username) {
        try {
            AivenServiceUser createKafkaUserResponse = webClient.get()
                    .uri("/project/{project_name}/service/{service_name}/user/{username}", aivenProperties.getProject(), aivenProperties.getService(), username)
                    .retrieve()
                    .bodyToMono(AivenServiceUser.class)
                    .block();

            return Optional.ofNullable(createKafkaUserResponse);


        } catch (WebClientResponseException e) {
            log.debug("Could not find  user '{}'. Proceeding to create...", username);
            return Optional.empty();
        }
    }

    public void createConnectionPool(PGDatabaseAndUser pgDatabaseAndUser) {
        log.debug("Creating connection pool for user '{}' ", pgDatabaseAndUser.getUsername());

        try {
            CreateConnectionPoolRepsonse block = webClient
                    .post()
                    .uri("/project/{project_name}/service/{service_name}/connection_pool", aivenProperties.getProject(), aivenProperties.getService())
                    .body(BodyInserters.fromValue(ConnectionPool
                            .builder()
                            .username(pgDatabaseAndUser.getUsername())
                            .database(pgDatabaseAndUser.getDatabase())
                            .poolName(pgDatabaseAndUser.getUsername())
                            .build()))
                    .retrieve()
                    // TODO: 28/11/2022 Needs improvement
                    .onStatus(httpStatus -> httpStatus.value() == 409, clientResponse -> Mono.empty())
                    .bodyToMono(CreateConnectionPoolRepsonse.class)
                    .block();
            log.debug("Creating connection pool response: {}", block.getMessage());
        } catch (WebClientResponseException e) {
            log.debug("Creating connection pool returned error code {} and body {}", e.getStatusCode(), e.getResponseBodyAsString());
        }
    }

    public void deleteConnectionPool(PGDatabaseAndUser pgDatabaseAndUser) {
        log.debug("Removing connection pool for user '{}' ", pgDatabaseAndUser.getUsername());

        webClient
                .delete()
                .uri("/project/{project_name}/service/{service_name}/connection_pool/{pool_name}", aivenProperties.getProject(), aivenProperties.getService(), pgDatabaseAndUser.getUsername())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }


}
