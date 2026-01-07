package Dto.Eligibility;

import java.time.Instant;

public record EligibilityRequestDto(
        String userId,
        String vehiclePlate,
        int activeSessionsForVehicle,
        int totalActiveSessionsForUser,
        int sessionsStartedToday,
        double hoursUsedToday,
        boolean hasUnpaidSessions,
        Instant now
) {}
