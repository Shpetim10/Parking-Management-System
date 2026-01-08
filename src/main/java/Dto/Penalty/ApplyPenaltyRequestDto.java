package Dto.Penalty;

import Enum.PenaltyType;

import java.math.BigDecimal;
import java.time.Instant;

public record ApplyPenaltyRequestDto(
        String userId,
        PenaltyType type,
        BigDecimal amount,
        Instant timestamp
) { }