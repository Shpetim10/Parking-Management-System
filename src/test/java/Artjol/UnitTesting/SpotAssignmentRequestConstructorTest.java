package Artjol.UnitTesting;

import Model.SpotAssignmentRequest;
import Model.SubscriptionPlan;
import Enum.ZoneType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;

// Unit Tests for M-30: SpotAssignmentRequest.constructor

class SpotAssignmentRequestConstructorTest {

    @Mock
    private SubscriptionPlan mockPlan;

    @Test
    @DisplayName("Constructor with valid parameters")
    void testConstructor_ValidParameters() {
        MockitoAnnotations.openMocks(this);
        LocalDateTime requestTime = LocalDateTime.now();

        SpotAssignmentRequest request = new SpotAssignmentRequest(
                "user-1",
                ZoneType.STANDARD,
                mockPlan,
                requestTime
        );

        assertEquals("user-1", request.getUserId());
        assertEquals(ZoneType.STANDARD, request.getRequestedZoneType());
        assertEquals(mockPlan, request.getSubscriptionPlan());
        assertEquals(requestTime, request.getRequestedStartTime());
    }


    //I left only this one for null tests cause the other: UserId, ZoneType, RequestedStartTime have the same behavior
    @Test
    @DisplayName("Constructor with null subscription plan")
    void testConstructor_NullSubscriptionPlan() {
        SpotAssignmentRequest request = new SpotAssignmentRequest(
                "user-1",
                ZoneType.STANDARD,
                null,
                LocalDateTime.now()
        );

        assertNull(request.getSubscriptionPlan());
    }



    @Test
    @DisplayName("Constructor creates distinct instances")
    void testConstructor_DistinctInstances() {
        MockitoAnnotations.openMocks(this);
        LocalDateTime now = LocalDateTime.now();

        SpotAssignmentRequest request1 = new SpotAssignmentRequest(
                "user-1",
                ZoneType.STANDARD,
                mockPlan,
                now
        );

        SpotAssignmentRequest request2 = new SpotAssignmentRequest(
                "user-1",
                ZoneType.STANDARD,
                mockPlan,
                now
        );

        assertNotSame(request1, request2);
    }

    @Test
    @DisplayName("Constructor with empty userId string")
    void testConstructor_EmptyUserId() {
        MockitoAnnotations.openMocks(this);

        SpotAssignmentRequest request = new SpotAssignmentRequest(
                "",
                ZoneType.STANDARD,
                mockPlan,
                LocalDateTime.now()
        );

        assertEquals("", request.getUserId());
    }


}