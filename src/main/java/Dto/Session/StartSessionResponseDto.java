package Dto.Session;

import Enum.SessionState;
import Enum.DayType;
import Enum.TimeOfDayBand;
import java.time.LocalDateTime;

public record StartSessionResponseDto(
        String sessionId,
        SessionState state,
        DayType dayType,
        TimeOfDayBand timeBand,
        LocalDateTime startTime
) {}