package UnitTesting.ArtjolZaimi;

import Enum.ZoneType;
import Model.*;
import Service.ZoneAllocationService;
import Service.impl.ZoneAllocationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Unit Tests for M-98: ZoneAllocationService.assignSpot

class ZoneAllocationServiceAssignSpotTest {

    private ZoneAllocationService service;

    @BeforeEach
    void setUp() {
        service = new ZoneAllocationServiceImpl();
    }

    @Test
    @DisplayName("assigns spot in regular zone successfully")
    void testAssignSpot_RegularZone() {
        ParkingZone zone = new ParkingZone("zone-1", ZoneType.STANDARD, 0.8);
        ParkingSpot spot = new ParkingSpot("spot-1", zone);
        zone.addSpot(spot);

        SubscriptionPlan plan = new SubscriptionPlan(
                1, 1, 5, 8.0, false, false, false,
                new DiscountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0)
        );
        SpotAssignmentRequest request = new SpotAssignmentRequest(
                "user-1", ZoneType.STANDARD, plan, LocalDateTime.now()
        );

        ParkingSpot assigned = service.assignSpot(request, zone);

        assertNotNull(assigned);
        assertEquals("spot-1", assigned.getSpotId());
    }

    @Test
    @DisplayName("returns null when zone type mismatch")
    void testAssignSpot_ZoneTypeMismatch() {
        ParkingZone zone = new ParkingZone("zone-1", ZoneType.STANDARD, 0.8);
        ParkingSpot spot = new ParkingSpot("spot-1", zone);
        zone.addSpot(spot);

        SubscriptionPlan plan = new SubscriptionPlan(
                1, 1, 5, 8.0, false, false, false,
                new DiscountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0)
        );
        SpotAssignmentRequest request = new SpotAssignmentRequest(
                "user-1", ZoneType.VIP, plan, LocalDateTime.now()
        );

        ParkingSpot assigned = service.assignSpot(request, zone);

        assertNull(assigned);
    }

    @Test
    @DisplayName("returns null when no free spot")
    void testAssignSpot_NoFreeSpot() {
        ParkingZone zone = new ParkingZone("zone-1", ZoneType.STANDARD, 0.8);
        ParkingSpot spot = new ParkingSpot("spot-1", zone);
        spot.occupy();
        zone.addSpot(spot);

        SubscriptionPlan plan = new SubscriptionPlan(
                1, 1, 5, 8.0, false, false, false,
                new DiscountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0)
        );
        SpotAssignmentRequest request = new SpotAssignmentRequest(
                "user-1", ZoneType.STANDARD, plan, LocalDateTime.now()
        );

        ParkingSpot assigned = service.assignSpot(request, zone);

        assertNull(assigned);
    }

    @Test
    @DisplayName("throws exception for null request")
    void testAssignSpot_NullRequest() {
        ParkingZone zone = new ParkingZone("zone-1", ZoneType.STANDARD, 0.8);

        assertThrows(NullPointerException.class, () -> {
            service.assignSpot(null, zone);
        });
    }
}
