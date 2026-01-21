package ECT_Decision_Table;

import Enum.ZoneType;
import Model.*;
import Service.ZoneAllocationService;
import Service.impl.ZoneAllocationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TestZoneAllocationServiceDecisionTable {

    private ZoneAllocationService service;

    @BeforeEach
    void setup() {
        service = new ZoneAllocationServiceImpl();
    }

    @Test
    void testAassignsSpotInStandardZone() {
        ParkingZone zone = new ParkingZone("Z1", ZoneType.STANDARD, 1.0);
        zone.addSpot(new ParkingSpot("S1", zone));

        SpotAssignmentRequest request =
                new SpotAssignmentRequest(
                        "U1",
                        ZoneType.STANDARD,
                        standardPlan(),
                        LocalDateTime.now()
                );

        ParkingSpot spot = service.assignSpot(request, zone);

        assertNotNull(spot);
        assertEquals("S1", spot.getSpotId());
    }

    @Test
    void testDeniesEvZoneWithoutEvRights() {
        ParkingZone zone = new ParkingZone("Z1", ZoneType.EV, 1.0);
        zone.addSpot(new ParkingSpot("S1", zone));

        SpotAssignmentRequest request =
                new SpotAssignmentRequest(
                        "U1",
                        ZoneType.EV,
                        standardPlan(), // no EV rights
                        LocalDateTime.now()
                );

        assertNull(service.assignSpot(request, zone));
    }

    @Test
    void testAssignsEvZoneWithEvRights() {
        ParkingZone zone = new ParkingZone("Z1", ZoneType.EV, 1.0);
        zone.addSpot(new ParkingSpot("S1", zone));

        SpotAssignmentRequest request =
                new SpotAssignmentRequest(
                        "U1",
                        ZoneType.EV,
                        evPlan(), //  EV rights
                        LocalDateTime.now()
                );

        assertNotNull(service.assignSpot(request, zone));
    }

    // helpers

    private SubscriptionPlan standardPlan() {
        return new SubscriptionPlan(
                1, 1, 5, 8.0,
                false,
                false,
                false,
                new DiscountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0)
        );
    }

    private SubscriptionPlan evPlan() {
        return new SubscriptionPlan(
                1, 1, 5, 8.0,
                false,
                true,
                false,
                new DiscountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0)
        );
    }
}