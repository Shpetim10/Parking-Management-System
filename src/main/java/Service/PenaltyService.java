package Service;

import java.math.BigDecimal;

public interface PenaltyService {

    BigDecimal calculatePenalty(
            boolean overstayed,
            double extraHours,
            boolean lostTicket,
            boolean zoneMisuse,
            BigDecimal baseOverstayRatePerHour,
            BigDecimal overstayCap,
            BigDecimal lostTicketFee,
            BigDecimal misuseFee
    );
}