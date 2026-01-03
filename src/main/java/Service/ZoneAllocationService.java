package Service;

import Model.ParkingSpot;
import Model.ParkingZone;
import Model.SpotAssignmentRequest;

import java.util.List;

public interface ZoneAllocationService {

    ParkingSpot assignSpot(
            SpotAssignmentRequest request,
            List<ParkingZone> zones,
            double currentOccupancyRatioForZone
    );
}