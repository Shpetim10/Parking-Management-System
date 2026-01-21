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
}