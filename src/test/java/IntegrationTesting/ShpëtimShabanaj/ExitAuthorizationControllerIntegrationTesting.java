package IntegrationTesting.ShpëtimShabanaj;

import Controller.ExitAuthorizationController;
import Dto.Exit.ExitAuthorizationRequestDto;
import Dto.Exit.ExitAuthorizationResponseDto;
import Enum.*;
import Model.*;
import Repository.ParkingSessionRepository;
import Repository.ParkingZoneRepository;
import Repository.UserRepository;
import Repository.impl.InMemoryParkingSessionRepository;
import Repository.impl.InMemoryParkingZoneRepository;
import Repository.impl.InMemoryUserRepository;
import Service.ExitAuthorizationService;
import Service.impl.ExitAuthorizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Neighbourhood Integration: ExitAuthorizationController")
class ExitAuthorizationControllerIntegrationTesting {
    private ExitAuthorizationController controller;

    // ALL REAL
    private ExitAuthorizationService exitAuthorizationService;
    private UserRepository userRepository;
    private ParkingSessionRepository sessionRepository;
    private ParkingZoneRepository zoneRepository;

    @BeforeEach
    void setUp() {
        exitAuthorizationService = new ExitAuthorizationServiceImpl();
        userRepository = new InMemoryUserRepository();
        sessionRepository = new InMemoryParkingSessionRepository();
        zoneRepository = new InMemoryParkingZoneRepository();

        controller = new ExitAuthorizationController(
                exitAuthorizationService,
                userRepository,
                sessionRepository,
                zoneRepository
        );
    }

    // IT-01: Happy path - complete flow
    @Test
    @DisplayName("IT-01: Should authorize exit, close session, set end time and release spot")
    void testAuthorizeExitValidRequestAllSideEffectsApplied() {
        User activeUser = new User("U1", UserStatus.ACTIVE);
        userRepository.save(activeUser);

        ParkingSession paidSession = new ParkingSession(
                "S1",
                "U1",
                "AA111",
                "Z1",
                "S1",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 15, 9, 0)
        );
        paidSession.markPaid();
        sessionRepository.save(paidSession);

        ParkingZone zone = new ParkingZone("Z1", ZoneType.STANDARD, 0.85);
        ParkingSpot spot = new ParkingSpot("S1", zone);
        spot.occupy();
        zone.addSpot(spot);
        zoneRepository.save(zone);

        ExitAuthorizationRequestDto request = new ExitAuthorizationRequestDto(
                "U1",
                "S1",
                "AA111"
        );

        LocalDateTime beforeExit = LocalDateTime.now();
        
        ExitAuthorizationResponseDto response = controller.authorizeExit(request);

        LocalDateTime afterExit = LocalDateTime.now();

        // Assert - Response
        assertAll("Verify exit authorized",
                () -> assertTrue(response.allowed()),
                () -> assertEquals( ExitFailureReason.NONE, response.reason())
        );

        // Assert - Session updated and endTime set in expected interval
        ParkingSession updatedSession = sessionRepository.findById("S1").orElseThrow();
        assertAll("Verify session closed and end time set",
                () -> assertEquals(SessionState.CLOSED, updatedSession.getState()),
                () -> assertNotNull(updatedSession.getEndTime()),
                () -> assertFalse(updatedSession.getEndTime().isBefore(beforeExit),
                        "endTime should not be before beforeExit"),
                () -> assertFalse(updatedSession.getEndTime().isAfter(afterExit),
                        "endTime should not be after afterExit")
        );

        // Assert - Spot released
        ParkingZone updatedZone = zoneRepository.findById("Z1");
        ParkingSpot updatedSpot = updatedZone.getSpots().stream()
                .filter(s -> s.getSpotId().equals("S1"))
                .findFirst()
                .orElseThrow();
        assertFalse(updatedSpot.isOccupied(), "Spot should be released");
    }

    // IT-02: User not found - early failure
    @Test
    @DisplayName("IT-02: Should fail early when user not found in repository")
    void testAuthorizeExitUserNotFoundFailsEarly() {
        // Don't save user
        ExitAuthorizationRequestDto request = new ExitAuthorizationRequestDto(
                "USR-NON-EXIST",
                "S1",
                "AA111"
        );

        
        ExitAuthorizationResponseDto response = controller.authorizeExit(request);

        // Assert
        assertAll("Verify early failure",
                () -> assertFalse(response.allowed()),
                () -> assertEquals(ExitFailureReason.USER_INACTIVE, response.reason())
        );
    }

    // IT-03: Session not found
    @Test
    @DisplayName("IT-03: Should return ALREADY_CLOSED when session not found")
    void testAuthorizeExitSessionNotFoundAlreadyClosedReason() {
        User activeUser = new User("U1", UserStatus.ACTIVE);
        userRepository.save(activeUser);

        // Do not save session
        ExitAuthorizationRequestDto request = new ExitAuthorizationRequestDto(
                "U1",
                "SESSION-NON-EXIST",
                "AA111"
        );

        
        ExitAuthorizationResponseDto response = controller.authorizeExit(request);

        // Assert
        assertAll("Verify session not found",
                () -> assertFalse(response.allowed()),
                () -> assertEquals(ExitFailureReason.ALREADY_CLOSED, response.reason())
        );
    }

    // IT-04: User inactive
    @Test
    @DisplayName("IT-04: Should deny exit when user is inactive")
    void testAuthorizeExitInactiveUserExitDenied() {
        User inactiveUser = new User("U2", UserStatus.INACTIVE);
        userRepository.save(inactiveUser);

        ParkingSession paidSession = new ParkingSession(
                "S2",
                "U2",
                "AA333",
                "Z1",
                "S1",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 15, 9, 0)
        );
        paidSession.markPaid();
        sessionRepository.save(paidSession);

        ExitAuthorizationRequestDto request = new ExitAuthorizationRequestDto(
                "U2",
                "S2",
                "AA333"
        );

        
        ExitAuthorizationResponseDto response = controller.authorizeExit(request);

        // Assert
        assertFalse(response.allowed());
        assertEquals(ExitFailureReason.USER_INACTIVE, response.reason());

        // Verify no side effects on session
        ParkingSession session = sessionRepository.findById("S2").orElseThrow();
        assertNotEquals(SessionState.CLOSED, session.getState());
        assertNull(session.getEndTime());
    }

    // IT-05: Session already closed
    @Test
    @DisplayName("IT-05: Should deny exit when session already closed")
    void testAuthorizeExitClosedSessionExitDenied() {
        User activeUser = new User("U1", UserStatus.ACTIVE);
        userRepository.save(activeUser);

        ParkingSession closedSession = new ParkingSession(
                "S3",
                "U1",
                "AA111",
                "Z1",
                "S1",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 15, 9, 0)
        );
        closedSession.markPaid();
        closedSession.close(LocalDateTime.of(2026, 1, 15, 13, 0));
        sessionRepository.save(closedSession);

        ExitAuthorizationRequestDto request = new ExitAuthorizationRequestDto(
                "U1",
                "S3",
                "AA111"
        );
        
        ExitAuthorizationResponseDto response = controller.authorizeExit(request);

        // Assert
        assertFalse(response.allowed());
        assertEquals(ExitFailureReason.ALREADY_CLOSED, response.reason());
    }

    // IT-06: Session not paid
    @Test
    @DisplayName("IT-06: Should deny exit when session not paid")
    void testAuthorizeExitUnpaidSessionExitDenied() {
        User activeUser = new User("U1", UserStatus.ACTIVE);
        userRepository.save(activeUser);

        ParkingSession unpaidSession = new ParkingSession(
                "S4",
                "U1",
                "AA111",
                "Z1",
                "S1",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 15, 9, 0)
        );
        // not paid
        sessionRepository.save(unpaidSession);

        ExitAuthorizationRequestDto request = new ExitAuthorizationRequestDto(
                "U1",
                "S4",
                "AA111"
        );

        
        ExitAuthorizationResponseDto response = controller.authorizeExit(request);

        // Assert
        assertFalse(response.allowed());
        assertEquals(ExitFailureReason.SESSION_NOT_PAID, response.reason());

        // Verify session not closed and endTime not set
        ParkingSession session = sessionRepository.findById("S4").orElseThrow();
        assertNotEquals(SessionState.CLOSED, session.getState());
        assertNull(session.getEndTime());
    }

    // IT-07: Vehicle plate mismatch
    @Test
    @DisplayName("IT-07: Should deny exit when vehicle plate doesn't match")
    void testAuthorizeExitVehicleMismatchExitDenied() {
        User activeUser = new User("U1", UserStatus.ACTIVE);
        userRepository.save(activeUser);

        ParkingSession paidSession = new ParkingSession(
                "S5",
                "U1",
                "AA111",
                "Z1",
                "S1",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 15, 9, 0)
        );
        paidSession.markPaid();
        sessionRepository.save(paidSession);

        ExitAuthorizationRequestDto request = new ExitAuthorizationRequestDto(
                "U1",
                "S5",
                "WRONG-PLATE"
        );

        
        ExitAuthorizationResponseDto response = controller.authorizeExit(request);

        // Assert
        assertFalse(response.allowed());
        assertEquals(ExitFailureReason.VEHICLE_MISMATCH, response.reason());

        // Verify session not closed and endTime not set
        ParkingSession session = sessionRepository.findById("S5").orElseThrow();
        assertNotEquals(SessionState.CLOSED, session.getState());
        assertNull(session.getEndTime());
    }

    // IT-08: Spot not released when exit denied
    @Test
    @DisplayName("IT-08: Should not release spot when exit is denied")
    void testAuthorizeExitDeniedExitSpotNotReleased() {
        User activeUser = new User("U1", UserStatus.ACTIVE);
        userRepository.save(activeUser);

        ParkingSession unpaidSession = new ParkingSession(
                "S7",
                "U1",
                "AA111",
                "Z1",
                "S7",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 15, 9, 0)
        );
        sessionRepository.save(unpaidSession);

        ParkingZone zone = new ParkingZone("Z1", ZoneType.STANDARD, 0.85);
        ParkingSpot spot = new ParkingSpot("S7", zone);
        spot.occupy();
        zone.addSpot(spot);
        zoneRepository.save(zone);

        ExitAuthorizationRequestDto request = new ExitAuthorizationRequestDto(
                "U1",
                "S7",
                "AA111"
        );

        
        ExitAuthorizationResponseDto response = controller.authorizeExit(request);

        // Exit denied
        assertFalse(response.allowed());

        // Spot still occupied
        ParkingZone updatedZone = zoneRepository.findById("Z1");
        ParkingSpot updatedSpot = updatedZone.getSpots().stream()
                .filter(s -> s.getSpotId().equals("S7"))
                .findFirst()
                .orElseThrow();
        assertTrue(updatedSpot.isOccupied(), "Spot should still be occupied");
    }

    // IT-09: Multiple users can exit independently
    @Test
    @DisplayName("IT-09: Should handle exit authorization for multiple users independently")
    void testAuthorizeExitMultipleUsersIndependentProcessing() {
        // User 1
        User user1 = new User("U1", UserStatus.ACTIVE);
        userRepository.save(user1);

        ParkingSession session1 = new ParkingSession(
                "S1",
                "U1",
                "AA111",
                "Z1",
                "S1",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 15, 9, 0)
        );
        session1.markPaid();
        sessionRepository.save(session1);

        // User 2
        User user2 = new User("U2", UserStatus.INACTIVE);//
        userRepository.save(user2);

        ParkingSession session2 = new ParkingSession(
                "S2",
                "U2",
                "AA222",
                "Z1",
                "S2",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 15, 9, 0)
        );
        session2.markPaid();
        sessionRepository.save(session2);

        ParkingZone zone = new ParkingZone("Z1", ZoneType.STANDARD, 0.85);
        ParkingSpot spot1 = new ParkingSpot("S1", zone);
        spot1.occupy();
        ParkingSpot spot2 = new ParkingSpot("S2", zone);
        spot2.occupy();
        zone.addSpot(spot1);
        zone.addSpot(spot2);
        zoneRepository.save(zone);

        // User 1 exits (should succeed)
        ExitAuthorizationResponseDto response1 = controller.authorizeExit(
                new ExitAuthorizationRequestDto("U1", "S1", "AA111")
        );

        // User 2 fails
        ExitAuthorizationResponseDto response2 = controller.authorizeExit(
                new ExitAuthorizationRequestDto("U2", "S2", "AA222")
        );

        // Assert - User 1 successful
        assertTrue(response1.allowed());
        assertEquals(SessionState.CLOSED, sessionRepository.findById("S1").orElseThrow().getState());

        // Assert - User 2 failed
        assertFalse(response2.allowed());
        assertEquals(ExitFailureReason.USER_INACTIVE, response2.reason());
        assertNotEquals(SessionState.CLOSED, sessionRepository.findById("S2").orElseThrow().getState());
    }

    // IT-10: Handles spot not in zone -- but the user can exit
    @Test
    @DisplayName("IT-10: Should handle case when spot not found in zone while still authorizing exit")
    void testAuthorizeExitSpotNotInZoneExitStillAuthorized() {
        
        User activeUser = new User("U1", UserStatus.ACTIVE);
        userRepository.save(activeUser);

        ParkingSession paidSession = new ParkingSession(
                "S9",
                "U1",
                "AA111",
                "Z1",
                "SPOT-NON-EXIST",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 15, 9, 0)
        );
        paidSession.markPaid();
        sessionRepository.save(paidSession);

        // Zone exists but has no spots matching SPOT-NON-EXIST
        ParkingZone zone = new ParkingZone("Z1", ZoneType.STANDARD, 0.85);
        zoneRepository.save(zone);

        ExitAuthorizationRequestDto request = new ExitAuthorizationRequestDto(
                "U1",
                "S9",
                "AA111"
        );

        
        ExitAuthorizationResponseDto response = controller.authorizeExit(request);

        // Assert - Exit still authorized
        assertTrue(response.allowed());

        // Session closed
        assertEquals(SessionState.CLOSED, sessionRepository.findById("S9").orElseThrow().getState());
    }
}
