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

/**
 * Decision Conditions:
 * C1: Requested Zone Type (STANDARD / EV)
 * C2: Subscription allows EV parking
 * C3: Free parking spot exists in zone
 *
 * Actions:
 * A1: Assign parking spot
 * A2: Deny assignment (return null)
 */
public class TestZoneAllocationServiceDecisionTable {

    private ZoneAllocationService service;

    @BeforeEach
    void setup() {
        service = new ZoneAllocationServiceImpl();
    }

    /**
     * Rule R1:
     * Zone = STANDARD
     * EV Rights = N/A
     * Free Spot = YES
     * → Spot is assigned
     */
    @Test
    void assignsSpotInStandardZone_WhenSpotIsAvailable() {
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

        assertNotNull(spot, "Spot should be assigned in STANDARD zone");
        assertEquals("S1", spot.getSpotId());
    }

    /**
     * Rule R2:
     * Zone = EV
     * EV Rights = NO
     * Free Spot = YES
     * → Assignment denied
     */
    @Test
    void deniesEvZone_WhenUserHasNoEvRights() {
        ParkingZone zone = new ParkingZone("Z1", ZoneType.EV, 1.0);
        zone.addSpot(new ParkingSpot("S1", zone));

        SpotAssignmentRequest request =
                new SpotAssignmentRequest(
                        "U1",
                        ZoneType.EV,
                        standardPlan(), // no EV rights
                        LocalDateTime.now()
                );

        ParkingSpot spot = service.assignSpot(request, zone);

        assertNull(
                spot,
                "Spot must not be assigned in EV zone without EV subscription"
        );
    }

    /**
     * Rule R3:
     * Zone = EV
     * EV Rights = YES
     * Free Spot = YES
     * -> Spot is assigned
     */
    @Test
    void assignsEvZone_WhenUserHasEvRights() {
        ParkingZone zone = new ParkingZone("Z1", ZoneType.EV, 1.0);
        zone.addSpot(new ParkingSpot("S1", zone));

        SpotAssignmentRequest request =
                new SpotAssignmentRequest(
                        "U1",
                        ZoneType.EV,
                        evPlan(), // EV rights enabled
                        LocalDateTime.now()
                );

        ParkingSpot spot = service.assignSpot(request, zone);

        assertNotNull(
                spot,
                "Spot should be assigned in EV zone for EV-enabled subscription"
        );
    }

    /**
     * Rule R4:
     * Zone = ANY
     * EV Rights = ANY
     * Free Spot = NO
     * -> Assignment denied
     */
    @Test
    void deniesAssignment_WhenNoFreeSpotsExist() {
        ParkingZone zone = new ParkingZone("Z1", ZoneType.STANDARD, 1.0);
        // No spots added → no free spots

        SpotAssignmentRequest request =
                new SpotAssignmentRequest(
                        "U1",
                        ZoneType.STANDARD,
                        standardPlan(),
                        LocalDateTime.now()
                );

        ParkingSpot spot = service.assignSpot(request, zone);

        assertNull(
                spot,
                "Spot assignment must fail when no free spots exist"
        );
    }



    // Helpers

    private SubscriptionPlan standardPlan() {
        return new SubscriptionPlan(
                1, 1, 5, 8.0,
                false,
                false,
                false,
                new DiscountInfo(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        false,
                        0
                )
        );
    }

    private SubscriptionPlan evPlan() {
        return new SubscriptionPlan(
                1, 1, 5, 8.0,
                false,
                true,
                false,
                new DiscountInfo(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        false,
                        0
                )
        );
    }
}