package Model;

import java.util.Objects;

public class SubscriptionPlan {

    public final int maxConcurrentSessions;
    public final int maxConcurrentSessionsPerVehicle;
    public final int maxDailySessions;
    public final double maxDailyHours;

    public final boolean weekdayOnly;

    public final boolean hasEvRights;
    public final boolean hasVipRights;

    public final DiscountInfo discountInfo;

    public SubscriptionPlan(
            int maxConcurrentSessions,
            int maxConcurrentSessionsPerVehicle,
            int maxDailySessions,
            double maxDailyHours,
            boolean weekdayOnly,
            boolean hasEvRights,
            boolean hasVipRights,
            DiscountInfo discountInfo
    ) {
        this.maxConcurrentSessions = maxConcurrentSessions;
        this.maxConcurrentSessionsPerVehicle = maxConcurrentSessionsPerVehicle;
        this.maxDailySessions = maxDailySessions;
        this.maxDailyHours = maxDailyHours;
        this.weekdayOnly = weekdayOnly;
        this.hasEvRights = hasEvRights;
        this.hasVipRights = hasVipRights;
        this.discountInfo = Objects.requireNonNull(discountInfo);
    }

    public boolean hasVipRights() {
        return hasVipRights;
    }

    public boolean hasEvRights() {
        return hasEvRights;
    }

    public int getMaxConcurrentSessions() {
        return maxConcurrentSessions;
    }

    public int getMaxConcurrentSessionsPerVehicle() {
        return maxConcurrentSessionsPerVehicle;
    }

    public int getMaxDailySessions() {
        return maxDailySessions;
    }

    public double getMaxDailyHours() {
        return maxDailyHours;
    }

    public boolean isHasVipRights() {
        return hasVipRights;
    }

    public boolean isWeekdayOnly() {
        return weekdayOnly;
    }

    public boolean isHasEvRights() {
        return hasEvRights;
    }

    public DiscountInfo getDiscountInfo() {
        return discountInfo;
    }
}
