package Service.impl;

import Enum.ZoneType;
import Model.ParkingSpot;
import Model.ParkingZone;
import Model.SpotAssignmentRequest;
import Service.ZoneAllocationService;

import java.util.Objects;

public class ZoneAllocationServiceImpl implements ZoneAllocationService {

    @Override
    public ParkingSpot assignSpot(SpotAssignmentRequest request, ParkingZone zone) {
        Objects.requireNonNull(request);
        Objects.requireNonNull(zone);

        if (!zone.getZoneType().equals(request.getRequestedZoneType())) return null;
        if (zoneAccessDenied(request, zone.getZoneType())) return null;
        if (!zone.hasFreeSpot()) return null;

        ParkingSpot spot = zone.getFirstFreeSpot();
        spot.reserve();
        return spot;
    }

    public boolean zoneAccessDenied(SpotAssignmentRequest request, ZoneType zoneType) {
        Objects.requireNonNull(request);
        if (zoneType == ZoneType.EV && !request.getSubscriptionPlan().hasEvRights()) return true;
        if (zoneType == ZoneType.VIP && !request.getSubscriptionPlan().hasVipRights()) return true;
        return false;
    }
}