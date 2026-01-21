package IntegrationTesting.NikolaRigo;

import Controller.ParkingSessionController;
import Dto.Session.StartSessionRequestDto;
import Dto.Session.StartSessionResponseDto;
import Enum.*;
import Model.*;
import Repository.ParkingSessionRepository;
import Repository.ParkingZoneRepository;
import Repository.impl.InMemoryParkingSessionRepository;
import Repository.impl.InMemoryParkingZoneRepository;
import Settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ParkingSessionControllerIT {

    private ParkingSessionController controller;
    private ParkingSessionRepository sessionRepo;
    private ParkingZoneRepository zoneRepo;

    // Fixed ID constants for testing
    private final String ZONE_ID = "zone-1";
    private final String SPOT_ID = "spot-1";
    private final String USER_ID = "user-1";
    private final String PLATE = "ABC-123";

    @BeforeEach
    void setUp() {
        // 1. Initialize In-Memory Repositories
        sessionRepo = new InMemoryParkingSessionRepository();
        zoneRepo = new InMemoryParkingZoneRepository();

        // 2. Initialize Controller
        controller = new ParkingSessionController(sessionRepo, zoneRepo);

        // 3. Seed Data: Create a Zone and a Spot
        ParkingZone zone = new ParkingZone(ZONE_ID, ZoneType.STANDARD, 1.0);
        ParkingSpot spot = new ParkingSpot(SPOT_ID, zone);
        zone.addSpot(spot);

        zoneRepo.save(zone);
    }

    // =========================================================================
    // 1. Start Session Tests (Happy Path & Logic)
    // =========================================================================

    @Test
    void startSession_whenValidRequest_shouldCreateSessionAndOccupySpot() {
        // Arrange: Use a Standard Weekday Morning (e.g., Wed 10:00 AM)
        LocalDateTime startTime = LocalDateTime.of(2023, Month.OCTOBER, 25, 10, 0);

        StartSessionRequestDto request = new StartSessionRequestDto(
                USER_ID, PLATE, ZONE_ID, SPOT_ID, ZoneType.STANDARD,
                false, // isHoliday
                startTime
        );

        // Act
        StartSessionResponseDto response = controller.startSession(request);

        // Assert 1: Response DTO
        assertNotNull(response);
        assertNotNull(response.sessionId());
        assertEquals(SessionState.OPEN, response.state());

        // Assert 2: Persistence
        Optional<ParkingSession> savedSession = sessionRepo.findById(response.sessionId());
        assertTrue(savedSession.isPresent(), "Session should be saved to repository");

        // Assert 3: Side Effect (Spot Occupancy)
        ParkingZone zone = zoneRepo.findById(ZONE_ID);
        ParkingSpot spot = zone.getSpots().get(0);
        assertEquals(SpotState.OCCUPIED, spot.getState(), "Spot should be marked OCCUPIED");
    }

    @Test
    void startSession_shouldCalculatePeakTimeCorrectly() {
        // Arrange: Settings say Peak is 11:00 - 21:00.
        // We test 12:00 PM (Should be PEAK)
        LocalDateTime peakTime = LocalDateTime.of(2023, Month.OCTOBER, 25, 12, 0);

        StartSessionRequestDto request = new StartSessionRequestDto(
                USER_ID, PLATE, ZONE_ID, SPOT_ID, ZoneType.STANDARD,
                false,
                peakTime
        );

        // Act
        StartSessionResponseDto response = controller.startSession(request);

        // Assert
        assertEquals(TimeOfDayBand.PEAK, response.timeBand());
    }

    @Test
    void startSession_shouldCalculateOffPeakTimeCorrectly() {
        // Arrange: Settings say Peak starts at 11:00.
        // We test 09:00 AM (Should be OFF_PEAK)
        LocalDateTime offPeakTime = LocalDateTime.of(2023, Month.OCTOBER, 25, 9, 0);

        StartSessionRequestDto request = new StartSessionRequestDto(
                USER_ID, PLATE, ZONE_ID, SPOT_ID, ZoneType.STANDARD,
                false,
                offPeakTime
        );

        // Act
        StartSessionResponseDto response = controller.startSession(request);

        // Assert
        assertEquals(TimeOfDayBand.OFF_PEAK, response.timeBand());
    }

    @Test
    void startSession_shouldDetectWeekend() {
        // Arrange: Oct 28, 2023 is a Saturday
        LocalDateTime saturday = LocalDateTime.of(2023, Month.OCTOBER, 28, 12, 0);

        StartSessionRequestDto request = new StartSessionRequestDto(
                USER_ID, PLATE, ZONE_ID, SPOT_ID, ZoneType.STANDARD,
                false,
                saturday
        );

        // Act
        StartSessionResponseDto response = controller.startSession(request);

        // Assert
        assertEquals(DayType.WEEKEND, response.dayType());
    }

    @Test
    void startSession_shouldPrioritizeHolidayFlag() {
        // Arrange: Use a normal Wednesday, but pass isHoliday = true
        LocalDateTime wednesday = LocalDateTime.of(2023, Month.OCTOBER, 25, 12, 0);

        StartSessionRequestDto request = new StartSessionRequestDto(
                USER_ID, PLATE, ZONE_ID, SPOT_ID, ZoneType.STANDARD,
                true, // <--- Holiday Flag
                wednesday
        );

        // Act
        StartSessionResponseDto response = controller.startSession(request);

        // Assert
        assertEquals(DayType.HOLIDAY, response.dayType());
    }

    // =========================================================================
    // 2. Error Handling Tests
    // =========================================================================

    @Test
    void startSession_whenSpotDoesNotExist_shouldThrowException() {
        // Arrange
        StartSessionRequestDto request = new StartSessionRequestDto(
                USER_ID, PLATE, ZONE_ID, "INVALID-SPOT", ZoneType.STANDARD,
                false,
                LocalDateTime.now()
        );

        // Act & Assert
        Exception e = assertThrows(IllegalStateException.class, () -> controller.startSession(request));
        assertEquals("Spot not found", e.getMessage());
    }

    // =========================================================================
    // 3. Close Session Tests
    // =========================================================================

    @Test
    void closeSession_whenSessionExists_shouldCloseAndPersistEndTime() {
        // Arrange: Manually seed an active session
        String sessionId = "sess-123";
        ParkingSession session = new ParkingSession(
                sessionId, USER_ID, PLATE, ZONE_ID, SPOT_ID,
                TimeOfDayBand.OFF_PEAK, DayType.WEEKDAY, ZoneType.STANDARD, LocalDateTime.now().minusHours(2)
        );
        sessionRepo.save(session);

        LocalDateTime endTime = LocalDateTime.now();

        // Act
        boolean result = controller.closeSession(sessionId, endTime);

        // Assert
        assertTrue(result, "Should return true for successful close");

        ParkingSession updatedSession = sessionRepo.findById(sessionId).orElseThrow();
        assertEquals(SessionState.CLOSED, updatedSession.getState());
        assertEquals(endTime, updatedSession.getEndTime());
    }

    @Test
    void closeSession_whenSessionIdDoesNotExist_shouldReturnFalse() {
        // Act
        boolean result = controller.closeSession("ghost-session-id", LocalDateTime.now());

        // Assert
        assertFalse(result, "Should return false if session not found");
    }
}