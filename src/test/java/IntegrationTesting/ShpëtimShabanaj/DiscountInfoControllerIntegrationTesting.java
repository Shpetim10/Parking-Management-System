package IntegrationTesting.ShpëtimShabanaj;

import Controller.DiscountInfoController;
import Dto.DiscountInfo.DiscountInfoDto;
import Model.DiscountInfo;
import Model.SubscriptionPlan;
import Repository.impl.InMemorySubscriptionPlanRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Testing: DiscountInfoController -- InMemorySubscriptionPlanRepository")
class DiscountInfoControllerIntegrationTesting {

    private DiscountInfoController discountInfoController;
    private InMemorySubscriptionPlanRepository subscriptionPlanRepository;

    @BeforeEach
    void setUp() {
        subscriptionPlanRepository = new InMemorySubscriptionPlanRepository();
        discountInfoController = new DiscountInfoController(subscriptionPlanRepository);
    }

    // IT-01: Happy path - get discount for subscribed user
    @Test
    @DisplayName("IT-01: Should return discount info DTO for subscribed user")
    void testGetDiscountForUserSubscribedUserReturnsDto() {
        DiscountInfo discountInfo = new DiscountInfo(
                BigDecimal.valueOf(0.20),
                BigDecimal.valueOf(0.10),
                BigDecimal.valueOf(5.00),
                true,
                2
        );

        SubscriptionPlan plan = new SubscriptionPlan(
                2,
                1,
                5,
                8.0,
                false,
                false,
                false,
                discountInfo
        );

        subscriptionPlanRepository.save("U1", plan);

        DiscountInfoDto dto = discountInfoController.getDiscountForUser("U1");

        assertAll(
                () -> assertEquals(0, BigDecimal.valueOf(0.20).compareTo(dto.subscriptionDiscountPercent())),
                () -> assertEquals(0, BigDecimal.valueOf(0.10).compareTo(dto.promoDiscountPercent())),
                () -> assertEquals(0, BigDecimal.valueOf(5.00).compareTo(dto.promoDiscountFixed())),
                () -> assertTrue(dto.subscriptionHasFreeHours()),
                () -> assertEquals(2, dto.freeHoursPerDay())
        );
    }

    // IT-02: User not subscribed to any plan
    @Test
    @DisplayName("IT-02: Should throw when user has no subscription plan")
    void testGetDiscountForUserUserWithoutPlanThrows() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> discountInfoController.getDiscountForUser("unknown-user")
        );

        assertEquals("User not subscribed to any plan!", ex.getMessage());
    }

    // IT-03: Null userId for getDiscountForUser
    @Test
    @DisplayName("IT-03: Should throw NullPointerException for null userId in getDiscountForUser")
    void testGetDiscountForUserNullUserIdThrowsNPE() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> discountInfoController.getDiscountForUser(null)
        );

        assertEquals("userId must not be null", ex.getMessage());
    }

    // IT-04: Plan exists but discountInfo is null
    @Test
    @DisplayName("IT-04: Should throw when plan has null discountInfo")
    void testGetDiscountForUserNullDiscountInfoThrows() {
        assertThrows(NullPointerException.class, () -> subscriptionPlanRepository.save("U2", new SubscriptionPlan(
                1,
                1,
                3,
                4.0,
                false,
                false,
                false,
                null
        )));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> discountInfoController.getDiscountForUser("U2")
        );

        assertEquals("User not subscribed to any plan!", ex.getMessage());
    }

    // IT-05: saveDiscountForUser updates plan's discount info and persists it
    @Test
    @DisplayName("IT-05: Should save and persist discount info for existing user")
    void testSaveDiscountForUserUpdatesPlanInRepository() {
        DiscountInfo initialDiscount = new DiscountInfo(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        SubscriptionPlan plan = new SubscriptionPlan(
                2,
                1,
                5,
                8.0,
                false,
                false,
                false,
                initialDiscount
        );

        subscriptionPlanRepository.save("U3", plan);

        DiscountInfoDto dto = new DiscountInfoDto(
                BigDecimal.valueOf(0.15),
                BigDecimal.valueOf(0.05),
                BigDecimal.valueOf(3.00),
                true,
                1
        );

        discountInfoController.saveDiscountForUser("U3", dto);

        SubscriptionPlan updatedPlan = subscriptionPlanRepository
                .getPlanForUser("U3")
                .orElseThrow();

        DiscountInfo updatedInfo = updatedPlan.discountInfo;

        assertAll(
                () -> assertEquals(0, BigDecimal.valueOf(0.15).compareTo(updatedInfo.getSubscriptionDiscountPercent())),
                () -> assertEquals(0, BigDecimal.valueOf(0.05).compareTo(updatedInfo.getPromoDiscountPercent())),
                () -> assertEquals(0, BigDecimal.valueOf(3.00).compareTo(updatedInfo.getPromoDiscountFixed())),
                () -> assertTrue(updatedInfo.isSubscriptionHasFreeHours()),
                () -> assertEquals(1, updatedInfo.getFreeHoursPerDay())
        );
    }

    // IT-06: Null userId in saveDiscountForUser
    @Test
    @DisplayName("IT-06: Should throw NullPointerException for null userId in saveDiscountForUser")
    void testSaveDiscountForUserNullUserIdThrowsNPE() {
        DiscountInfoDto dto = new DiscountInfoDto(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> discountInfoController.saveDiscountForUser(null, dto)
        );

        assertEquals("userId must not be null", ex.getMessage());
    }

    // IT-07: Null dto in saveDiscountForUser
    @Test
    @DisplayName("IT-07: Should throw NullPointerException for null dto in saveDiscountForUser")
    void testSaveDiscountForUser_NullDto_ThrowsNPE() {
        assertThrows(
                NullPointerException.class,
                () -> discountInfoController.saveDiscountForUser("user-unknown", null)
        );
    }

    // IT-07 (fixed): Null dto in saveDiscountForUser with existing user
    @Test
    @DisplayName("IT-07: Should throw NullPointerException for null dto when user exists")
    void testSaveDiscountForUser_ExistingUserNullDto_ThrowsNPE() {
        DiscountInfo discountInfo = new DiscountInfo(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        SubscriptionPlan plan = new SubscriptionPlan(
                1,
                1,
                1,
                1.0,
                false,
                false,
                false,
                discountInfo
        );

        subscriptionPlanRepository.save("U4", plan);

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> discountInfoController.saveDiscountForUser("U4", null)
        );

        assertEquals("dto must not be null", ex.getMessage());
    }

    // IT-08: saveDiscountForUser for non-subscribed user
    @Test
    @DisplayName("IT-08: Should throw when saving discount for user without plan")
    void testSaveDiscountForUser_UserWithoutPlan_Throws() {
        DiscountInfoDto dto = new DiscountInfoDto(
                BigDecimal.valueOf(0.10),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> discountInfoController.saveDiscountForUser("no-plan-user", dto)
        );

        assertEquals("User not subscribed to any plan!", ex.getMessage());
    }
}
