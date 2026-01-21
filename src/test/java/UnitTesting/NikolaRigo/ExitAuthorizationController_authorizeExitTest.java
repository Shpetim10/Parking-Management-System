package UnitTesting.NikolaRigo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import Controller.ExitAuthorizationController;
import Dto.Exit.ExitAuthorizationRequestDto;
import Dto.Exit.ExitAuthorizationResponseDto;
import Model.*;
import Repository.ParkingSessionRepository;
import Repository.ParkingZoneRepository;
import Repository.UserRepository;
import Service.ExitAuthorizationService;
import Enum.ExitFailureReason;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExitAuthorizationController_authorizeExitTest {

    private ExitAuthorizationController controller;

    @Mock
    private ExitAuthorizationService mockExitAuthService;

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private ParkingSessionRepository mockSessionRepository;

    @Mock
    private ParkingZoneRepository mockZoneRepository;

    @Mock
    private User mockUser;

    @Mock
    private ParkingSession mockSession;

    @Mock
    private ParkingZone mockZone;

    @Mock
    private ParkingSpot mockSpot;

    @Mock
    private ExitDecision mockDecision;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ExitAuthorizationController(
                mockExitAuthService,
                mockUserRepository,
                mockSessionRepository,
                mockZoneRepository
        );
    }

    @Test
    void authorizeExit_WhenUserNotFound_ShouldReturnDeniedWithUserInactiveReason() {
        // Arrange
        ExitAuthorizationRequestDto dto = new ExitAuthorizationRequestDto(
                "user-123",
                "session-456",
                "ABC-1234"
        );
        when(mockUserRepository.findById("user-123")).thenReturn(Optional.empty());

        // Act
        ExitAuthorizationResponseDto result = controller.authorizeExit(dto);

        // Assert
        assertNotNull(result);
        assertFalse(result.allowed());
        assertEquals(ExitFailureReason.USER_INACTIVE, result.reason());
        verify(mockUserRepository).findById("user-123");
        verify(mockSessionRepository, never()).findById(anyString());
        verify(mockExitAuthService, never()).authorizeExit(any(), any(), anyString());
        verify(mockSessionRepository, never()).save(any());
    }

    @Test
    void authorizeExit_WhenSessionNotFound_ShouldReturnDeniedWithAlreadyClosedReason() {
        // Arrange
        ExitAuthorizationRequestDto dto = new ExitAuthorizationRequestDto(
                "user-123",
                "session-456",
                "ABC-1234"
        );
        when(mockUserRepository.findById("user-123")).thenReturn(Optional.of(mockUser));
        when(mockSessionRepository.findById("session-456")).thenReturn(Optional.empty());

        // Act
        ExitAuthorizationResponseDto result = controller.authorizeExit(dto);

        // Assert
        assertNotNull(result);
        assertFalse(result.allowed());
        assertEquals(ExitFailureReason.ALREADY_CLOSED, result.reason());
        verify(mockUserRepository).findById("user-123");
        verify(mockSessionRepository).findById("session-456");
        verify(mockExitAuthService, never()).authorizeExit(any(), any(), anyString());
        verify(mockSessionRepository, never()).save(any());
    }

    @Test
    void authorizeExit_WhenAuthorizationDenied_ShouldReturnDeniedResponse() {
        // Arrange
        ExitAuthorizationRequestDto dto = new ExitAuthorizationRequestDto(
                "user-123",
                "session-456",
                "ABC-1234"
        );
        when(mockUserRepository.findById("user-123")).thenReturn(Optional.of(mockUser));
        when(mockSessionRepository.findById("session-456")).thenReturn(Optional.of(mockSession));
        when(mockDecision.isAllowed()).thenReturn(false);
        when(mockDecision.getReason()).thenReturn(ExitFailureReason.SESSION_NOT_PAID);
        when(mockExitAuthService.authorizeExit(mockUser, mockSession, "ABC-1234"))
                .thenReturn(mockDecision);

        // Act
        ExitAuthorizationResponseDto result = controller.authorizeExit(dto);

        // Assert
        assertNotNull(result);
        assertFalse(result.allowed());
        assertEquals(ExitFailureReason.SESSION_NOT_PAID, result.reason());
        verify(mockUserRepository).findById("user-123");
        verify(mockSessionRepository).findById("session-456");
        verify(mockExitAuthService).authorizeExit(mockUser, mockSession, "ABC-1234");
        verify(mockSession, never()).close(any());
        verify(mockSessionRepository, never()).save(any());
        verify(mockZoneRepository, never()).findById(anyString());
    }

    @Test
    void authorizeExit_WhenAuthorizationAllowed_ShouldCloseSessionAndReleaseSpot() {
        // Arrange
        ExitAuthorizationRequestDto dto = new ExitAuthorizationRequestDto(
                "user-123",
                "session-456",
                "ABC-1234"
        );

        List<ParkingSpot> spots = new ArrayList<>();
        spots.add(mockSpot);

        when(mockUserRepository.findById("user-123")).thenReturn(Optional.of(mockUser));
        when(mockSessionRepository.findById("session-456")).thenReturn(Optional.of(mockSession));
        when(mockDecision.isAllowed()).thenReturn(true);
        when(mockDecision.getReason()).thenReturn(ExitFailureReason.NONE);
        when(mockExitAuthService.authorizeExit(mockUser, mockSession, "ABC-1234"))
                .thenReturn(mockDecision);
        when(mockSession.getZoneId()).thenReturn("zone-789");
        when(mockSession.getSpotId()).thenReturn("spot-001");
        when(mockZoneRepository.findById("zone-789")).thenReturn(mockZone);
        when(mockZone.getSpots()).thenReturn(spots);
        when(mockSpot.getSpotId()).thenReturn("spot-001");
        when(mockSpot.isOccupied()).thenReturn(true);

        // Act
        ExitAuthorizationResponseDto result = controller.authorizeExit(dto);

        // Assert
        assertNotNull(result);
        assertTrue(result.allowed());
        assertEquals(ExitFailureReason.NONE, result.reason());
        verify(mockUserRepository).findById("user-123");
        verify(mockSessionRepository).findById("session-456");
        verify(mockExitAuthService).authorizeExit(mockUser, mockSession, "ABC-1234");
        verify(mockSession).close(any(LocalDateTime.class));
        verify(mockSessionRepository).save(mockSession);
        verify(mockZoneRepository).findById("zone-789");
        verify(mockSpot).isOccupied();
        verify(mockSpot).release();
    }

    @Test
    void authorizeExit_WhenAuthorizationAllowedButSpotNotOccupied_ShouldNotReleaseSpot() {
        // Arrange
        ExitAuthorizationRequestDto dto = new ExitAuthorizationRequestDto(
                "user-123",
                "session-456",
                "ABC-1234"
        );

        List<ParkingSpot> spots = new ArrayList<>();
        spots.add(mockSpot);

        when(mockUserRepository.findById("user-123")).thenReturn(Optional.of(mockUser));
        when(mockSessionRepository.findById("session-456")).thenReturn(Optional.of(mockSession));
        when(mockDecision.isAllowed()).thenReturn(true);
        when(mockDecision.getReason()).thenReturn(ExitFailureReason.NONE);
        when(mockExitAuthService.authorizeExit(mockUser, mockSession, "ABC-1234"))
                .thenReturn(mockDecision);
        when(mockSession.getZoneId()).thenReturn("zone-789");
        when(mockSession.getSpotId()).thenReturn("spot-001");
        when(mockZoneRepository.findById("zone-789")).thenReturn(mockZone);
        when(mockZone.getSpots()).thenReturn(spots);
        when(mockSpot.getSpotId()).thenReturn("spot-001");
        when(mockSpot.isOccupied()).thenReturn(false);

        // Act
        ExitAuthorizationResponseDto result = controller.authorizeExit(dto);

        // Assert
        assertTrue(result.allowed());
        verify(mockSession).close(any(LocalDateTime.class));
        verify(mockSessionRepository).save(mockSession);
        verify(mockSpot).isOccupied();
        verify(mockSpot, never()).release();
    }

    @Test
    void authorizeExit_WhenAuthorizationAllowedWithMultipleSpots_ShouldReleaseCorrectSpot() {
        // Arrange
        ExitAuthorizationRequestDto dto = new ExitAuthorizationRequestDto(
                "user-123",
                "session-456",
                "ABC-1234"
        );

        ParkingSpot mockSpot1 = mock(ParkingSpot.class);
        ParkingSpot mockSpot2 = mock(ParkingSpot.class);
        ParkingSpot mockSpot3 = mock(ParkingSpot.class);

        List<ParkingSpot> spots = new ArrayList<>();
        spots.add(mockSpot1);
        spots.add(mockSpot2);
        spots.add(mockSpot3);

        when(mockUserRepository.findById("user-123")).thenReturn(Optional.of(mockUser));
        when(mockSessionRepository.findById("session-456")).thenReturn(Optional.of(mockSession));
        when(mockDecision.isAllowed()).thenReturn(true);
        when(mockDecision.getReason()).thenReturn(ExitFailureReason.NONE);
        when(mockExitAuthService.authorizeExit(mockUser, mockSession, "ABC-1234"))
                .thenReturn(mockDecision);
        when(mockSession.getZoneId()).thenReturn("zone-789");
        when(mockSession.getSpotId()).thenReturn("spot-002");
        when(mockZoneRepository.findById("zone-789")).thenReturn(mockZone);
        when(mockZone.getSpots()).thenReturn(spots);
        when(mockSpot1.getSpotId()).thenReturn("spot-001");
        when(mockSpot2.getSpotId()).thenReturn("spot-002");
        when(mockSpot3.getSpotId()).thenReturn("spot-003");
        when(mockSpot2.isOccupied()).thenReturn(true);

        // Act
        ExitAuthorizationResponseDto result = controller.authorizeExit(dto);

        // Assert
        assertTrue(result.allowed());
        verify(mockSession).close(any(LocalDateTime.class));
        verify(mockSessionRepository).save(mockSession);
        verify(mockSpot1, never()).isOccupied();
        verify(mockSpot1, never()).release();
        verify(mockSpot2).isOccupied();
        verify(mockSpot2).release();
        verify(mockSpot3, never()).isOccupied();
        verify(mockSpot3, never()).release();
    }

    @Test
    void authorizeExit_WhenAuthorizationAllowedWithVehicleMismatch_ShouldReturnDenied() {
        // Arrange
        ExitAuthorizationRequestDto dto = new ExitAuthorizationRequestDto(
                "user-123",
                "session-456",
                "XYZ-9999"
        );

        when(mockUserRepository.findById("user-123")).thenReturn(Optional.of(mockUser));
        when(mockSessionRepository.findById("session-456")).thenReturn(Optional.of(mockSession));
        when(mockDecision.isAllowed()).thenReturn(false);
        when(mockDecision.getReason()).thenReturn(ExitFailureReason.VEHICLE_MISMATCH);
        when(mockExitAuthService.authorizeExit(mockUser, mockSession, "XYZ-9999"))
                .thenReturn(mockDecision);

        // Act
        ExitAuthorizationResponseDto result = controller.authorizeExit(dto);

        // Assert
        assertFalse(result.allowed());
        assertEquals(ExitFailureReason.VEHICLE_MISMATCH, result.reason());
        verify(mockExitAuthService).authorizeExit(mockUser, mockSession, "XYZ-9999");
        verify(mockSession, never()).close(any());
        verify(mockSessionRepository, never()).save(any());
    }

    @Test
    void authorizeExit_WhenSessionClosedSuccessfully_ShouldPassCurrentTime() {
        // Arrange
        ExitAuthorizationRequestDto dto = new ExitAuthorizationRequestDto(
                "user-123",
                "session-456",
                "ABC-1234"
        );

        List<ParkingSpot> spots = new ArrayList<>();
        spots.add(mockSpot);

        when(mockUserRepository.findById("user-123")).thenReturn(Optional.of(mockUser));
        when(mockSessionRepository.findById("session-456")).thenReturn(Optional.of(mockSession));
        when(mockDecision.isAllowed()).thenReturn(true);
        when(mockExitAuthService.authorizeExit(mockUser, mockSession, "ABC-1234"))
                .thenReturn(mockDecision);
        when(mockSession.getZoneId()).thenReturn("zone-789");
        when(mockSession.getSpotId()).thenReturn("spot-001");
        when(mockZoneRepository.findById("zone-789")).thenReturn(mockZone);
        when(mockZone.getSpots()).thenReturn(spots);
        when(mockSpot.getSpotId()).thenReturn("spot-001");
        when(mockSpot.isOccupied()).thenReturn(true);

        LocalDateTime beforeCall = LocalDateTime.now();

        // Act
        controller.authorizeExit(dto);

        LocalDateTime afterCall = LocalDateTime.now();

        // Assert
        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(mockSession).close(timeCaptor.capture());

        LocalDateTime capturedTime = timeCaptor.getValue();
        assertTrue(!capturedTime.isBefore(beforeCall));
        assertTrue(!capturedTime.isAfter(afterCall));
    }

    @Test
    void authorizeExit_WhenSpotNotFoundInZone_ShouldStillCloseSession() {
        // Arrange
        ExitAuthorizationRequestDto dto = new ExitAuthorizationRequestDto(
                "user-123",
                "session-456",
                "ABC-1234"
        );

        ParkingSpot differentSpot = mock(ParkingSpot.class);
        List<ParkingSpot> spots = new ArrayList<>();
        spots.add(differentSpot);

        when(mockUserRepository.findById("user-123")).thenReturn(Optional.of(mockUser));
        when(mockSessionRepository.findById("session-456")).thenReturn(Optional.of(mockSession));
        when(mockDecision.isAllowed()).thenReturn(true);
        when(mockExitAuthService.authorizeExit(mockUser, mockSession, "ABC-1234"))
                .thenReturn(mockDecision);
        when(mockSession.getZoneId()).thenReturn("zone-789");
        when(mockSession.getSpotId()).thenReturn("spot-001");
        when(mockZoneRepository.findById("zone-789")).thenReturn(mockZone);
        when(mockZone.getSpots()).thenReturn(spots);
        when(differentSpot.getSpotId()).thenReturn("spot-999");

        // Act
        ExitAuthorizationResponseDto result = controller.authorizeExit(dto);

        // Assert
        assertTrue(result.allowed());
        verify(mockSession).close(any(LocalDateTime.class));
        verify(mockSessionRepository).save(mockSession);
        verify(differentSpot, never()).isOccupied();
        verify(differentSpot, never()).release();
    }
}
