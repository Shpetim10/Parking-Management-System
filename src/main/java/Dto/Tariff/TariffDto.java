package Dto.Tariff;

import Enum.ZoneType;
import java.math.BigDecimal;

public record TariffDto(
        ZoneType zoneType,
        BigDecimal baseHourlyRate,
        BigDecimal dailyCap,
        boolean overnightFlatRateEnabled,
        BigDecimal overnightFlatRate,
        BigDecimal weekendOrHolidaySurchargePercent
) { }
