package Service.impl;
import Service.PenaltyService;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PenaltyServiceImpl implements PenaltyService {

    @Override
    public BigDecimal calculatePenalty(
            boolean overstayed,
            double extraHours,
            boolean lostTicket,
            boolean zoneMisuse,
            BigDecimal baseOverstayRatePerHour,
            BigDecimal overstayCap,
            BigDecimal lostTicketFee,
            BigDecimal misuseFee
    ) {

        if (baseOverstayRatePerHour == null
                || overstayCap == null
                || lostTicketFee == null
                || misuseFee == null) {
            throw new IllegalArgumentException("Penalty rates and caps cannot be null");
        }

        BigDecimal penalty = BigDecimal.ZERO;

        // Overstay penalty
        if (overstayed) {
            if (extraHours < 0) {
                throw new IllegalArgumentException("Extra hours cannot be negative");
            }

            BigDecimal overstayPenalty =
                    baseOverstayRatePerHour.multiply(BigDecimal.valueOf(extraHours));

            if (overstayPenalty.compareTo(overstayCap) > 0) {
                overstayPenalty = overstayCap;
            }

            penalty = penalty.add(overstayPenalty);
        }

        // Lost ticket penalty
        if (lostTicket) {
            penalty = penalty.add(lostTicketFee);
        }

        // Zone misuse penalty
        if (zoneMisuse) {
            penalty = penalty.add(misuseFee);
        }

        if (penalty.compareTo(BigDecimal.ZERO) < 0) {
            penalty = BigDecimal.ZERO;
        }

        return penalty.setScale(2, RoundingMode.HALF_UP);
    }
}