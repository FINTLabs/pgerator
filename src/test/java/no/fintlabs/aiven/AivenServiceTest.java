package no.fintlabs.aiven;

import no.fintlabs.operator.PGUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AivenServiceTest {
    @Mock
    private WebClient webClient;

    @Mock
    private RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RequestHeadersSpec requestHeadersSpec;

    @Mock
    private ResponseSpec responseSpec;

    @Mock
    private AivenProperties aivenProperties;

    @InjectMocks
    private AivenService aivenService;

    @BeforeEach
    void setUp() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/project/{project_name}/service/{service_name}/user/{username}", aivenProperties.getProject(), aivenProperties.getService(), "testUser")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void testGetServiceUser_Success() {
        AivenServiceUser mockUser = new AivenServiceUser();
        when(responseSpec.bodyToMono(AivenServiceUser.class)).thenReturn(Mono.just(mockUser));

        Optional<AivenServiceUser> result = aivenService.getServiceUser("testUser");

        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
    }

    @Test
    void testGetServiceUser_Exception() {
        when(responseSpec.bodyToMono(AivenServiceUser.class)).thenThrow(WebClientResponseException.create(
                HttpStatus.NOT_FOUND.value(), "Not found", null, null, null));

        Optional<AivenServiceUser> result = aivenService.getServiceUser("testUser");

        assertTrue(result.isEmpty());
    }
}



