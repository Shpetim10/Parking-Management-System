package Model;

import Enum.PenaltyType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class PenaltySummaryReport {

    private final BigDecimal totalOverstay;
    private final BigDecimal totalLostTicket;
    private final BigDecimal totalMisuse;
    private final int blacklistCandidatesCount;

    public PenaltySummaryReport(
            BigDecimal totalOverstay,
            BigDecimal totalLostTicket,
            BigDecimal totalMisuse,
            int blacklistCandidatesCount
    ) {
        this.totalOverstay = Objects.requireNonNull(totalOverstay);
        this.totalLostTicket = Objects.requireNonNull(totalLostTicket);
        this.totalMisuse = Objects.requireNonNull(totalMisuse);
        this.blacklistCandidatesCount = blacklistCandidatesCount;
    }

    public static PenaltySummaryReport from(List<PenaltyHistory> histories) {

        BigDecimal overstay = BigDecimal.ZERO;
        BigDecimal lost = BigDecimal.ZERO;
        BigDecimal misuse = BigDecimal.ZERO;

        for (PenaltyHistory history : histories) {
            for (Penalty penalty : history.getPenalties()) {
                if (penalty.getType() == PenaltyType.OVERSTAY) {
                    overstay = overstay.add(penalty.getAmount());
                } else if (penalty.getType() == PenaltyType.LOST_TICKET) {
                    lost = lost.add(penalty.getAmount());
                } else if (penalty.getType() == PenaltyType.MISUSE) {
                    misuse = misuse.add(penalty.getAmount());
                }
            }
        }

        return new PenaltySummaryReport(overstay, lost, misuse, 0);
    }

    public BigDecimal getTotalOverstay() {
        return totalOverstay;
    }

    public BigDecimal getTotalLostTicket() {
        return totalLostTicket;
    }

    public BigDecimal getTotalMisuse() {
        return totalMisuse;
    }

    public int getBlacklistCandidatesCount() {
        return blacklistCandidatesCount;
    }
}