package Dto.DiscountInfo;

import java.math.BigDecimal;

public record DiscountInfoDto(
        BigDecimal subscriptionDiscountPercent,
        BigDecimal promoDiscountPercent,
        BigDecimal promoDiscountFixed,
        boolean subscriptionHasFreeHours,
        int freeHoursPerDay
) { }
