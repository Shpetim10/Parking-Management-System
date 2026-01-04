package Service.Billing.impl;

import Model.DiscountInfo;
import Service.Billing.DiscountAndCapService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class DefaultDiscountAndCapService implements DiscountAndCapService {
    @Override
    public BigDecimal applyDiscountAndCaps(BigDecimal basePrice, DiscountInfo discountInfo, BigDecimal penalties, BigDecimal maxPriceCap) {
        Objects.requireNonNull(basePrice, "basePrice must not be null");
        Objects.requireNonNull(discountInfo, "discountInfo must not be null");
        Objects.requireNonNull(penalties, "penalties must not be null");

        if (basePrice.signum() < 0) {
            throw new IllegalArgumentException("basePrice must not be negative");
        }
        if (penalties.signum() < 0) {
            throw new IllegalArgumentException("penalties must not be negative");
        }
        if (maxPriceCap != null && maxPriceCap.signum() < 0) {
            throw new IllegalArgumentException("maxPriceCap must not be negative");
        }

        // 1) Start amount = base + penalties
        BigDecimal amount = basePrice.add(penalties);

        // 2) Subscription percent discount
        if (discountInfo.hasSubscriptionPercentDiscount()) {
            BigDecimal subDiscount = amount.multiply(discountInfo.getSubscriptionDiscountPercent());
            amount = amount.subtract(subDiscount);
        }

        // 3) Promo percent discount
        if (discountInfo.hasPromoPercentDiscount()) {
            BigDecimal promoPercentDiscount = amount.multiply(discountInfo.getPromoDiscountPercent());
            amount = amount.subtract(promoPercentDiscount);
        }

        // 4) Promo fixed discount
        if (discountInfo.hasPromoFixedDiscount()) {
            amount = amount.subtract(discountInfo.getPromoDiscountFixed());
        }

        // 5) Clamp lower bound to 0
        if (amount.signum() < 0) {
            amount = BigDecimal.ZERO;
        }

        // 6) Apply cap if configured
        if (maxPriceCap != null && amount.compareTo(maxPriceCap) > 0) {
            amount = maxPriceCap;
        }

        // 7) Final rounding to 2 decimal places (currency)
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}

