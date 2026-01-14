package Dto.Billing;

import Enum.TimeOfDayBand;
import Enum.ZoneType;
import Enum.DayType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BillingRequest(
        String sessionId,
        ZoneType zoneType,
        DayType dayType,
        TimeOfDayBand timeOfDayBand,
        double occupancyRatio,
        LocalDateTime exitTime,
        BigDecimal penalties,
        int maxDurationHours
) { }