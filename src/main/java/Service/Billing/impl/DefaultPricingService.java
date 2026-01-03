package Service.Billing.impl;

import Model.DynamicPricingConfig;
import Model.Tariff;
import Service.Billing.PricingService;
import Enum.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class DefaultPricingService implements PricingService {
    @Override
    public BigDecimal calculateBasePrice(double durationHours,
                                         ZoneType zoneType,
                                         DayType dayType,
                                         TimeOfDayBand timeOfDayBand,
                                         double occupancyRatio,
                                         Tariff tariff,
                                         DynamicPricingConfig config) {

        Objects.requireNonNull(zoneType, "zoneType must not be null");
        Objects.requireNonNull(dayType, "dayType must not be null");
        Objects.requireNonNull(timeOfDayBand, "timeOfDayBand must not be null");
        Objects.requireNonNull(tariff, "tariff must not be null");
        Objects.requireNonNull(config, "config must not be null");

        if (Double.isNaN(durationHours) || Double.isInfinite(durationHours) || durationHours < 0.0) {
            throw new IllegalArgumentException("durationHours must be a finite value >= 0.0");
        }
        if (Double.isNaN(occupancyRatio) || Double.isInfinite(occupancyRatio)
                || occupancyRatio < 0.0 || occupancyRatio > 1.0) {
            throw new IllegalArgumentException("occupancyRatio must be between 0.0 and 1.0 inclusive");
        }

        // No time parked -> 0 cost
        if (durationHours == 0.0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }

        BigDecimal hours = BigDecimal.valueOf(durationHours);
        BigDecimal price = tariff.getBaseHourlyRate().multiply(hours);

        // 1) Time-of-day multiplier
        BigDecimal timeMultiplier = (timeOfDayBand == TimeOfDayBand.PEAK)
                ? BigDecimal.valueOf(config.getPeakHourMultiplier())
                : BigDecimal.valueOf(config.getOffPeakMultiplier());
        price = price.multiply(timeMultiplier);

        // 2) High-occupancy surge
        if (occupancyRatio >= config.getHighOccupancyThreshold()) {
            BigDecimal surgeMultiplier = BigDecimal.valueOf(config.getHighOccupancyMultiplier());
            price = price.multiply(surgeMultiplier);
        }

        // 3) Weekend/Holiday surcharge
        if (dayType == DayType.WEEKEND || dayType == DayType.HOLIDAY) {
            BigDecimal surchargePercent = getWeekendOrHolidaySurchargePercent(tariff);
            if (surchargePercent.signum() > 0) {
                BigDecimal onePlus = BigDecimal.ONE.add(surchargePercent);
                price = price.multiply(onePlus);
            }
        }

        // 4) Daily/session cap
        BigDecimal dailyCap = tariff.getDailyCap();
        if (dailyCap != null && dailyCap.signum() > 0 && price.compareTo(dailyCap) > 0) {
            price = dailyCap;
        }

        // Keep some precision; final 2-decimal rounding happens later.
        return price.setScale(4, RoundingMode.HALF_UP);
    }


    private BigDecimal getWeekendOrHolidaySurchargePercent(Tariff tariff) {
        BigDecimal s = tariff.getWeekendOrHolidaySurchargePercent();
        return (s == null || s.signum() < 0) ? BigDecimal.ZERO : s;
    }
}
