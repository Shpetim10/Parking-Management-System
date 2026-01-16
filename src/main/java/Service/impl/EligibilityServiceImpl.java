package Service.impl;

import Service.EligibilityService;
import Model.*;
import Enum.UserStatus;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class EligibilityServiceImpl implements EligibilityService {

    @Override
    public EligibilityResult canStartSession(
            User user,
            Vehicle vehicle,
            int activeSessionsForVehicle,
            int totalActiveSessionsForUser,
            int sessionsStartedToday,
            double hoursUsedToday,
            SubscriptionPlan plan,
            boolean hasUnpaidSessions,
            LocalDateTime now
    ) {

        if (user.getStatus() != UserStatus.ACTIVE)
            return EligibilityResult.denied("USER_NOT_ACTIVE");

        if (hasUnpaidSessions)
            return EligibilityResult.denied("UNPAID_SESSIONS_EXIST");

        if (totalActiveSessionsForUser >= plan.maxConcurrentSessions)
            return EligibilityResult.denied("MAX_CONCURRENT_SESSIONS_REACHED");

        if (activeSessionsForVehicle >= plan.maxConcurrentSessionsPerVehicle)
            return EligibilityResult.denied("MAX_SESSIONS_PER_VEHICLE_REACHED");

        if (sessionsStartedToday >= plan.maxDailySessions)
            return EligibilityResult.denied("DAILY_SESSION_LIMIT_REACHED");

        if (hoursUsedToday >= plan.maxDailyHours)
            return EligibilityResult.denied("DAILY_HOURS_LIMIT_REACHED");

        var day = now.getDayOfWeek();

        if (plan.weekdayOnly &&
                (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY))
            return EligibilityResult.denied("WEEKDAY_ONLY_PLAN");

        return EligibilityResult.allowed();
    }
}