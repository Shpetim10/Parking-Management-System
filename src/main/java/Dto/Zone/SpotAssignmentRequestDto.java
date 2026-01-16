package Dto.Zone;

import Enum.ZoneType;
import java.time.LocalDateTime;

public record SpotAssignmentRequestDto(
        String userId,
        ZoneType requestedZoneType,
        LocalDateTime requestedStartTime
) {}