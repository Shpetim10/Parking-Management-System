package Dto.Monitoring;

import java.math.BigDecimal;

public record PenaltySummaryResponseDto(
        BigDecimal totalOverstay,
        BigDecimal totalLostTicket,
        BigDecimal totalMisuse,
        int blacklistCandidatesCount
) { }