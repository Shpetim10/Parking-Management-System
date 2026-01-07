package Repository.impl;

import Enum.SessionState;
import Model.ParkingSession;
import Repository.ParkingSessionRepository;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryParkingSessionRepository implements ParkingSessionRepository {

    private final Map<String, ParkingSession> sessions = new HashMap<>();

    @Override
    public Optional<ParkingSession> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public List<ParkingSession> findActiveSessionsForUser(String userId) {
        return sessions.values().stream()
                .filter(s -> s.getState() == SessionState.OPEN)
                .filter(s -> s.getVehiclePlate().equals(userId) == false)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParkingSession> findActiveSessionsForVehicle(String plate) {
        return sessions.values().stream()
                .filter(s -> s.getState() == SessionState.OPEN)
                .filter(s -> s.getVehiclePlate().equals(plate))
                .collect(Collectors.toList());
    }

    @Override
    public void save(ParkingSession session) {
        sessions.put(session.getVehiclePlate(), session);
    }
}