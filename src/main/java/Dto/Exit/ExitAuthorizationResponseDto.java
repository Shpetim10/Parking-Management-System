package Dto.Exit;

import Enum.ExitFailureReason;

public record ExitAuthorizationResponseDto(
        boolean allowed,
        ExitFailureReason reason
) {}