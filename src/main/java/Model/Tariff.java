package Model;

import java.math.BigDecimal;
import java.util.Objects;
import Enum.ZoneType;

public class Tariff {
    private final ZoneType zoneType;
    private final BigDecimal baseHourlyRate;
    private final BigDecimal dailyCap;
    private final boolean overnightFlatRateEnabled;
    private final BigDecimal overnightFlatRate;

    private final BigDecimal weekendOrHolidaySurchargePercent;

    public Tariff(ZoneType zoneType,
                  BigDecimal baseHourlyRate,
                  BigDecimal dailyCap,
                  boolean overnightFlatRateEnabled,
                  BigDecimal overnightFlatRate, BigDecimal weekendOrHolidaySurchargePercent) {

        this.zoneType = Objects.requireNonNull(zoneType, "zoneType must not be null");
        this.baseHourlyRate = requireNonNegative(baseHourlyRate, "baseHourlyRate");
        this.dailyCap = requireNonNegativeOrNull(dailyCap, "dailyCap");

        this.overnightFlatRateEnabled = overnightFlatRateEnabled;
        this.weekendOrHolidaySurchargePercent = weekendOrHolidaySurchargePercent;

        if (overnightFlatRateEnabled) {
            // if flag is true, rate must be provided and non-negative
            this.overnightFlatRate = requireNonNegative(overnightFlatRate, "overnightFlatRate");
        } else {
            // if flag is false, we ignore any provided value and store null
            this.overnightFlatRate = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tariff tariff)) return false;
        return overnightFlatRateEnabled == tariff.overnightFlatRateEnabled && zoneType == tariff.zoneType && Objects.equals(baseHourlyRate, tariff.baseHourlyRate) && Objects.equals(dailyCap, tariff.dailyCap) && Objects.equals(overnightFlatRate, tariff.overnightFlatRate) && Objects.equals(weekendOrHolidaySurchargePercent, tariff.weekendOrHolidaySurchargePercent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zoneType, baseHourlyRate, dailyCap, overnightFlatRateEnabled, overnightFlatRate, weekendOrHolidaySurchargePercent);
    }

    private static BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }

    private static BigDecimal requireNonNegativeOrNull(BigDecimal value, String fieldName) {
        if (value == null) {
            return null;
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }

    public ZoneType getZoneType() {
        return zoneType;
    }

    public BigDecimal getBaseHourlyRate() {
        return baseHourlyRate;
    }

    public BigDecimal getDailyCap() {
        return dailyCap;
    }

    public boolean isOvernightFlatRateEnabled() {
        return overnightFlatRateEnabled;
    }

    public BigDecimal getOvernightFlatRate() {
        return overnightFlatRate;
    }

    public BigDecimal getWeekendOrHolidaySurchargePercent() {
        return weekendOrHolidaySurchargePercent;
    }
}
