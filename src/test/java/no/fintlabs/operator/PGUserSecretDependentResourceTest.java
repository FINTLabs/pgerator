package no.fintlabs.operator;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.Matcher;
import no.fintlabs.FlaisWorkflow;
import no.fintlabs.OperatorProperties;
import no.fintlabs.aiven.AivenService;
import no.fintlabs.pg.PgService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PGUserSecretDependentResourceTest {
    @Test
    public void testDesired() {
        PGUserCRD primary = new PGUserCRD();
        Context<PGUserCRD> context = mock(Context.class);

        PGUserSecretDependentResource pgUserSecretDependentResource = new PGUserSecretDependentResource(
                mock(FlaisWorkflow.class),
                mock(KubernetesClient.class),
                mock(PGUserDependentResource.class),
                mock(OperatorProperties.class)
        );

        PGUser pgUser = new PGUser();
        pgUser.setUsername("testUser");
        pgUser.setPassword("testPassword");
        pgUser.setDatabase("testDatabase");

        when(context.getSecondaryResource(PGUser.class)).thenReturn(Optional.of(pgUser));

        Secret secret = pgUserSecretDependentResource.desired(primary, context);

        byte[] decodedUsername = Base64.getDecoder().decode(secret.getData().get("fint.database.username"));
        String decodedUsernameString = new String(decodedUsername);

        assertEquals(pgUser.getUsername(), decodedUsernameString);

        when(context.getSecondaryResource(PGUser.class)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            pgUserSecretDependentResource.desired(primary, context);
        });
    }

    @Test
    public void testGenerateSecret() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        PGUserCRD primary = new PGUserCRD();
        Context<PGUserCRD> context = mock(Context.class);
        PGUser pgUser = new PGUser();
        PGUserWorkflow workflow = mock(PGUserWorkflow.class);
        pgUser.setUsername("testUser");
        pgUser.setPassword("testPassword");
        pgUser.setDatabase("testDatabase");
        OperatorProperties properties = mock(OperatorProperties.class);

        when(context.getSecondaryResource(PGUser.class)).thenReturn(Optional.of(pgUser));
        when(properties.getPoolBaseUrl()).thenReturn("testUrl");

        PGUserSecretDependentResource resource = new PGUserSecretDependentResource(workflow, mock(KubernetesClient.class), new PGUserDependentResource(workflow, mock(AivenService.class), mock(PgService.class)), properties);

        Method generateSecretMethod = PGUserSecretDependentResource.class.getDeclaredMethod("generateSecret", PGUserCRD.class, Context.class);
        generateSecretMethod.setAccessible(true);
        Secret secret = (Secret) generateSecretMethod.invoke(resource, primary, context);

        byte[] decodedUsername = Base64.getDecoder().decode(secret.getData().get("fint.database.username"));
        byte[] decodedPassword = Base64.getDecoder().decode(secret.getData().get("fint.database.password"));
        byte[] decodedUrl = Base64.getDecoder().decode(secret.getData().get("fint.database.url"));
        String decodedUsernameString = new String(decodedUsername);
        String decodedPasswordString = new String(decodedPassword);
        String decodedUrlString = new String(decodedUrl);


        assertEquals("Opaque", secret.getType());
        assertEquals("testUser", decodedUsernameString);
        assertEquals("testPassword", decodedPasswordString);
        assertEquals("testUrltestDatabase?sslmode=require&prepareThreshold=0&ApplicationName=testUser", decodedUrlString);
    }

    @Test
    public void testEncode() {
        String value = "test string";
        String encodedValue = Base64.getEncoder().encodeToString(value.getBytes());
        String hardCodedValue = "dGVzdCBzdHJpbmc=";
        assertEquals(encodedValue, hardCodedValue);
    }

    @Test
    public void testDecode() {
        String value = "dGVzdCBzdHJpbmc=";
        String decodedValue = new String(Base64.getDecoder().decode(value));
        String hardCodedValue = "test string";
        assertEquals(decodedValue, hardCodedValue);
    }

}
