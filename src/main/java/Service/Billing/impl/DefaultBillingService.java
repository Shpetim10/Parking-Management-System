package Service.Billing.impl;

import Model.BillingResult;
import Model.DiscountInfo;
import Model.DynamicPricingConfig;
import Model.Tariff;
import Service.Billing.BillingService;
import Service.Billing.DiscountAndCapService;
import Service.Billing.DurationCalculator;
import Service.Billing.PricingService;
import Enum.*;
import Record.DurationInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class DefaultBillingService implements BillingService {

    private final DurationCalculator durationCalculator;
    private final PricingService pricingService;
    private final DiscountAndCapService discountAndCapService;

    public DefaultBillingService(DurationCalculator durationCalculator,
                                 PricingService pricingService,
                                 DiscountAndCapService discountAndCapService) {
        this.durationCalculator = Objects.requireNonNull(durationCalculator, "durationCalculator must not be null");
        this.pricingService = Objects.requireNonNull(pricingService, "pricingService must not be null");
        this.discountAndCapService = Objects.requireNonNull(discountAndCapService, "discountAndCapService must not be null");
    }

    @Override
    public BillingResult calculateBill(LocalDateTime entryTime,
                                       LocalDateTime exitTime,
                                       ZoneType zoneType,
                                       DayType dayType,
                                       TimeOfDayBand timeOfDayBand,
                                       double occupancyRatio,
                                       Tariff tariff,
                                       DynamicPricingConfig dynamicConfig,
                                       DiscountInfo discountInfo,
                                       BigDecimal penalties,
                                       int maxDurationHours,
                                       BigDecimal maxPriceCap) {

        Objects.requireNonNull(entryTime, "entryTime must not be null");
        Objects.requireNonNull(exitTime, "exitTime must not be null");
        Objects.requireNonNull(zoneType, "zoneType must not be null");
        Objects.requireNonNull(dayType, "dayType must not be null");
        Objects.requireNonNull(timeOfDayBand, "timeOfDayBand must not be null");
        Objects.requireNonNull(tariff, "tariff must not be null");
        Objects.requireNonNull(dynamicConfig, "dynamicConfig must not be null");
        Objects.requireNonNull(discountInfo, "discountInfo must not be null");
        Objects.requireNonNull(penalties, "penalties must not be null");

        // 1) Calculate duration
        DurationInfo durationInfo = durationCalculator.calculateDuration(entryTime, exitTime, maxDurationHours);
        int durationHours = durationInfo.hours();

        // NOTE: durationInfo.exceededMax() is not directly used here;
        // overstay behavior (extra penalties) is handled externally by Person C
        // and should be passed in via the 'penalties' parameter.

        // 2) Calculate base price
        BigDecimal basePrice = pricingService.calculateBasePrice(
                durationHours,
                zoneType,
                dayType,
                timeOfDayBand,
                occupancyRatio,
                tariff,
                dynamicConfig
        );

        // 3) Apply discounts, penalties, and cap to get final price
        BigDecimal finalPrice = discountAndCapService.applyDiscountAndCaps(
                basePrice,
                discountInfo,
                penalties,
                maxPriceCap
        );

        // 4) Compute discountsTotal: base + penalties - final
        BigDecimal basePlusPenalties = basePrice.add(penalties);
        BigDecimal discountsTotal = basePlusPenalties.subtract(finalPrice);
        if (discountsTotal.signum() < 0) {
            discountsTotal = BigDecimal.ZERO;
        }

        return new BillingResult(
                basePrice,
                discountsTotal,
                penalties,
                finalPrice
        );
    }

    @Override
    public DurationInfo calculateDuration(LocalDateTime entryTime,
                                          LocalDateTime exitTime,
                                          int maxDurationHours) {
        return durationCalculator.calculateDuration(entryTime, exitTime, maxDurationHours);
    }
}