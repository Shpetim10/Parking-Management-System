package Repository.impl;

import Enum.SessionState;
import Model.ParkingSession;
import Repository.ParkingSessionRepository;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryParkingSessionRepository implements ParkingSessionRepository {

    private final Map<String, ParkingSession> sessions = new HashMap<>();

    @Override
    public Optional<ParkingSession> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public Collection<ParkingSession> findAll() {
        return sessions.values();
    }

    @Override
    public List<ParkingSession> findActiveSessionsForUser(String userId) {
        return sessions.values().stream()
                .filter(ParkingSession::isActive)
                .filter(s -> s.getUserId().equals(userId))
                .toList();
    }

    @Override
    public List<ParkingSession> findActiveSessionsForVehicle(String plate) {
        return sessions.values().stream()
                .filter(ParkingSession::isActive)
                .filter(s -> s.getVehiclePlate().equals(plate))
                .toList();
    }

    @Override
    public void save(ParkingSession session) {
        sessions.put(session.getId(), session);
    }

    @Override
    public void delete(ParkingSession session) {
        Objects.requireNonNull(session);
        sessions.remove(session.getId());
    }

    @Override
    public int getActiveSessionsCountForUser(String userId) {
        return (int) sessions.values().stream()
                .filter(ParkingSession::isActive)
                .filter(s -> s.getUserId().equals(userId))
                .count();
    }

    @Override
    public int getActiveSessionsCountForVehicle(String plate) {
        return (int) sessions.values().stream()
                .filter(ParkingSession::isActive)
                .filter(s -> s.getVehiclePlate().equals(plate))
                .count();
    }

    @Override
    public int getSessionsCountForToday(String userId) {
        LocalDateTime today = LocalDateTime.now();
        return (int) sessions.values().stream()
                .filter(s -> s.getUserId().equals(userId))
                .filter(s -> s.getStartTime().toLocalDate().equals(today.toLocalDate()))
                .count();
    }

    @Override
    public int getHoursUsedTodayForUser(String userId) {
        LocalDateTime now = LocalDateTime.now();
        int minutes = 0;

        for (ParkingSession session : sessions.values()) {
            if (session.getUserId().equals(userId)) {
                LocalDateTime end = session.getEndTime() != null
                        ? session.getEndTime()
                        : now;

                minutes += java.time.Duration
                        .between(session.getStartTime(), end)
                        .toMinutes();
            }
        }

        return (int) Math.ceil(minutes / 60.0);
    }

    @Override
    public boolean hasUnpaidSessionsForUser(String userId) {
        return sessions.values().stream()
                .anyMatch(s ->
                        s.getUserId().equals(userId) &&
                                s.getState() == SessionState.OPEN
                );
    }
}