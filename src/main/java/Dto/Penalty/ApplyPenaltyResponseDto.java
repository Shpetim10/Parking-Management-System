package Dto.Penalty;

import Enum.BlacklistStatus;

import java.math.BigDecimal;

public record ApplyPenaltyResponseDto(
        String userId,
        BigDecimal newTotalPenaltyAmount,
        int penaltyCount,
        BlacklistStatus blacklistStatus
) { }