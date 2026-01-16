package Dto.Tariff;

import Enum.ZoneType;
import java.math.BigDecimal;

public record TariffDto(
        ZoneType zoneType,
        BigDecimal baseHourlyRate,
        BigDecimal dailyCap,
        BigDecimal weekendOrHolidaySurchargePercent
) { }
