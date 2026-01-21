package CodeCoverage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import Service.impl.ExitAuthorizationServiceImpl;
import Model.User;
import Model.ParkingSession;
import Model.ExitDecision;
import Enum.UserStatus;
import Enum.SessionState;
import Enum.ExitFailureReason;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExitAuthorizationServiceImpl_authorizeExitTest_MCDC {

    private ExitAuthorizationServiceImpl service;

    @Mock
    private User mockUser;

    @Mock
    private ParkingSession mockSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ExitAuthorizationServiceImpl();
    }

    // ============================================================================
    // MC/DC (Modified Condition/Decision Coverage) TESTS
    // ============================================================================
    // Conditions:
    // C1: user.getStatus() == UserStatus.ACTIVE
    // C2: session.getState() != SessionState.CLOSED
    // C3: session.getState() == SessionState.PAID
    // C4: session.getVehiclePlate().equals(plate)
    //
    // For MC/DC, each condition must independently affect the outcome
    // ============================================================================

    @Test
    void authorizeExit_AllConditionsTrue_ShouldAllow_MCDC() {
        // [MC/DC - Baseline] C1=T, C2=T, C3=T, C4=T → ALLOW

        // Arrange
        String plate = "ABC-1234";
        when(mockUser.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(mockSession.getState()).thenReturn(SessionState.PAID);
        when(mockSession.getVehiclePlate()).thenReturn(plate);

        // Act
        ExitDecision result = service.authorizeExit(mockUser, mockSession, plate);

        // Assert
        assertTrue(result.isAllowed());
        assertEquals(ExitFailureReason.NONE, result.getReason());
    }

    @Test
    void authorizeExit_C1False_UserInactive_ShouldDeny_MCDC() {
        // [MC/DC - Test C1] C1=F, C2=T, C3=T, C4=T → DENY (tests C1 independently)

        // Arrange
        String plate = "ABC-1234";
        when(mockUser.getStatus()).thenReturn(UserStatus.INACTIVE); // C1 = FALSE
        when(mockSession.getState()).thenReturn(SessionState.PAID);
        when(mockSession.getVehiclePlate()).thenReturn(plate);

        // Act
        ExitDecision result = service.authorizeExit(mockUser, mockSession, plate);

        // Assert
        assertFalse(result.isAllowed());
        assertEquals(ExitFailureReason.USER_INACTIVE, result.getReason());
    }

    @Test
    void authorizeExit_C2False_SessionClosed_ShouldDeny_MCDC() {
        // [MC/DC - Test C2] C1=T, C2=F, C3=*, C4=T → DENY (tests C2 independently)

        // Arrange
        String plate = "ABC-1234";
        when(mockUser.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(mockSession.getState()).thenReturn(SessionState.CLOSED); // C2 = FALSE
        when(mockSession.getVehiclePlate()).thenReturn(plate);

        // Act
        ExitDecision result = service.authorizeExit(mockUser, mockSession, plate);

        // Assert
        assertFalse(result.isAllowed());
        assertEquals(ExitFailureReason.ALREADY_CLOSED, result.getReason());
    }

    @Test
    void authorizeExit_C3False_SessionNotPaid_ShouldDeny_MCDC() {
        // [MC/DC - Test C3] C1=T, C2=T, C3=F, C4=T → DENY (tests C3 independently)

        // Arrange
        String plate = "ABC-1234";
        when(mockUser.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(mockSession.getState()).thenReturn(SessionState.OPEN); // C2=T, C3=F
        when(mockSession.getVehiclePlate()).thenReturn(plate);

        // Act
        ExitDecision result = service.authorizeExit(mockUser, mockSession, plate);

        // Assert
        assertFalse(result.isAllowed());
        assertEquals(ExitFailureReason.SESSION_NOT_PAID, result.getReason());
    }

    @Test
    void authorizeExit_C4False_VehicleMismatch_ShouldDeny_MCDC() {
        // [MC/DC - Test C4] C1=T, C2=T, C3=T, C4=F → DENY (tests C4 independently)

        // Arrange
        String sessionPlate = "ABC-1234";
        String providedPlate = "XYZ-9999"; // Different plate
        when(mockUser.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(mockSession.getState()).thenReturn(SessionState.PAID);
        when(mockSession.getVehiclePlate()).thenReturn(sessionPlate); // C4 = FALSE

        // Act
        ExitDecision result = service.authorizeExit(mockUser, mockSession, providedPlate);

        // Assert
        assertFalse(result.isAllowed());
        assertEquals(ExitFailureReason.VEHICLE_MISMATCH, result.getReason());
    }

    @Test
    void authorizeExit_UserSuspended_ShouldDeny_MCDC() {
        // [MC/DC - Additional C1 test] C1=F (SUSPENDED), C2=T, C3=T, C4=T → DENY

        // Arrange
        String plate = "ABC-1234";
        when(mockUser.getStatus()).thenReturn(UserStatus.BLACKLISTED);
        when(mockSession.getState()).thenReturn(SessionState.PAID);
        when(mockSession.getVehiclePlate()).thenReturn(plate);

        // Act
        ExitDecision result = service.authorizeExit(mockUser, mockSession, plate);

        // Assert
        assertFalse(result.isAllowed());
        assertEquals(ExitFailureReason.USER_INACTIVE, result.getReason());
    }

    @Test
    void authorizeExit_ShortCircuitEvaluation_UserInactiveSkipsOtherChecks_MCDC() {
        // [MC/DC - Short-circuit verification] C1=F → should not check other conditions

        // Arrange
        String plate = "ABC-1234";
        when(mockUser.getStatus()).thenReturn(UserStatus.INACTIVE);
        // Don't stub session methods - they shouldn't be called

        // Act
        ExitDecision result = service.authorizeExit(mockUser, mockSession, plate);

        // Assert
        assertFalse(result.isAllowed());
        assertEquals(ExitFailureReason.USER_INACTIVE, result.getReason());
        verify(mockUser).getStatus();
        verify(mockSession, never()).getState();
        verify(mockSession, never()).getVehiclePlate();
    }
}
