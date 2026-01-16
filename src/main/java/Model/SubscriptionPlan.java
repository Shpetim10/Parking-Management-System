package Model;

public class SubscriptionPlan {

    public final int maxConcurrentSessions;
    public final int maxConcurrentSessionsPerVehicle;
    public final int maxDailySessions;
    public final double maxDailyHours;

    public final boolean weekdayOnly;

    public final boolean hasEvRights;
    public final boolean hasVipRights;

    public SubscriptionPlan(
            int maxConcurrentSessions,
            int maxConcurrentSessionsPerVehicle,
            int maxDailySessions,
            double maxDailyHours,
            boolean weekdayOnly,
            boolean hasEvRights,
            boolean hasVipRights
    ) {
        this.maxConcurrentSessions = maxConcurrentSessions;
        this.maxConcurrentSessionsPerVehicle = maxConcurrentSessionsPerVehicle;
        this.maxDailySessions = maxDailySessions;
        this.maxDailyHours = maxDailyHours;
        this.weekdayOnly = weekdayOnly;
        this.hasEvRights = hasEvRights;
        this.hasVipRights = hasVipRights;
    }
}
