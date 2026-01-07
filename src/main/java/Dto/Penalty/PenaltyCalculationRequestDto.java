package Dto.Penalty;

import java.math.BigDecimal;

public record PenaltyCalculationRequestDto(
        boolean overstayed,
        double extraHours,
        boolean lostTicket,
        boolean zoneMisuse,
        BigDecimal baseOverstayRatePerHour,
        BigDecimal overstayCap,
        BigDecimal lostTicketFee,
        BigDecimal misuseFee
) { }