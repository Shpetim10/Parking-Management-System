package Service;

import Model.*;
import java.time.Instant;

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
            Instant now
    );
}
