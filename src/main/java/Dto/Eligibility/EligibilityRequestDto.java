package Dto.Eligibility;

import java.time.Instant;
import java.time.LocalDateTime;

public record EligibilityRequestDto(
        String userId,
        String vehiclePlate,
        int activeSessionsForVehicle,
        int totalActiveSessionsForUser,
        int sessionsStartedToday,
        double hoursUsedToday,
        boolean hasUnpaidSessions,
        LocalDateTime now
) {}
