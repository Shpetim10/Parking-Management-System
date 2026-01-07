package Dto.Exit;

public record ExitAuthorizationRequestDto(
        String userId,
        String sessionId,
        String plateAtGate
) {}