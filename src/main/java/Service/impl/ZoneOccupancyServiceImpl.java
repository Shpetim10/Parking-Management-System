package Service.impl;

import Model.ParkingSession;
import Repository.ParkingSessionRepository;
import Repository.ParkingZoneRepository;
import Service.ZoneOccupancyService;

import java.util.Objects;

public class ZoneOccupancyServiceImpl implements ZoneOccupancyService {

    private final ParkingZoneRepository zoneRepo;
    private final ParkingSessionRepository sessionRepo;

    public ZoneOccupancyServiceImpl(
            ParkingZoneRepository zoneRepo,
            ParkingSessionRepository sessionRepo
    ) {
        this.zoneRepo = Objects.requireNonNull(zoneRepo);
        this.sessionRepo = Objects.requireNonNull(sessionRepo);
    }

    @Override
    public double calculateOccupancyRatioForZone(String zoneId) {
        long totalSpots = zoneRepo.findById(zoneId).getTotalSpots();

        long occupied = sessionRepo.findAll().stream()
                .filter(ParkingSession::isActive)
                .filter(s -> s.getZoneId().equals(zoneId))
                .count();

        return totalSpots == 0 ? 0.0 : (double) occupied / totalSpots;
    }
}