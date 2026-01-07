package Controller;

import Dto.Zone.SpotAssignmentRequestDto;
import Dto.Zone.SpotAssignmentResponseDto;
import Model.ParkingSpot;
import Model.ParkingZone;
import Model.SpotAssignmentRequest;
import Repository.ParkingZoneRepository;
import Service.ZoneAllocationService;

import java.util.List;
import java.util.Objects;

public class ZoneAllocationController {

    private final ZoneAllocationService zoneAllocationService;
    private final ParkingZoneRepository parkingZoneRepository;

    public ZoneAllocationController(
            ZoneAllocationService zoneAllocationService,
            ParkingZoneRepository parkingZoneRepository
    ) {
        this.zoneAllocationService = Objects.requireNonNull(zoneAllocationService);
        this.parkingZoneRepository = Objects.requireNonNull(parkingZoneRepository);
    }

    public SpotAssignmentResponseDto assignSpot(SpotAssignmentRequestDto dto) {
        Objects.requireNonNull(dto, "dto must not be null");

        List<ParkingZone> zones = parkingZoneRepository.findAll();

        SpotAssignmentRequest request = new SpotAssignmentRequest(
                dto.userId(),
                dto.requestedZoneType(),
                dto.hasEvRights(),
                dto.hasVipRights(),
                dto.requestedStartTime()
        );

        ParkingSpot assigned = zoneAllocationService.assignSpot(
                request,
                zones,
                dto.occupancyRatioForZone()
        );

        if (assigned == null) {
            return null; // consistent with your service returning null when no spot is available
        }

        return new SpotAssignmentResponseDto(
                assigned.getSpotId(),
                assigned.getZoneType(),
                assigned.getState()
        );
    }
}