package no.fintlabs.aiven;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.operator.NameFactory;
import no.fintlabs.operator.PGUser;
import no.fintlabs.operator.PGUserCRD;
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

    public void createUserForService(PGUser desired) throws FailedToCreateAivenObjectException {

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


            desired.setPassword(Objects.requireNonNull(aivenServiceUser).getAivenPGServiceUser().getPassword());
        } catch (WebClientResponseException e) {
            log.debug("Creating Aiven service user returned error code {} and body {}", e.getStatusText(), e.getResponseBodyAsString());
            throw new FailedToCreateAivenObjectException();
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


    public Set<PGUser> getPgUser(PGUserCRD primary) {

        String username = NameFactory.createUsername(primary);
        Optional<AivenServiceUser> serviceUser = getServiceUser(username);

        if (serviceUser.isPresent()) {
            log.debug("Found service user " + username);

            return Collections.singleton(PGUser
                    .builder()
                    .database(primary.getSpec().getDatabase())
                    .username(serviceUser.get().getAivenPGServiceUser().getUsername())
                    .password(serviceUser.get().getAivenPGServiceUser().getPassword())
                    .build()
            );
        }

        log.debug("Did not find service user: " + username);
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
}
