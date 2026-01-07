package Model;

import java.math.BigDecimal;
import java.util.Objects;

public class DiscountInfo {
    private final BigDecimal subscriptionDiscountPercent; // in [0,1]
    private final BigDecimal promoDiscountPercent;        // in [0,1]
    private final BigDecimal promoDiscountFixed;          // >= 0
    private final boolean subscriptionHasFreeHours;
    private final int freeHoursPerDay;

    public DiscountInfo(BigDecimal subscriptionDiscountPercent,
                        BigDecimal promoDiscountPercent,
                        BigDecimal promoDiscountFixed,
                        boolean subscriptionHasFreeHours,
                        int freeHoursPerDay) {

        this.subscriptionDiscountPercent = normalizePercent(subscriptionDiscountPercent, "subscriptionDiscountPercent");
        this.promoDiscountPercent = normalizePercent(promoDiscountPercent, "promoDiscountPercent");
        this.promoDiscountFixed = normalizeNonNegativeAmount(promoDiscountFixed, "promoDiscountFixed");

        if (freeHoursPerDay < 0) {
            throw new IllegalArgumentException("freeHoursPerDay must not be negative");
        }
        if (!subscriptionHasFreeHours && freeHoursPerDay > 0) {
            throw new IllegalArgumentException(
                    "freeHoursPerDay > 0 is not allowed when subscriptionHasFreeHours is false");
        }
        this.subscriptionHasFreeHours = subscriptionHasFreeHours;
        this.freeHoursPerDay = freeHoursPerDay;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DiscountInfo that)) return false;
        return subscriptionHasFreeHours == that.subscriptionHasFreeHours && freeHoursPerDay == that.freeHoursPerDay && Objects.equals(subscriptionDiscountPercent, that.subscriptionDiscountPercent) && Objects.equals(promoDiscountPercent, that.promoDiscountPercent) && Objects.equals(promoDiscountFixed, that.promoDiscountFixed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionDiscountPercent, promoDiscountPercent, promoDiscountFixed, subscriptionHasFreeHours, freeHoursPerDay);
    }

    private static BigDecimal normalizePercent(BigDecimal value, String fieldName) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        if (value.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(fieldName + " must be <= 1.0");
        }
        return value;
    }

    private static BigDecimal normalizeNonNegativeAmount(BigDecimal value, String fieldName) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }

    public BigDecimal getSubscriptionDiscountPercent() {
        return subscriptionDiscountPercent;
    }

    public BigDecimal getPromoDiscountPercent() {
        return promoDiscountPercent;
    }

    public BigDecimal getPromoDiscountFixed() {
        return promoDiscountFixed;
    }

    public boolean isSubscriptionHasFreeHours() {
        return subscriptionHasFreeHours;
    }

    public int getFreeHoursPerDay() {
        return freeHoursPerDay;
    }

    // Convenience helpers for discount logic
    public boolean hasSubscriptionPercentDiscount() {
        return subscriptionDiscountPercent.signum() > 0;
    }

    public boolean hasPromoPercentDiscount() {
        return promoDiscountPercent.signum() > 0;
    }

    public boolean hasPromoFixedDiscount() {
        return promoDiscountFixed.signum() > 0;
    }
}
