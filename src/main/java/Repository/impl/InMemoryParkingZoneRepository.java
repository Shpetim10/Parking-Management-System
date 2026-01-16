package Repository.impl;

import Model.ParkingSpot;
import Model.ParkingZone;
import Repository.ParkingZoneRepository;

import java.util.*;

public class InMemoryParkingZoneRepository implements ParkingZoneRepository {

    private final Map<String, ParkingZone> zones = new HashMap<>();

    public InMemoryParkingZoneRepository() {

    }

    @Override
    public boolean zoneExists(String id) {
        return zones.containsKey(id);
    }

    @Override
    public ParkingZone findById(String zoneId) {
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        ParkingZone zone = zones.get(zoneId);
        if (zone == null) {
            throw new IllegalArgumentException("No parking zone found with id: " + zoneId);
        }
        return zone;
    }

    @Override
    public List<ParkingZone> findAll() {
        return new ArrayList<>(zones.values());
    }

    @Override
    public void save(ParkingZone zone) {
        Objects.requireNonNull(zone, "zone must not be null");
        zones.put(zone.getZoneId(), zone);
    }

    @Override
    public boolean spotExists(String spotId) {
        for(ParkingZone zone : zones.values()){
            for (ParkingSpot spot: zone.getSpots()){
                if(spot.getSpotId().equals(spotId)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ParkingZone findZoneById(String zoneId) {
        for(ParkingZone zone : zones.values()){
            if(zone.getZoneId().equals(zoneId)){
                return zone;
            }
        }
        return null;
    }

    @Override
    public ParkingSpot findSpotById(String spotId) {
        for(ParkingZone zone : zones.values()){
            for (ParkingSpot spot: zone.getSpots()){
                if(spot.getSpotId().equals(spotId)){
                    return spot;
                }
            }
        }
        return null;
    }
}