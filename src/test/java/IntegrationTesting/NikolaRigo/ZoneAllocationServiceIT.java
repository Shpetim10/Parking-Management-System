package IntegrationTesting.NikolaRigo;

import Enum.*;
import Model.*;
import Repository.ParkingSessionRepository;
import Repository.ParkingZoneRepository;
import Repository.impl.InMemoryParkingSessionRepository;
import Repository.impl.InMemoryParkingZoneRepository;
import Service.impl.ZoneAllocationServiceImpl;
import Service.impl.ZoneOccupancyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ZoneAllocationServiceIT {

    private ZoneAllocationServiceImpl zoneAllocationService;
    private ZoneOccupancyServiceImpl zoneOccupancyService;
    private ParkingZoneRepository zoneRepository;
    private ParkingSessionRepository sessionRepository;

    @BeforeEach
    void setUp() {
        // 1. Initialize Repositories
        zoneRepository = new InMemoryParkingZoneRepository();
        sessionRepository = new InMemoryParkingSessionRepository();

        // 2. Initialize Services
        // Uses the "Logic-Only" impl you provided earlier
        zoneAllocationService = new ZoneAllocationServiceImpl();
        // Uses the "Neighbourhood" impl you provided in Turn 5
        zoneOccupancyService = new ZoneOccupancyServiceImpl(zoneRepository, sessionRepository);

        // 3. Seed Data
        seedData();
    }

    private void seedData() {
        // Create a Standard Zone with 100% capacity
        ParkingZone zone = new ParkingZone("zone-1", ZoneType.STANDARD, 1.0);

        // Add spots
        ParkingSpot s1 = new ParkingSpot("spot-1", zone);
        ParkingSpot s2 = new ParkingSpot("spot-2", zone);
        s2.occupy(); // Set s2 as occupied to ensure we find s1

        zone.addSpot(s1);
        zone.addSpot(s2);

        zoneRepository.save(zone);
    }

    // -------------------------------------------------------------------------
    // HELPER: Create a valid SubscriptionPlan with your new DiscountInfo
    // -------------------------------------------------------------------------
    private SubscriptionPlan createPlan(boolean isVip) {
        // Create a "No Discount" configuration for the test
        DiscountInfo noDiscount = new DiscountInfo(
                BigDecimal.ZERO, // Subscription %
                BigDecimal.ZERO, // Promo %
                BigDecimal.ZERO, // Promo Fixed
                false,           // Has Free Hours
                0                // Free Hours Amount
        );

        return new SubscriptionPlan(
                1,      // maxConcurrentSessions
                1,      // maxConcurrentSessionsPerVehicle
                5,      // maxDailySessions
                10.0,   // maxDailyHours
                false,  // weekdayOnly
                false,  // hasEvRights
                isVip,  // hasVipRights (The key flag we are testing)
                noDiscount
        );
    }

    // -------------------------------------------------------------------------
    // TEST CASES
    // -------------------------------------------------------------------------

    @Test
    void assignSpot_whenFreeSpotAvailable_shouldReturnSpotAndReserveIt() {
        // Arrange
        SubscriptionPlan standardPlan = createPlan(false); // Not VIP

        SpotAssignmentRequest request = new SpotAssignmentRequest(
                "user-1",
                ZoneType.STANDARD,
                standardPlan,
                LocalDateTime.now()
        );

        // Retrieve the zone manually
        ParkingZone zone = zoneRepository.findById("zone-1");

        // Act
        ParkingSpot assignedSpot = zoneAllocationService.assignSpot(request, zone);

        // Assert
        assertNotNull(assignedSpot, "Should find spot-1");
        assertEquals("spot-1", assignedSpot.getSpotId());
        assertEquals(SpotState.RESERVED, assignedSpot.getState());
    }

    @Test
    void assignSpot_whenTypeMismatch_shouldReturnNull() {
        // Arrange: User wants VIP zone but we only pass them a STANDARD zone
        SubscriptionPlan vipPlan = createPlan(true);

        SpotAssignmentRequest request = new SpotAssignmentRequest(
                "user-vip",
                ZoneType.VIP,
                vipPlan,
                LocalDateTime.now()
        );

        ParkingZone standardZone = zoneRepository.findById("zone-1");

        // Act
        ParkingSpot result = zoneAllocationService.assignSpot(request, standardZone);

        // Assert
        assertNull(result, "Should fail because the Zone Type doesn't match the Request");
    }

    @Test
    void calculateOccupancy_shouldReflectActiveSessions() {
        // Arrange: "zone-1" has 2 spots (spot-1, spot-2).
        // We create 1 active session in "zone-1".

        ParkingSession session = new ParkingSession(
                "sess-1",
                "user-x",
                "ABC-123",
                "zone-1",
                "spot-2",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );

        sessionRepository.save(session);

        // Act
        double ratio = zoneOccupancyService.calculateOccupancyRatioForZone("zone-1");

        // Assert
        // 1 active session / 2 total spots = 0.5
        assertEquals(0.5, ratio, 0.001);
    }

    @Test
    void calculateOccupancy_shouldIgnoreCompletedSessions() {
        // Arrange
        String zoneId = "zone-history";
        ParkingZone zone = new ParkingZone(zoneId, ZoneType.STANDARD, 1.0);
        zone.addSpot(new ParkingSpot("spot-1", zone)); // Total spots = 1
        zoneRepository.save(zone);

        // Create a session that is already CLOSED
        ParkingSession oldSession = new ParkingSession(
                "sess-old", "user-1", "OLD-123", zoneId, "spot-1",
                TimeOfDayBand.OFF_PEAK, DayType.WEEKDAY, ZoneType.STANDARD,
                LocalDateTime.now().minusHours(5)
        );
        // Simulate closing the session
        oldSession.close(LocalDateTime.now().minusHours(1));

        sessionRepository.save(oldSession);

        // Act
        double ratio = zoneOccupancyService.calculateOccupancyRatioForZone(zoneId);

        // Assert
        // 1 Spot, 0 Active Sessions (1 closed) -> Ratio should be 0.0
        assertEquals(0.0, ratio, 0.001);
    }

    @Test
    void calculateOccupancy_shouldNotCrossContaminateZones() {
        // Arrange
        // Zone A: Full (1 spot, 1 active session)
        String zoneAId = "zone-A";
        ParkingZone zoneA = new ParkingZone(zoneAId, ZoneType.STANDARD, 1.0);
        zoneA.addSpot(new ParkingSpot("spot-a-1", zoneA));
        zoneRepository.save(zoneA);

        ParkingSession sessionA = new ParkingSession(
                "sess-a", "user-a", "AAA-111", zoneAId, "spot-a-1",
                TimeOfDayBand.PEAK, DayType.WEEKDAY, ZoneType.STANDARD, LocalDateTime.now()
        );
        sessionRepository.save(sessionA);

        // Zone B: Empty (1 spot, 0 sessions)
        String zoneBId = "zone-B";
        ParkingZone zoneB = new ParkingZone(zoneBId, ZoneType.STANDARD, 1.0);
        zoneB.addSpot(new ParkingSpot("spot-b-1", zoneB));
        zoneRepository.save(zoneB);

        // Act
        double ratioA = zoneOccupancyService.calculateOccupancyRatioForZone(zoneAId);
        double ratioB = zoneOccupancyService.calculateOccupancyRatioForZone(zoneBId);

        // Assert
        assertEquals(1.0, ratioA, 0.001, "Zone A should be full");
        assertEquals(0.0, ratioB, 0.001, "Zone B should be empty");
    }

    @Test
    void calculateOccupancy_shouldReturnZero_whenNoSessionsExist() {
        // Arrange
        String zoneId = "zone-empty";
        // FIXED: Threshold set to 0.9 instead of invalid 1.5
        ParkingZone zone = new ParkingZone(zoneId, ZoneType.VIP, 0.9);

        // Add 5 spots
        for (int i = 0; i < 5; i++) {
            zone.addSpot(new ParkingSpot("spot-" + i, zone));
        }
        zoneRepository.save(zone);

        // Act
        double ratio = zoneOccupancyService.calculateOccupancyRatioForZone(zoneId);

        // Assert
        assertEquals(0.0, ratio, 0.001);
    }
}