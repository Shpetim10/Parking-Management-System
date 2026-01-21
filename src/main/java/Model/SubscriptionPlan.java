package Model;

import java.math.BigDecimal;
import java.util.Objects;

public class SubscriptionPlan {

    public final int maxConcurrentSessions;
    public final int maxConcurrentSessionsPerVehicle;
    public final int maxDailySessions;
    public final double maxDailyHours;

    public final boolean weekdayOnly;

    public final boolean hasEvRights;
    public final boolean hasVipRights;

    public DiscountInfo discountInfo;

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

    // Default plan
    public static SubscriptionPlan defaultPlan() {
        return new SubscriptionPlan(
                1, 1, 5, 8,
                false, false, false,
                new DiscountInfo(new BigDecimal("0.1"), BigDecimal.ZERO, BigDecimal.ZERO, false, 0)
        );
    }

    // EV Zone plan
    public static SubscriptionPlan evZonePlan() {
        return new SubscriptionPlan(
                2, 1, 6, 10,
                true, true, false,
                new DiscountInfo(new BigDecimal("0.15"), BigDecimal.ZERO, BigDecimal.ZERO, true, 1)
        );
    }

    // VIP/Zip Zone plan
    public static SubscriptionPlan vipZonePlan() {
        return new SubscriptionPlan(
                3, 2, 8, 12,
                false, false, true,
                new DiscountInfo(new BigDecimal("0.20"), BigDecimal.ZERO, BigDecimal.ZERO, true, 2)
        );
    }


}
