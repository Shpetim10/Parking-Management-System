package Dto.Penalty;

import java.math.BigDecimal;

public record PenaltyCalculationResponseDto(
        BigDecimal totalPenalty
) { }