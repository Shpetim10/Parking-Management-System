package UnitTesting.NikolaRigo;

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

class ExitAuthorizationServiceImpl_authorizeExitTest {

    private ExitAuthorizationServiceImpl authorizationService;

    @Mock
    private User mockUser;

    @Mock
    private ParkingSession mockSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authorizationService = new ExitAuthorizationServiceImpl();
    }

    @Test
    void authorizeExit_WithAllConditionsMet_ShouldAllowExit() {
        // Arrange
        String vehiclePlate = "ABC-1234";
        when(mockUser.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(mockSession.getState()).thenReturn(SessionState.PAID);
        when(mockSession.getVehiclePlate()).thenReturn(vehiclePlate);

        // Act
        ExitDecision result = authorizationService.authorizeExit(mockUser, mockSession, vehiclePlate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isAllowed());
        assertEquals(ExitFailureReason.NONE, result.getReason());
        verify(mockUser).getStatus();
        verify(mockSession, times(2)).getState();
        verify(mockSession).getVehiclePlate();
    }

    @Test
    void authorizeExit_WithInactiveUser_ShouldDenyWithUserInactiveReason() {
        // Arrange
        String vehiclePlate = "ABC-1234";
        when(mockUser.getStatus()).thenReturn(UserStatus.INACTIVE);
        when(mockSession.getState()).thenReturn(SessionState.PAID);
        when(mockSession.getVehiclePlate()).thenReturn(vehiclePlate);

        // Act
        ExitDecision result = authorizationService.authorizeExit(mockUser, mockSession, vehiclePlate);

        // Assert
        assertNotNull(result);
        assertFalse(result.isAllowed());
        assertEquals(ExitFailureReason.USER_INACTIVE, result.getReason());
        verify(mockUser).getStatus();
        verify(mockSession, never()).getState();
        verify(mockSession, never()).getVehiclePlate();
    }

    @Test
    void authorizeExit_WithClosedSession_ShouldDenyWithAlreadyClosedReason() {
        // Arrange
        String vehiclePlate = "ABC-1234";
        when(mockUser.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(mockSession.getState()).thenReturn(SessionState.CLOSED);
        when(mockSession.getVehiclePlate()).thenReturn(vehiclePlate);

        // Act
        ExitDecision result = authorizationService.authorizeExit(mockUser, mockSession, vehiclePlate);

        // Assert
        assertNotNull(result);
        assertFalse(result.isAllowed());
        assertEquals(ExitFailureReason.ALREADY_CLOSED, result.getReason());
        verify(mockUser).getStatus();
        verify(mockSession).getState();
        verify(mockSession, never()).getVehiclePlate();
    }

    @Test
    void authorizeExit_WithUnpaidSession_ShouldDenyWithSessionNotPaidReason() {
        // Arrange
        String vehiclePlate = "ABC-1234";
        when(mockUser.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(mockSession.getState()).thenReturn(SessionState.OPEN);
        when(mockSession.getVehiclePlate()).thenReturn(vehiclePlate);

        // Act
        ExitDecision result = authorizationService.authorizeExit(mockUser, mockSession, vehiclePlate);

        // Assert
        assertNotNull(result);
        assertFalse(result.isAllowed());
        assertEquals(ExitFailureReason.SESSION_NOT_PAID, result.getReason());
        verify(mockUser).getStatus();
        verify(mockSession, times(2)).getState();
        verify(mockSession, never()).getVehiclePlate();
    }

    @Test
    void authorizeExit_WithVehicleMismatch_ShouldDenyWithVehicleMismatchReason() {
        // Arrange
        String sessionPlate = "ABC-1234";
        String providedPlate = "XYZ-5678";
        when(mockUser.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(mockSession.getState()).thenReturn(SessionState.PAID);
        when(mockSession.getVehiclePlate()).thenReturn(sessionPlate);

        // Act
        ExitDecision result = authorizationService.authorizeExit(mockUser, mockSession, providedPlate);

        // Assert
        assertNotNull(result);
        assertFalse(result.isAllowed());
        assertEquals(ExitFailureReason.VEHICLE_MISMATCH, result.getReason());
        verify(mockUser).getStatus();
        verify(mockSession, times(2)).getState();
        verify(mockSession).getVehiclePlate();
    }

    @Test
    void authorizeExit_ChecksConditionsInCorrectOrder_ShouldShortCircuit() {
        // Arrange
        String vehiclePlate = "ABC-1234";
        when(mockUser.getStatus()).thenReturn(UserStatus.INACTIVE);
        // Session state and plate should not be checked if user is inactive

        // Act
        ExitDecision result = authorizationService.authorizeExit(mockUser, mockSession, vehiclePlate);

        // Assert
        assertFalse(result.isAllowed());
        verify(mockUser).getStatus();
        verify(mockSession, never()).getState();
        verify(mockSession, never()).getVehiclePlate();
    }

    @Test
    void authorizeExit_WithCaseSensitivePlates_ShouldDenyIfCaseDiffers() {
        // Arrange
        String sessionPlate = "ABC-1234";
        String providedPlate = "abc-1234";
        when(mockUser.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(mockSession.getState()).thenReturn(SessionState.PAID);
        when(mockSession.getVehiclePlate()).thenReturn(sessionPlate);

        // Act
        ExitDecision result = authorizationService.authorizeExit(mockUser, mockSession, providedPlate);

        // Assert
        assertFalse(result.isAllowed());
        assertEquals(ExitFailureReason.VEHICLE_MISMATCH, result.getReason());
        verify(mockSession).getVehiclePlate();
    }

    @Test
    void authorizeExit_CalledMultipleTimesWithSameValidInputs_ShouldAllowEachTime() {
        // Arrange
        String vehiclePlate = "ABC-1234";
        when(mockUser.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(mockSession.getState()).thenReturn(SessionState.PAID);
        when(mockSession.getVehiclePlate()).thenReturn(vehiclePlate);

        // Act
        ExitDecision result1 = authorizationService.authorizeExit(mockUser, mockSession, vehiclePlate);
        ExitDecision result2 = authorizationService.authorizeExit(mockUser, mockSession, vehiclePlate);
        ExitDecision result3 = authorizationService.authorizeExit(mockUser, mockSession, vehiclePlate);

        // Assert
        assertTrue(result1.isAllowed());
        assertTrue(result2.isAllowed());
        assertTrue(result3.isAllowed());
        verify(mockUser, times(3)).getStatus();
        verify(mockSession, times(6)).getState();
        verify(mockSession, times(3)).getVehiclePlate();
    }
}
