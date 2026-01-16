package Dto.Penalty;

import Enum.PenaltyType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record ApplyPenaltyRequestDto(
        String userId,
        PenaltyType type,
        BigDecimal amount,
        LocalDateTime timestamp
) {}