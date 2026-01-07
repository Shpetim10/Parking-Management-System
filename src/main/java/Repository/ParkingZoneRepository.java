package Repository;

import Model.ParkingZone;

import java.util.List;

public interface ParkingZoneRepository {

    ParkingZone findById(String zoneId);

    List<ParkingZone> findAll();

    void save(ParkingZone zone);
}