package Dto.Eligibility;

public record EligibilityResponseDto(
        boolean allowed,
        String reason
) {}