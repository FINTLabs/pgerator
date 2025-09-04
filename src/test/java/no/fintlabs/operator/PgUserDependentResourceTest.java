package no.fintlabs.operator;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import no.fintlabs.aiven.AivenService;
import no.fintlabs.aiven.AivenServiceUser;
import no.fintlabs.aiven.FailedToCreateAivenObjectException;
import no.fintlabs.exceptions.NonRetryableException;
import no.fintlabs.pg.PgService;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PgUserDependentResourceTest {

    @Test
    public void testDesiredWhenSecondaryResourceIsPresent() {
        Context<PGUserCRD> context = mock(Context.class);
        PGUserCRD primary = new PGUserCRD();
        PGUserDependentResource resource = new PGUserDependentResource(new PGUserWorkflow(), mock(AivenService.class), mock(PgService.class));

        when(context.getSecondaryResource(PGUser.class)).thenReturn(Optional.of(new PGUser()));

        PGUser result = resource.desired(primary, context);

        assertNotNull(result);
    }

    @Test
    public void testDesiredWhenSecondaryResourceIsAbsent() {
        Context<PGUserCRD> context = mock(Context.class);
        PGUserCRD primary = new PGUserCRD();
        PGUserDependentResource resource = new PGUserDependentResource(new PGUserWorkflow(), mock(AivenService.class), mock(PgService.class));

        when(context.getSecondaryResource(PGUser.class)).thenReturn(Optional.empty());

        assertThrows(NullPointerException.class, () -> {
            PGUser result = resource.desired(primary, context);
        });
    }


    @Test
    public void testDelete() {

        AivenService aivenService = mock(AivenService.class);
        PgService pgService = mock(PgService.class);

        PGUserDependentResource objectUnderTest = new PGUserDependentResource(new PGUserWorkflow(), aivenService, pgService);

        Context<PGUserCRD> mockContext = mock(Context.class);
        PGUser mockPGUser = mock(PGUser.class);

        when(mockContext.getSecondaryResource(PGUser.class)).thenReturn(Optional.of(mockPGUser));

        objectUnderTest.delete(new PGUserCRD(), mockContext);

        verify(aivenService, times(1)).deleteUserForService(any());
    }

    @Test
    public void testCreateNewUser() {
        PGUser desired = new PGUser();
        desired.setUsername("testUser");
        PGUserCRD primary = new PGUserCRD();
        Context<PGUserCRD> context = mock(Context.class);
        AivenService aivenService = mock(AivenService.class);

        when(context.getSecondaryResource(PGUser.class)).thenReturn(Optional.empty());
        when(aivenService.getServiceUser("testUser")).thenReturn(Optional.empty());

        PGUser result = new PGUserDependentResource(new PGUserWorkflow(), aivenService, mock(PgService.class)).create(desired, primary, context);

        assertEquals(desired, result);
    }

    @Test
    public void testCreateExistingUser() {
        PGUser desired = new PGUser();
        desired.setUsername("existingUser");
        PGUserCRD primary = new PGUserCRD();
        Context<PGUserCRD> context = mock(Context.class);
        AivenService aivenService = mock(AivenService.class);

        when(context.getSecondaryResource(PGUser.class)).thenReturn(Optional.empty());

        AivenServiceUser existingUser = new AivenServiceUser();
        when(aivenService.getServiceUser("existingUser")).thenReturn(Optional.of(existingUser));

        assertThrows(IllegalStateException.class, () -> new PGUserDependentResource(new PGUserWorkflow(), aivenService, mock(PgService.class)).create(desired, primary, context));
    }

    @Test
    public void testFetchResources() {
        PGUserCRD primary = new PGUserCRD();
        AivenService aivenService = mock(AivenService.class);
        PgService pgService = mock(PgService.class);

        PGUserDependentResource resource = new PGUserDependentResource(new PGUserWorkflow(), aivenService, pgService);

        when(aivenService.getPgUser(primary)).thenReturn(new HashSet<>());

        Set<PGUser> result = resource.fetchResources(primary);

        assertEquals(0, result.size());
    }

    @Test
    void createCatchesNonRetryableProblemDeletesAivenUserAndDoesNotRethrow()
    throws FailedToCreateAivenObjectException {

        AivenService aivenService = mock(AivenService.class);
        PgService pgService = mock(PgService.class);
        PGUserWorkflow pgUserWorkflow = mock(PGUserWorkflow.class);
        @SuppressWarnings("unchecked")
        Context<PGUserCRD> mockContext = mock(Context.class);
        String db = "initialdb";
        String username = "ok_name";

        when(mockContext.getSecondaryResource(PGUser.class)).thenReturn(Optional.empty());
        when(aivenService.getServiceUser(username)).thenReturn(Optional.empty());

        doNothing().when(aivenService).createUserForService(any());

        doThrow(new NonRetryableException("Illegal DB identifier 'ok_name'"))
                .when(pgService).ensureSchema(db, username);

        PGUserDependentResource resource = new PGUserDependentResource(pgUserWorkflow, aivenService, pgService);

        PGUser desired = PGUser.builder()
                .database(db)
                .username(username)
                .build();
        PGUserCRD primary = new PGUserCRD();

        PGUser result = resource.create(desired, primary, mockContext);

        assertEquals(desired, result);
        verify(aivenService).createUserForService(desired);
        verify(aivenService).deleteUserForService(username);
        verify(pgService).ensureSchema(db, username);
        verify(pgService, never()).ensureUsageAndCreateOnSchema(anyString(), anyString());
    }
}
