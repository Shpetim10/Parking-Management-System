package Repository.impl;

import Model.ParkingZone;
import Repository.ParkingZoneRepository;

import java.util.*;

public class InMemoryParkingZoneRepository implements ParkingZoneRepository {

    private final Map<String, ParkingZone> zones = new HashMap<>();

    public InMemoryParkingZoneRepository(List<ParkingZone> initialZones) {
        if (initialZones != null) {
            for (ParkingZone zone : initialZones) {
                zones.put(zone.getZoneId(), zone);
            }
        }
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
}