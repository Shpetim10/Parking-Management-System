package UnitTesting.NikolaRigo;

import Enum.UserStatus;
import Model.*;
import Service.impl.EligibilityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EligibilityServiceImplTest_canStartSession {

    private EligibilityServiceImpl service;
    private User mockUser;
    private Vehicle mockVehicle;
    private SubscriptionPlan plan;
    private DiscountInfo mockDiscountInfo;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        service = new EligibilityServiceImpl();
        mockUser = mock(User.class);
        mockVehicle = mock(Vehicle.class);
        mockDiscountInfo = mock(DiscountInfo.class);

        // Default setup for a weekday
        testDateTime = LocalDateTime.of(2024, 1, 15, 10, 0); // Monday

        // Create real SubscriptionPlan with default values
        plan = new SubscriptionPlan(
                5,                  // maxConcurrentSessions
                1,                  // maxConcurrentSessionsPerVehicle
                10,                 // maxDailySessions
                12.0,               // maxDailyHours
                false,              // weekdayOnly
                false,              // hasEvRights
                false,              // hasVipRights
                mockDiscountInfo    // discountInfo
        );

        // Default mock behaviors for successful eligibility
        when(mockUser.getStatus()).thenReturn(UserStatus.ACTIVE);
    }

    @Test
    void withAllValidConditions_ShouldAllowSession() {
        // Arrange
        int activeSessionsForVehicle = 0;
        int totalActiveSessionsForUser = 0;
        int sessionsStartedToday = 0;
        double hoursUsedToday = 0.0;
        boolean hasUnpaidSessions = false;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                plan,
                hasUnpaidSessions,
                testDateTime
        );

        // Assert
        assertTrue(result.isAllowed());
        assertNull(result.getReason());
        verify(mockUser).getStatus();
    }

    @Test
    void withInactiveUser_ShouldDenyWithUserNotActiveReason() {
        // Arrange
        when(mockUser.getStatus()).thenReturn(UserStatus.INACTIVE);

        int activeSessionsForVehicle = 0;
        int totalActiveSessionsForUser = 0;
        int sessionsStartedToday = 0;
        double hoursUsedToday = 0.0;
        boolean hasUnpaidSessions = false;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                plan,
                hasUnpaidSessions,
                testDateTime
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("USER_NOT_ACTIVE", result.getReason());
        verify(mockUser).getStatus();
    }

    @Test
    void withBlacklistedUser_ShouldDenyWithUserNotActiveReason() {
        // Arrange
        when(mockUser.getStatus()).thenReturn(UserStatus.BLACKLISTED);

        int activeSessionsForVehicle = 0;
        int totalActiveSessionsForUser = 0;
        int sessionsStartedToday = 0;
        double hoursUsedToday = 0.0;
        boolean hasUnpaidSessions = false;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                plan,
                hasUnpaidSessions,
                testDateTime
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("USER_NOT_ACTIVE", result.getReason());
    }

    @Test
    void withUnpaidSessions_ShouldDenyWithUnpaidSessionsReason() {
        // Arrange
        int activeSessionsForVehicle = 0;
        int totalActiveSessionsForUser = 0;
        int sessionsStartedToday = 0;
        double hoursUsedToday = 0.0;
        boolean hasUnpaidSessions = true;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                plan,
                hasUnpaidSessions,
                testDateTime
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("UNPAID_SESSIONS_EXIST", result.getReason());
    }

    @Test
    void withMaxConcurrentSessionsReached_ShouldDenyWithMaxConcurrentReason() {
        // Arrange
        SubscriptionPlan customPlan = new SubscriptionPlan(
                3, 1, 10, 12.0, false, false, false, mockDiscountInfo
        );

        int activeSessionsForVehicle = 0;
        int totalActiveSessionsForUser = 3; // Already at max
        int sessionsStartedToday = 0;
        double hoursUsedToday = 0.0;
        boolean hasUnpaidSessions = false;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                customPlan,
                hasUnpaidSessions,
                testDateTime
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("MAX_CONCURRENT_SESSIONS_REACHED", result.getReason());
    }

    @Test
    void withMaxSessionsPerVehicleReached_ShouldDenyWithMaxPerVehicleReason() {
        // Arrange
        int activeSessionsForVehicle = 1; // Already at max for this vehicle
        int totalActiveSessionsForUser = 1;
        int sessionsStartedToday = 0;
        double hoursUsedToday = 0.0;
        boolean hasUnpaidSessions = false;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                plan,
                hasUnpaidSessions,
                testDateTime
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("MAX_SESSIONS_PER_VEHICLE_REACHED", result.getReason());
    }

    @Test
    void withDailySessionLimitReached_ShouldDenyWithDailyLimitReason() {
        // Arrange
        SubscriptionPlan customPlan = new SubscriptionPlan(
                5, 1, 5, 12.0, false, false, false, mockDiscountInfo
        );

        int activeSessionsForVehicle = 0;
        int totalActiveSessionsForUser = 0;
        int sessionsStartedToday = 5; // Already at daily limit
        double hoursUsedToday = 0.0;
        boolean hasUnpaidSessions = false;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                customPlan,
                hasUnpaidSessions,
                testDateTime
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("DAILY_SESSION_LIMIT_REACHED", result.getReason());
    }

    @Test
    void withDailyHoursLimitReached_ShouldDenyWithDailyHoursReason() {
        // Arrange
        SubscriptionPlan customPlan = new SubscriptionPlan(
                5, 1, 10, 8.0, false, false, false, mockDiscountInfo
        );

        int activeSessionsForVehicle = 0;
        int totalActiveSessionsForUser = 0;
        int sessionsStartedToday = 2;
        double hoursUsedToday = 8.0; // Already at daily hours limit
        boolean hasUnpaidSessions = false;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                customPlan,
                hasUnpaidSessions,
                testDateTime
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("DAILY_HOURS_LIMIT_REACHED", result.getReason());
    }

    @Test
    void withWeekdayOnlyPlanOnSaturday_ShouldDenyWithWeekdayOnlyReason() {
        // Arrange
        SubscriptionPlan weekdayPlan = new SubscriptionPlan(
                5, 1, 10, 12.0, true, false, false, mockDiscountInfo
        );
        LocalDateTime saturday = LocalDateTime.of(2024, 1, 13, 10, 0); // Saturday

        int activeSessionsForVehicle = 0;
        int totalActiveSessionsForUser = 0;
        int sessionsStartedToday = 0;
        double hoursUsedToday = 0.0;
        boolean hasUnpaidSessions = false;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                weekdayPlan,
                hasUnpaidSessions,
                saturday
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("WEEKDAY_ONLY_PLAN", result.getReason());
    }

    @Test
    void withWeekdayOnlyPlanOnSunday_ShouldDenyWithWeekdayOnlyReason() {
        // Arrange
        SubscriptionPlan weekdayPlan = new SubscriptionPlan(
                5, 1, 10, 12.0, true, false, false, mockDiscountInfo
        );
        LocalDateTime sunday = LocalDateTime.of(2024, 1, 14, 10, 0); // Sunday

        int activeSessionsForVehicle = 0;
        int totalActiveSessionsForUser = 0;
        int sessionsStartedToday = 0;
        double hoursUsedToday = 0.0;
        boolean hasUnpaidSessions = false;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                weekdayPlan,
                hasUnpaidSessions,
                sunday
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("WEEKDAY_ONLY_PLAN", result.getReason());
    }

    @Test
    void withWeekdayOnlyPlanOnWeekday_ShouldAllowSession() {
        // Arrange
        SubscriptionPlan weekdayPlan = new SubscriptionPlan(
                5, 1, 10, 12.0, true, false, false, mockDiscountInfo
        );
        LocalDateTime wednesday = LocalDateTime.of(2024, 1, 17, 10, 0); // Wednesday

        int activeSessionsForVehicle = 0;
        int totalActiveSessionsForUser = 0;
        int sessionsStartedToday = 0;
        double hoursUsedToday = 0.0;
        boolean hasUnpaidSessions = false;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                weekdayPlan,
                hasUnpaidSessions,
                wednesday
        );

        // Assert
        assertTrue(result.isAllowed());
        assertNull(result.getReason());
    }

    @Test
    void withBoundaryValues_JustBelowLimits_ShouldAllowSession() {
        // Arrange
        SubscriptionPlan customPlan = new SubscriptionPlan(
                5, 2, 10, 12.0, false, false, false, mockDiscountInfo
        );

        int activeSessionsForVehicle = 1; // Below max of 2
        int totalActiveSessionsForUser = 4; // Below max of 5
        int sessionsStartedToday = 9; // Below max of 10
        double hoursUsedToday = 11.5; // Below max of 12.0
        boolean hasUnpaidSessions = false;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                customPlan,
                hasUnpaidSessions,
                testDateTime
        );

        // Assert
        assertTrue(result.isAllowed());
        assertNull(result.getReason());
    }

    @Test
    void withBoundaryValues_AtLimits_ShouldDenySession() {
        // Arrange
        SubscriptionPlan customPlan = new SubscriptionPlan(
                5, 1, 10, 12.0, false, false, false, mockDiscountInfo
        );

        int activeSessionsForVehicle = 0;
        int totalActiveSessionsForUser = 5; // At max
        int sessionsStartedToday = 0;
        double hoursUsedToday = 0.0;
        boolean hasUnpaidSessions = false;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                customPlan,
                hasUnpaidSessions,
                testDateTime
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("MAX_CONCURRENT_SESSIONS_REACHED", result.getReason());
    }

    @Test
    void withZeroValues_ShouldAllowSession() {
        // Arrange
        int activeSessionsForVehicle = 0;
        int totalActiveSessionsForUser = 0;
        int sessionsStartedToday = 0;
        double hoursUsedToday = 0.0;
        boolean hasUnpaidSessions = false;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                plan,
                hasUnpaidSessions,
                testDateTime
        );

        // Assert
        assertTrue(result.isAllowed());
        assertNull(result.getReason());
    }

    @Test
    void withMultipleViolations_ShouldReturnFirstViolation() {
        // Arrange
        when(mockUser.getStatus()).thenReturn(UserStatus.INACTIVE);
        SubscriptionPlan customPlan = new SubscriptionPlan(
                1, 1, 10, 12.0, false, false, false, mockDiscountInfo
        );
        boolean hasUnpaidSessions = true;

        int activeSessionsForVehicle = 0;
        int totalActiveSessionsForUser = 5;
        int sessionsStartedToday = 0;
        double hoursUsedToday = 0.0;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                customPlan,
                hasUnpaidSessions,
                testDateTime
        );

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("USER_NOT_ACTIVE", result.getReason());
    }

    @Test
    void onFriday_WithWeekdayOnlyPlan_ShouldAllowSession() {
        // Arrange
        SubscriptionPlan weekdayPlan = new SubscriptionPlan(
                5, 1, 10, 12.0, true, false, false, mockDiscountInfo
        );
        LocalDateTime friday = LocalDateTime.of(2024, 1, 19, 10, 0); // Friday

        int activeSessionsForVehicle = 0;
        int totalActiveSessionsForUser = 0;
        int sessionsStartedToday = 0;
        double hoursUsedToday = 0.0;
        boolean hasUnpaidSessions = false;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                weekdayPlan,
                hasUnpaidSessions,
                friday
        );

        // Assert
        assertTrue(result.isAllowed());
        assertNull(result.getReason());
    }

    @Test
    void onMonday_WithWeekdayOnlyPlan_ShouldAllowSession() {
        // Arrange
        SubscriptionPlan weekdayPlan = new SubscriptionPlan(
                5, 1, 10, 12.0, true, false, false, mockDiscountInfo
        );
        LocalDateTime monday = LocalDateTime.of(2024, 1, 15, 10, 0); // Monday

        int activeSessionsForVehicle = 0;
        int totalActiveSessionsForUser = 0;
        int sessionsStartedToday = 0;
        double hoursUsedToday = 0.0;
        boolean hasUnpaidSessions = false;

        // Act
        EligibilityResult result = service.canStartSession(
                mockUser,
                mockVehicle,
                activeSessionsForVehicle,
                totalActiveSessionsForUser,
                sessionsStartedToday,
                hoursUsedToday,
                weekdayPlan,
                hasUnpaidSessions,
                monday
        );

        // Assert
        assertTrue(result.isAllowed());
        assertNull(result.getReason());
    }
}