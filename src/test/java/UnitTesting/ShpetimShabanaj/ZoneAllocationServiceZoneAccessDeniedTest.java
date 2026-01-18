package UnitTesting.ShpetimShabanaj;

import Model.ParkingSpot;
import Model.ParkingZone;
import Model.SpotAssignmentRequest;
import Model.SubscriptionPlan;
import Enum.ZoneType;
import Service.impl.ZoneAllocationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ZoneAllocationServiceZoneAccessDeniedTest {
    private ZoneAllocationServiceImpl allocationService;
    private SpotAssignmentRequest mockRequest;
    private SubscriptionPlan mockPlan;

    @BeforeEach
    void setUp() {
        allocationService = new ZoneAllocationServiceImpl();
        mockRequest = mock(SpotAssignmentRequest.class);
        mockPlan = mock(SubscriptionPlan.class);

        when(mockRequest.getSubscriptionPlan()).thenReturn(mockPlan);
    }

    @Test
    @DisplayName("TC-01: Should deny access to EV zone if user lacks EV rights(Deny true)")
    void testEvAccessDenied() {
        when(mockPlan.hasEvRights()).thenReturn(false);

        assertTrue(allocationService.zoneAccessDenied(
                mockRequest,ZoneType.EV
        ));
    }

    @Test
    @DisplayName("TC-02: Should grant access to EV zone if user has EV rights(Deny false)")
    void testEvAccessGranted() {
        when(mockPlan.hasEvRights()).thenReturn(true);

        assertFalse(allocationService.zoneAccessDenied(
                mockRequest,ZoneType.EV
        ));
    }

    @Test
    @DisplayName("TC-03: Should deny access to VIP zone if user lacks VIP rights")
    void testVipAccessDenied() {
        when(mockPlan.hasVipRights()).thenReturn(false);
        when(mockPlan.hasEvRights()).thenReturn(true);

        assertTrue(allocationService.zoneAccessDenied(
                mockRequest,ZoneType.VIP
        ));
    }

    @Test
    @DisplayName("TC-04: Should grant access to VIP zone if user has VIP rights(Deny false)")
    void testVipAccessGranted() {
        when(mockPlan.hasVipRights()).thenReturn(true);
        when(mockPlan.hasEvRights()).thenReturn(true);

        assertFalse(allocationService.zoneAccessDenied(
                mockRequest,ZoneType.VIP
        ));
    }

    @Test
    @DisplayName("TC-05: Should grant access to STANDARD zone regardless of special rights")
    void testStandardAccess() {
        when(mockPlan.hasVipRights()).thenReturn(true);
        when(mockPlan.hasEvRights()).thenReturn(true);

        assertFalse(allocationService.zoneAccessDenied(
                mockRequest,ZoneType.STANDARD
        ));
    }

    @Test
    @DisplayName("TC-06: Should fail for a null request")
    void testForNullRequest() {
        when(mockPlan.hasVipRights()).thenReturn(true);
        when(mockPlan.hasEvRights()).thenReturn(true);

        assertThrows(NullPointerException.class, ()-> allocationService.zoneAccessDenied(null,ZoneType.VIP));
    }
}
