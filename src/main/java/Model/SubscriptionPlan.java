package Model;

import java.time.LocalTime;

public class SubscriptionPlan {

    public final int maxConcurrentSessions;
    public final int maxConcurrentSessionsPerVehicle;
    public final int maxDailySessions;
    public final double maxDailyHours;

    public final boolean curfewRestricted;
    public final LocalTime curfewStart;
    public final LocalTime curfewEnd;

    public final boolean weekdayOnly;

    public SubscriptionPlan(
            int maxConcurrentSessions,
            int maxConcurrentSessionsPerVehicle,
            int maxDailySessions,
            double maxDailyHours,
            boolean curfewRestricted,
            LocalTime curfewStart,
            LocalTime curfewEnd,
            boolean weekdayOnly
    ) {
        this.maxConcurrentSessions = maxConcurrentSessions;
        this.maxConcurrentSessionsPerVehicle = maxConcurrentSessionsPerVehicle;
        this.maxDailySessions = maxDailySessions;
        this.maxDailyHours = maxDailyHours;
        this.curfewRestricted = curfewRestricted;
        this.curfewStart = curfewStart;
        this.curfewEnd = curfewEnd;
        this.weekdayOnly = weekdayOnly;
    }
}
