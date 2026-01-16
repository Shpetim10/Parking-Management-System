package Dto.Session;

import Enum.ZoneType;
import java.time.LocalDateTime;

public record StartSessionRequestDto(
        String userId,
        String vehiclePlate,
        String zoneId,
        String spotId,
        ZoneType zoneType,
        boolean isHoliday,
        LocalDateTime startTime
) {}