package Service.impl;

import Model.*;
import Service.*;
import Enum.*;
import Record.DurationInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class DefaultBillingService implements BillingService {

    private final DurationCalculator durationCalculator;
    private final PricingService pricingService;
    private final DiscountAndCapService discountAndCapService;
    private final TaxService taxService;

    public DefaultBillingService(DurationCalculator durationCalculator,
                                 PricingService pricingService,
                                 DiscountAndCapService discountAndCapService, TaxService taxService) {
        this.durationCalculator = Objects.requireNonNull(durationCalculator, "durationCalculator must not be null");
        this.pricingService = Objects.requireNonNull(pricingService, "pricingService must not be null");
        this.discountAndCapService = Objects.requireNonNull(discountAndCapService, "discountAndCapService must not be null");
        this.taxService = taxService;
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
                                       BigDecimal maxPriceCap,
                                       BigDecimal taxRate) {

        //duration
        DurationInfo durationInfo = durationCalculator.calculateDuration(entryTime, exitTime, maxDurationHours);
        int durationHours = durationInfo.hours();

        //base price
        BigDecimal basePrice = pricingService.calculateBasePrice(
                durationHours,
                dayType,
                timeOfDayBand,
                occupancyRatio,
                tariff,
                dynamicConfig
        );

        //discounts and caps
        BigDecimal netPrice = discountAndCapService.applyDiscountAndCaps(
                basePrice,
                discountInfo,
                penalties
        );

        //compute total discount
        BigDecimal basePlusPenalties = basePrice.add(penalties);
        BigDecimal discountsTotal = basePlusPenalties.subtract(netPrice);
        if (discountsTotal.signum() < 0) {
            discountsTotal = BigDecimal.ZERO;
        }

        //VAT/tax
        BigDecimal taxAmount = taxService.calculateTax(netPrice, taxRate);
        BigDecimal grossPrice = netPrice.add(taxAmount);


        return new BillingResult(
                basePrice,
                discountsTotal,
                penalties,
                netPrice,
                taxAmount,
                grossPrice
        );

    }

    @Override
    public DurationInfo calculateDuration(LocalDateTime entryTime,
                                          LocalDateTime exitTime,
                                          int maxDurationHours) {
        return durationCalculator.calculateDuration(entryTime, exitTime, maxDurationHours);
    }
}