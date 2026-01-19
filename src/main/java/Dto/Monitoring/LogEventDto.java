package Dto.Monitoring;

import java.time.LocalDateTime;

public record LogEventDto(
        LocalDateTime timestamp,
        String type,
        String details
) { }