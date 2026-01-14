package Service;

import Model.*;
import java.time.Instant;
import java.time.LocalDateTime;

public interface EligibilityService {

    EligibilityResult canStartSession(
            User user,
            Vehicle vehicle,
            int activeSessionsForVehicle,
            int totalActiveSessionsForUser,
            int sessionsStartedToday,
            double hoursUsedToday,
            SubscriptionPlan plan,
            boolean hasUnpaidSessions,
            LocalDateTime now
    );
}
