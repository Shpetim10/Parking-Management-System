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
    public BigDecimal calculateBasePrice(double durationHours,
                                         ZoneType zoneType,
                                         DayType dayType,
                                         TimeOfDayBand timeOfDayBand,
                                         double occupancyRatio,
                                         Tariff tariff,
                                         DynamicPricingConfig config) {

        validateInputs(durationHours, occupancyRatio, zoneType, dayType, timeOfDayBand, tariff, config);

        if (durationHours == 0.0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }

        BigDecimal price = calculateBase(durationHours, tariff);
        price = applyTimeOfDayMultiplier(price, timeOfDayBand, config);
        price = applyHighOccupancySurge(price, occupancyRatio, config);
        price = applyWeekendOrHolidaySurcharge(price, dayType, tariff);
        price = applyDailyCap(price, tariff.getDailyCap());

        return price.setScale(4, RoundingMode.HALF_UP);
    }

    private void validateInputs(double durationHours,
                                double occupancyRatio,
                                ZoneType zoneType,
                                DayType dayType,
                                TimeOfDayBand timeOfDayBand,
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
    }

    private BigDecimal calculateBase(double durationHours, Tariff tariff) {
        BigDecimal hours = BigDecimal.valueOf(durationHours);
        return tariff.getBaseHourlyRate().multiply(hours);
    }

    private BigDecimal applyTimeOfDayMultiplier(BigDecimal price,
                                                TimeOfDayBand band,
                                                DynamicPricingConfig config) {
        double multiplier = (band == TimeOfDayBand.PEAK)
                ? config.getPeakHourMultiplier()
                : config.getOffPeakMultiplier();
        return price.multiply(BigDecimal.valueOf(multiplier));
    }

    private BigDecimal applyHighOccupancySurge(BigDecimal price,
                                               double occupancyRatio,
                                               DynamicPricingConfig config) {
        if (occupancyRatio >= config.getHighOccupancyThreshold()) {
            return price.multiply(BigDecimal.valueOf(config.getHighOccupancyMultiplier()));
        }
        return price;
    }

    private BigDecimal applyWeekendOrHolidaySurcharge(BigDecimal price,
                                                      DayType dayType,
                                                      Tariff tariff) {
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

    private BigDecimal applyDailyCap(BigDecimal price, BigDecimal dailyCap) {
        if (dailyCap == null || dailyCap.signum() <= 0) {
            return price;
        }
        return (price.compareTo(dailyCap) > 0) ? dailyCap : price;
    }

    private BigDecimal getWeekendOrHolidaySurchargePercent(Tariff tariff) {
        BigDecimal s = tariff.getWeekendOrHolidaySurchargePercent();
        return (s == null || s.signum() < 0) ? BigDecimal.ZERO : s;
    }
}
