package Dto.Monitoring;

import java.time.Instant;

public record LogEventDto(
        Instant timestamp,
        String type,
        String details
) { }