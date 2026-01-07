package Dto.Zone;

import Enum.ZoneType;

import java.time.Instant;

public record SpotAssignmentRequestDto(
        String userId,
        ZoneType requestedZoneType,
        boolean hasEvRights,
        boolean hasVipRights,
        Instant requestedStartTime,
        double occupancyRatioForZone
) { }