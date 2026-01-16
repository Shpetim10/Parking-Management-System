package Repository;

import Model.ParkingSpot;
import Model.ParkingZone;

import java.util.List;

public interface ParkingZoneRepository {
    boolean zoneExists(String id);

    ParkingZone findById(String zoneId);

    List<ParkingZone> findAll();

    void save(ParkingZone zone);

    boolean spotExists(String zoneId);
    ParkingZone findZoneById(String zoneId);

    ParkingSpot findSpotById(String spotId);
}