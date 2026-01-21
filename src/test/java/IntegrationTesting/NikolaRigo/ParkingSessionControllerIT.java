package IntegrationTesting.NikolaRigo;

import Controller.ParkingSessionController;
import Dto.Session.StartSessionRequestDto;
import Dto.Session.StartSessionResponseDto;
import Enum.*;
import Model.*;
import Repository.ParkingSessionRepository;
import Repository.ParkingZoneRepository;
import Repository.UserRepository;
import Repository.impl.InMemoryParkingSessionRepository;
import Repository.impl.InMemoryParkingZoneRepository;
import Repository.impl.InMemoryUserRepository;
import Service.DurationCalculator;
import Service.EligibilityService;
import Service.ZoneAllocationService;
import Service.impl.DefaultDurationCalculator;
import Service.impl.EligibilityServiceImpl;
import Service.impl.ZoneAllocationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ParkingSessionControllerIT {

    private ParkingSessionController sessionController;
    private ParkingSessionRepository sessionRepository;
    private UserRepository userRepository;
    private ParkingZoneRepository zoneRepository;

    // Services
    private EligibilityService eligibilityService;
    private ZoneAllocationService zoneAllocationService;
    private DurationCalculator durationCalculator;

    @BeforeEach
    void setUp() {
        // 1. Initialize Repositories
        sessionRepository = new InMemoryParkingSessionRepository();
        userRepository = new InMemoryUserRepository();
        zoneRepository = new InMemoryParkingZoneRepository();

        // 2. Initialize Services
        eligibilityService = new EligibilityServiceImpl();
        zoneAllocationService = new ZoneAllocationServiceImpl();
        durationCalculator = new DefaultDurationCalculator();

        // 3. Initialize Controller (using the 2-arg constructor you defined)
        sessionController = new ParkingSessionController(sessionRepository, zoneRepository);

        // 4. Inject Dependencies manually (via the setters we discussed)
        sessionController.setUserRepository(userRepository);
        sessionController.setEligibilityService(eligibilityService);
        sessionController.setZoneAllocationService(zoneAllocationService);
        sessionController.setDurationCalculator(durationCalculator);

        // 5. Seed Data
        seedData();
    }

    private void seedData() {
        // Create Active User
        userRepository.save(new User("user-active", UserStatus.ACTIVE));

        // Create Standard Zone
        ParkingZone standardZone = new ParkingZone("zone-1", ZoneType.STANDARD, 1.0);

        // Create Spot and link to Zone
        ParkingSpot spot1 = new ParkingSpot("spot-1", standardZone);
        standardZone.addSpot(spot1);

        zoneRepository.save(standardZone);
    }

    // =========================================================================
    // TEST CASES
    // =========================================================================

    @Test
    void startSession_whenValid_shouldSucceed() {
        // Arrange
        String userId = "user-active";
        String vehiclePlate = "ABC-123";
        String zoneId = "zone-1";
        String spotId = "spot-1";
        LocalDateTime now = LocalDateTime.now();

        // Matches your Record: (userId, plate, zoneId, spotId, type, isHoliday, startTime)
        StartSessionRequestDto requestDto = new StartSessionRequestDto(
                userId,
                vehiclePlate,
                zoneId,
                spotId,
                ZoneType.STANDARD,
                false, // isHoliday
                now    // startTime
        );

        // Act
        StartSessionResponseDto response = sessionController.startSession(requestDto);

        // Assert
        assertNotNull(response);
        assertNotNull(response.sessionId());

        // Verify Repository State
        Optional<ParkingSession> session = sessionRepository.findById(response.sessionId());
        assertTrue(session.isPresent());
        assertEquals(userId, session.get().getUserId());
        assertEquals(spotId, session.get().getSpotId());
        assertEquals(SessionState.OPEN, session.get().getState());
    }

    @Test
    void closeSession_shouldCalculateDurationCorrectly() {
        // Arrange: Start a valid session
        LocalDateTime startTime = LocalDateTime.now();

        StartSessionRequestDto requestDto = new StartSessionRequestDto(
                "user-active",
                "ABC-123",
                "zone-1",
                "spot-1",
                ZoneType.STANDARD,
                false, // isHoliday
                startTime
        );

        StartSessionResponseDto response = sessionController.startSession(requestDto);
        String sessionId = response.sessionId();

        // Act: Close the session 125 minutes later
        // We simulate this by passing an exit time relative to the start time we just recorded
        LocalDateTime exitTime = startTime.plusMinutes(125); // 2 hours 5 mins

        sessionController.closeSession(sessionId, exitTime);

        // Assert
        ParkingSession closedSession = sessionRepository.findById(sessionId).get();

        // Verify Duration Calculation (2h 5m -> rounds up to 3 hours)
        long minutes = Duration.between(closedSession.getStartTime(), closedSession.getEndTime()).toMinutes();
        int calculatedHours = (int) ((minutes + 59) / 60);

        assertEquals(3, calculatedHours);
        assertEquals(exitTime, closedSession.getEndTime());
    }
}