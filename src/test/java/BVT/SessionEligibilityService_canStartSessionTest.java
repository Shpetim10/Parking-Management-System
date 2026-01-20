package BVT;

import Service.impl.EligibilityServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import Model.User;
import Model.Vehicle;
import Model.SubscriptionPlan;
import Model.DiscountInfo;
import Model.EligibilityResult;
import Enum.UserStatus;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SessionEligibilityService_canStartSessionTest {

    private EligibilityServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EligibilityServiceImpl(); // Adjust to actual class name
    }

    // ============================================================================
    // EQUIVALENCE CLASS (EC) TESTS
    // ============================================================================

    @Test
    void canStartSession_WithAllValidConditions_ShouldAllowSession_EC() {
        // [EC] Valid equivalence class: All conditions satisfied

        // Arrange
        User user = new User("user-1", UserStatus.ACTIVE);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO,  // subscriptionDiscountPercent
                BigDecimal.ZERO,  // promoDiscountPercent
                BigDecimal.ZERO,  // promoDiscountFixed
                false,            // subscriptionHasFreeHours
                0                 // freeHoursPerDay
        );
        SubscriptionPlan plan = new SubscriptionPlan(
                3,      // maxConcurrentSessions
                2,      // maxConcurrentSessionsPerVehicle
                10,     // maxDailySessions
                8.0,    // maxDailyHours
                false,  // weekdayOnly
                false,  // hasEvRights
                false,  // hasVipRights
                discount
        );

        int activeSessionsForVehicle = 0;
        int totalActiveSessionsForUser = 0;
        int sessionsStartedToday = 0;
        double hoursUsedToday = 0.0;
        boolean hasUnpaidSessions = false;
        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0); // Monday

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, activeSessionsForVehicle,
                totalActiveSessionsForUser, sessionsStartedToday,
                hoursUsedToday, plan, hasUnpaidSessions, now
        );

        // Assert
        assertTrue(result.isAllowed());
        assertNull(result.getReason());
    }

    @Test
    void canStartSession_WithInactiveUser_ShouldDeny_EC() {
        // [EC] Invalid equivalence class: User not active

        // Arrange
        User user = new User("user-1", UserStatus.INACTIVE);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(3, 2, 10, 8.0, false, false, false, discount);

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, 0, 0, 0, 0.0, plan, false,
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("USER_NOT_ACTIVE", result.getReason());
    }

    @Test
    void canStartSession_WithSuspendedUser_ShouldDeny_EC() {
        // [EC] Invalid equivalence class: User suspended

        // Arrange
        User user = new User("user-1", UserStatus.BLACKLISTED);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(3, 2, 10, 8.0, false, false, false, discount);

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, 0, 0, 0, 0.0, plan, false,
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("USER_NOT_ACTIVE", result.getReason());
    }

    @Test
    void canStartSession_WithUnpaidSessions_ShouldDeny_EC() {
        // [EC] Invalid equivalence class: Unpaid sessions exist

        // Arrange
        User user = new User("user-1", UserStatus.ACTIVE);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(3, 2, 10, 8.0, false, false, false, discount);
        boolean hasUnpaidSessions = true;

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, 0, 0, 0, 0.0, plan, hasUnpaidSessions,
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("UNPAID_SESSIONS_EXIST", result.getReason());
    }

    @Test
    void canStartSession_WithWeekdayOnlyPlanOnSaturday_ShouldDeny_EC() {
        // [EC] Invalid equivalence class: Weekend day with weekday-only plan

        // Arrange
        User user = new User("user-1", UserStatus.ACTIVE);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(
                3, 2, 10, 8.0,
                true,  // weekdayOnly
                false, false, discount
        );
        LocalDateTime saturday = LocalDateTime.of(2024, 1, 13, 10, 0); // Saturday

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, 0, 0, 0, 0.0, plan, false, saturday
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("WEEKDAY_ONLY_PLAN", result.getReason());
    }

    @Test
    void canStartSession_WithWeekdayOnlyPlanOnSunday_ShouldDeny_EC() {
        // [EC] Invalid equivalence class: Sunday with weekday-only plan

        // Arrange
        User user = new User("user-1", UserStatus.ACTIVE);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(3, 2, 10, 8.0, true, false, false, discount);
        LocalDateTime sunday = LocalDateTime.of(2024, 1, 14, 10, 0); // Sunday

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, 0, 0, 0, 0.0, plan, false, sunday
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("WEEKDAY_ONLY_PLAN", result.getReason());
    }

    // ============================================================================
    // BOUNDARY VALUE TESTING (BVT) TESTS
    // ============================================================================

    @Test
    void canStartSession_WithMaxConcurrentSessionsAtBoundary_ShouldAllow_BVT() {
        // [BVT] Boundary: totalActiveSessionsForUser = maxConcurrentSessions - 1

        // Arrange
        User user = new User("user-1", UserStatus.ACTIVE);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(3, 2, 10, 8.0, false, false, false, discount);
        int totalActiveSessionsForUser = 2; // Just below limit of 3

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, 0, totalActiveSessionsForUser, 0, 0.0, plan, false,
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );

        // Assert
        assertTrue(result.isAllowed());
    }

    @Test
    void canStartSession_WithMaxConcurrentSessionsAtLimit_ShouldDeny_BVT() {
        // [BVT] Boundary: totalActiveSessionsForUser = maxConcurrentSessions (at limit)

        // Arrange
        User user = new User("user-1", UserStatus.ACTIVE);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(3, 2, 10, 8.0, false, false, false, discount);
        int totalActiveSessionsForUser = 3; // At limit

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, 0, totalActiveSessionsForUser, 0, 0.0, plan, false,
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("MAX_CONCURRENT_SESSIONS_REACHED", result.getReason());
    }

    @Test
    void canStartSession_WithMaxConcurrentSessionsAboveLimit_ShouldDeny_BVT() {
        // [BVT] Boundary: totalActiveSessionsForUser > maxConcurrentSessions

        // Arrange
        User user = new User("user-1", UserStatus.ACTIVE);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(3, 2, 10, 8.0, false, false, false, discount);
        int totalActiveSessionsForUser = 4; // Above limit

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, 0, totalActiveSessionsForUser, 0, 0.0, plan, false,
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("MAX_CONCURRENT_SESSIONS_REACHED", result.getReason());
    }

    @Test
    void canStartSession_WithVehicleSessionsAtBoundary_ShouldAllow_BVT() {
        // [BVT] Boundary: activeSessionsForVehicle = maxConcurrentSessionsPerVehicle - 1

        // Arrange
        User user = new User("user-1", UserStatus.ACTIVE);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(3, 2, 10, 8.0, false, false, false, discount);
        int activeSessionsForVehicle = 1; // Just below limit of 2

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, activeSessionsForVehicle, 0, 0, 0.0, plan, false,
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );

        // Assert
        assertTrue(result.isAllowed());
    }

    @Test
    void canStartSession_WithVehicleSessionsAtLimit_ShouldDeny_BVT() {
        // [BVT] Boundary: activeSessionsForVehicle = maxConcurrentSessionsPerVehicle

        // Arrange
        User user = new User("user-1", UserStatus.ACTIVE);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(3, 2, 10, 8.0, false, false, false, discount);
        int activeSessionsForVehicle = 2; // At limit

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, activeSessionsForVehicle, 0, 0, 0.0, plan, false,
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("MAX_SESSIONS_PER_VEHICLE_REACHED", result.getReason());
    }

    @Test
    void canStartSession_WithDailySessionsAtBoundary_ShouldAllow_BVT() {
        // [BVT] Boundary: sessionsStartedToday = maxDailySessions - 1

        // Arrange
        User user = new User("user-1", UserStatus.ACTIVE);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(3, 2, 10, 8.0, false, false, false, discount);
        int sessionsStartedToday = 9; // Just below limit of 10

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, 0, 0, sessionsStartedToday, 0.0, plan, false,
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );

        // Assert
        assertTrue(result.isAllowed());
    }

    @Test
    void canStartSession_WithDailySessionsAtLimit_ShouldDeny_BVT() {
        // [BVT] Boundary: sessionsStartedToday = maxDailySessions

        // Arrange
        User user = new User("user-1", UserStatus.ACTIVE);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(3, 2, 10, 8.0, false, false, false, discount);
        int sessionsStartedToday = 10; // At limit

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, 0, 0, sessionsStartedToday, 0.0, plan, false,
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("DAILY_SESSION_LIMIT_REACHED", result.getReason());
    }

    @Test
    void canStartSession_WithDailyHoursAtBoundary_ShouldAllow_BVT() {
        // [BVT] Boundary: hoursUsedToday = maxDailyHours - 0.1

        // Arrange
        User user = new User("user-1", UserStatus.ACTIVE);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(3, 2, 10, 8.0, false, false, false, discount);
        double hoursUsedToday = 7.9; // Just below limit of 8.0

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, 0, 0, 0, hoursUsedToday, plan, false,
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );

        // Assert
        assertTrue(result.isAllowed());
    }

    @Test
    void canStartSession_WithDailyHoursAtLimit_ShouldDeny_BVT() {
        // [BVT] Boundary: hoursUsedToday = maxDailyHours

        // Arrange
        User user = new User("user-1", UserStatus.ACTIVE);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(3, 2, 10, 8.0, false, false, false, discount);
        double hoursUsedToday = 8.0; // At limit

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, 0, 0, 0, hoursUsedToday, plan, false,
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("DAILY_HOURS_LIMIT_REACHED", result.getReason());
    }

    @Test
    void canStartSession_WithDailyHoursAboveLimit_ShouldDeny_BVT() {
        // [BVT] Boundary: hoursUsedToday > maxDailyHours

        // Arrange
        User user = new User("user-1", UserStatus.ACTIVE);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(3, 2, 10, 8.0, false, false, false, discount);
        double hoursUsedToday = 8.5; // Above limit

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, 0, 0, 0, hoursUsedToday, plan, false,
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("DAILY_HOURS_LIMIT_REACHED", result.getReason());
    }

    @Test
    void canStartSession_WithZeroValues_ShouldAllow_BVT() {
        // [BVT] Boundary: All numeric inputs at zero (minimum valid values)

        // Arrange
        User user = new User("user-1", UserStatus.ACTIVE);
        Vehicle vehicle = new Vehicle("ABC-1234", "user-1");
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(3, 2, 10, 8.0, false, false, false, discount);

        // Act
        EligibilityResult result = service.canStartSession(
                user, vehicle, 0, 0, 0, 0.0, plan, false,
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );

        // Assert
        assertTrue(result.isAllowed());
    }
}