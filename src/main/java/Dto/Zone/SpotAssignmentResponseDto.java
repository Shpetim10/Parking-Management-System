package Dto.Zone;

import Enum.SpotState;
import Enum.ZoneType;

public record SpotAssignmentResponseDto(
        String spotId,
        ZoneType zoneType,
        SpotState state
) { }