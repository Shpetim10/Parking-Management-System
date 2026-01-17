package Service.impl;

import Model.DynamicPricingConfig;
import Model.Tariff;
import Service.PricingService;
import Enum.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class DefaultPricingService implements PricingService {

    @Override
    public BigDecimal calculateBasePrice(int durationHours,
                                         DayType dayType,
                                         TimeOfDayBand timeOfDayBand,
                                         double occupancyRatio,
                                         Tariff tariff,
                                         DynamicPricingConfig config) {

        validateInputs(durationHours, occupancyRatio,dayType, timeOfDayBand, tariff, config);

        if (durationHours == 0.0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal price = calculateBase(durationHours, tariff);
        price = applyTimeOfDayMultiplier(price, timeOfDayBand, config);
        price = applyHighOccupancySurge(price, occupancyRatio, config);
        price = applyWeekendOrHolidaySurcharge(price, dayType, tariff);
        price = applyDailyCap(price, tariff.getDailyCap(), durationHours);

        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateInputs(int durationHours,
                                double occupancyRatio,
                                DayType dayType,
                                TimeOfDayBand timeOfDayBand,
                                Tariff tariff,
                                DynamicPricingConfig config) {

        Objects.requireNonNull(dayType, "dayType must not be null");
        Objects.requireNonNull(timeOfDayBand, "timeOfDayBand must not be null");
        Objects.requireNonNull(tariff, "tariff must not be null");
        Objects.requireNonNull(config, "config must not be null");

        if (durationHours < 0.0) {
            throw new IllegalArgumentException("durationHours must be a finite value >= 0.0");
        }
        if (occupancyRatio < 0.0 || occupancyRatio > 1.0) {
            throw new IllegalArgumentException("occupancyRatio must be between 0.0 and 1.0 inclusive");
        }
    }

    private BigDecimal calculateBase(int durationHours, Tariff tariff) {
        if(durationHours < 0 || tariff == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal hours = BigDecimal.valueOf(durationHours);
        return tariff.getBaseHourlyRate().multiply(hours);
    }

    private BigDecimal applyTimeOfDayMultiplier(BigDecimal price,
                                                TimeOfDayBand band,
                                                DynamicPricingConfig config) {
        if(price == null || band == null || config==null) {
            return BigDecimal.ZERO.setScale(2);
        }

        if (band == TimeOfDayBand.OFF_PEAK) return price;

        double multiplier = config.getPeakHourMultiplier();

        return price.multiply(BigDecimal.valueOf(multiplier));
    }

    private BigDecimal applyHighOccupancySurge(BigDecimal price,
                                               double occupancyRatio,
                                               DynamicPricingConfig config) {
        if (price == null || occupancyRatio < 0 || occupancyRatio>1 || config == null) {
            return BigDecimal.ZERO.setScale(2);
        }

        if (occupancyRatio >= config.getHighOccupancyThreshold()) {
            return price.multiply(BigDecimal.valueOf(config.getHighOccupancyMultiplier()));
        }
        return price;
    }

    private BigDecimal applyWeekendOrHolidaySurcharge(BigDecimal price,
                                                      DayType dayType,
                                                      Tariff tariff) {
        if(price == null || dayType == null || tariff == null) {
            return BigDecimal.ZERO.setScale(2);
        }

        if (dayType != DayType.WEEKEND && dayType != DayType.HOLIDAY) {
            return price;
        }
        BigDecimal surchargePercent = getWeekendOrHolidaySurchargePercent(tariff);
        if (surchargePercent.signum() <= 0) {
            return price;
        }
        BigDecimal factor = BigDecimal.ONE.add(surchargePercent);
        return price.multiply(factor);
    }

    private BigDecimal applyDailyCap(BigDecimal price, BigDecimal dailyCap, int durationHours) {
        if (dailyCap == null || dailyCap.signum() <= 0) {
            return price;
        }
        // if duration is more than 24h
        dailyCap = dailyCap.multiply(new BigDecimal(Math.ceil(durationHours/24.0)));

        return (price.compareTo(dailyCap) > 0) ? dailyCap : price;
    }

    private BigDecimal getWeekendOrHolidaySurchargePercent(Tariff tariff) {
        BigDecimal s = tariff.getWeekendOrHolidaySurchargePercent();
        return (s == null || s.signum() < 0) ? BigDecimal.ZERO : s.divide(BigDecimal.valueOf(100));
    }
}
