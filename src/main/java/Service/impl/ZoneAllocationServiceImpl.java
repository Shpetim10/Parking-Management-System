package Service.impl;

import Model.ParkingSpot;
import Model.ParkingZone;
import Model.SpotAssignmentRequest;
import Model.Enum.ZoneType;
import Service.ZoneAllocationService;

import java.util.List;

public class ZoneAllocationServiceImpl implements ZoneAllocationService {

    @Override
    public ParkingSpot assignSpot(
            SpotAssignmentRequest request,
            List<ParkingZone> zones,
            double currentOccupancyRatioForZone
    ) {

        if (request == null || zones == null) {
            throw new IllegalArgumentException("Request and zones cannot be null");
        }

        for (ParkingZone zone : zones) {

            if (!zone.getZoneType().equals(request.getRequestedZoneType())) {
                continue;
            }

            if (zoneAccessDenied(request, zone.getZoneType())) {
                continue;
            }

            if (currentOccupancyRatioForZone >= zone.getMaxOccupancyThreshold()) {
                continue;
            }

            if (!zone.hasFreeSpot()) {
                continue;
            }

            ParkingSpot spot = zone.getFirstFreeSpot();
            spot.reserve();
            return spot;
        }

        return null; // or throw NoAvailableSpotException
    }

    private boolean zoneAccessDenied(SpotAssignmentRequest request, ZoneType zoneType) {
        if (zoneType == ZoneType.EV && !request.hasEvRights()) {
            return true;
        }
        if (zoneType == ZoneType.VIP && !request.hasVipRights()) {
            return true;
        }
        return false;
    }
}