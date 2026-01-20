package Controller;

import Dto.Zone.SpotAssignmentRequestDto;
import Dto.Zone.SpotAssignmentResponseDto;
import Exceptions.NoSpotsAvailableException;
import Model.ParkingSpot;
import Model.ParkingZone;
import Model.SpotAssignmentRequest;
import Model.SubscriptionPlan;
import Repository.ParkingZoneRepository;
import Repository.SubscriptionPlanRepository;
import Service.ZoneAllocationService;
import Service.ZoneOccupancyService;

import java.util.Objects;

public class ZoneAllocationController {

    private final ZoneAllocationService zoneAllocationService;
    private final ParkingZoneRepository parkingZoneRepository;
    private final ZoneOccupancyService occupancyService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public ZoneAllocationController(
            ZoneAllocationService zoneAllocationService,
            ParkingZoneRepository parkingZoneRepository,
            ZoneOccupancyService occupancyService, SubscriptionPlanRepository subscriptionPlanRepository
    ) {
        this.zoneAllocationService = Objects.requireNonNull(zoneAllocationService);
        this.parkingZoneRepository = Objects.requireNonNull(parkingZoneRepository);
        this.occupancyService = Objects.requireNonNull(occupancyService);
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    public SpotAssignmentResponseDto assignSpot(SpotAssignmentRequestDto dto){
        Objects.requireNonNull(dto);

        SubscriptionPlan subscriptionPlan= subscriptionPlanRepository.getPlanForUser(dto.userId()).orElseThrow();

        SpotAssignmentRequest request = new SpotAssignmentRequest(
                dto.userId(),
                dto.requestedZoneType(),
                subscriptionPlan,
                dto.requestedStartTime()
        );

        for (ParkingZone zone : parkingZoneRepository.findAll()) {

            if (!zone.getZoneType().equals(dto.requestedZoneType())) {
                continue;
            }

            double occupancy = occupancyService
                    .calculateOccupancyRatioForZone(zone.getZoneId());

            if (occupancy >= zone.getMaxOccupancyThreshold()) {
                continue;
            }

            ParkingSpot spot = zoneAllocationService.assignSpot(request, zone);
            if (spot != null) {
                return new SpotAssignmentResponseDto(
                        spot.getSpotId(),
                        spot.getParkingZone().getZoneType(),
                        spot.getState(),
                        zone.getZoneId()
                );
            }
        }

        return null;
    }
}