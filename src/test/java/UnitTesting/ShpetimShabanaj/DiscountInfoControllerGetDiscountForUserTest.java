package UnitTesting.ShpetimShabanaj;

import Controller.DiscountInfoController;
import Dto.DiscountInfo.DiscountInfoDto;
import Model.DiscountInfo;
import Model.SubscriptionPlan;
import Repository.DiscountPolicyRepository;
import Repository.SubscriptionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DiscountInfoControllerGetDiscountForUserTest {

    private DiscountPolicyRepository repository;
    private SubscriptionPlanRepository subscriptionPlanRepository;
    private DiscountInfoController controller;

    @BeforeEach
    void setUp() {
        repository = mock(DiscountPolicyRepository.class);
        subscriptionPlanRepository = mock(SubscriptionPlanRepository.class);
        controller = new DiscountInfoController(subscriptionPlanRepository);
    }

    @Test
    @DisplayName("TC-01: Should return mapped DiscountInfoDto when plan and discount info are present")
    void testGetDiscountForUserHappyPath() {
        String userId = "U1";

        // Mock DiscountInfo
        DiscountInfo discountInfoMock = mock(DiscountInfo.class);
        when(discountInfoMock.getSubscriptionDiscountPercent()).thenReturn(new BigDecimal("0.20"));
        when(discountInfoMock.getPromoDiscountPercent()).thenReturn(new BigDecimal("0.00"));
        when(discountInfoMock.getPromoDiscountFixed()).thenReturn(new BigDecimal("0.00"));
        when(discountInfoMock.isSubscriptionHasFreeHours()).thenReturn(true);
        when(discountInfoMock.getFreeHoursPerDay()).thenReturn(2);

        SubscriptionPlan plan = new SubscriptionPlan(
                1,                  // maxConcurrentSessions
                1,                  // maxConcurrentSessionsPerVehicle
                5,                  // maxDailySessions
                8.0,                // maxDailyHours
                false,              // weekdayOnly
                false,              // hasEvRights
                false,              // hasVipRights
                discountInfoMock    // discountInfo
        );

        when(subscriptionPlanRepository.getPlanForUser(userId))
                .thenReturn(Optional.of(plan));

        DiscountInfoDto result = controller.getDiscountForUser(userId);

        assertNotNull(result);
        assertAll("Verify all fields are mapped correctly",
                () -> assertEquals(new BigDecimal("0.20"), result.subscriptionDiscountPercent()),
                () -> assertEquals(new BigDecimal("0.00"), result.promoDiscountPercent()),
                () -> assertEquals(new BigDecimal("0.00"), result.promoDiscountFixed()),
                () -> assertTrue(result.subscriptionHasFreeHours()),
                () -> assertEquals(2, result.freeHoursPerDay())
        );
    }

    @Test
    @DisplayName("TC-02: Should throw NullPointerException when userId is null")
    void testGetDiscountWithNullUserId() {
        NullPointerException ex = assertThrows(NullPointerException.class, () ->
                controller.getDiscountForUser(null)
        );
        assertEquals("userId must not be null", ex.getMessage());
    }

    @Test
    @DisplayName("TC-03: Should throw IllegalArgumentException when user is not subscribed to any plan")
    void testGetDiscountWhenUserNotSubscribedToAnyPlan() {
        String userId = "unknown-user";

        when(subscriptionPlanRepository.getPlanForUser(userId))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                controller.getDiscountForUser(userId)
        );
        assertEquals("User not subscribed to any plan!", ex.getMessage());
    }

    @Test
    @DisplayName("TC-04: Should throw NullPointerException when plan has null discountInfo")
    void testGetDiscountWhenPlanHasNullDiscountInfo() {
        String userId = "U2";

        DiscountInfo someDiscountInfo = mock(DiscountInfo.class);
        SubscriptionPlan plan = new SubscriptionPlan(
                1,
                1,
                5,
                8.0,
                false,
                false,
                false,
                someDiscountInfo
        );

        plan.discountInfo = null;

        when(subscriptionPlanRepository.getPlanForUser(userId))
                .thenReturn(Optional.of(plan));

        NullPointerException ex = assertThrows(NullPointerException.class, () ->
                controller.getDiscountForUser(userId)
        );
        assertEquals("discountInfo must not be null", ex.getMessage());
    }
}
