package Model;

import java.math.BigDecimal;
import java.util.Objects;
import Enum.ZoneType;

public class Tariff {
    private final ZoneType zoneType;
    private final BigDecimal baseHourlyRate;
    private final BigDecimal dailyCap;

    private final BigDecimal weekendOrHolidaySurchargePercent;

    public Tariff(ZoneType zoneType,
                  BigDecimal baseHourlyRate,
                  BigDecimal dailyCap,
                  BigDecimal weekendOrHolidaySurchargePercent) {

        this.zoneType = Objects.requireNonNull(zoneType, "zoneType must not be null");
        this.baseHourlyRate = requireNonNegative(baseHourlyRate, "baseHourlyRate");
        this.dailyCap = requireNonNegativeOrNull(dailyCap, "dailyCap");

        this.weekendOrHolidaySurchargePercent = weekendOrHolidaySurchargePercent;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tariff tariff)) return false;
        return zoneType == tariff.zoneType && Objects.equals(baseHourlyRate, tariff.baseHourlyRate) && Objects.equals(dailyCap, tariff.dailyCap) && Objects.equals(weekendOrHolidaySurchargePercent, tariff.weekendOrHolidaySurchargePercent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zoneType, baseHourlyRate, dailyCap, weekendOrHolidaySurchargePercent);
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

    public BigDecimal getWeekendOrHolidaySurchargePercent() {
        return weekendOrHolidaySurchargePercent;
    }
}
