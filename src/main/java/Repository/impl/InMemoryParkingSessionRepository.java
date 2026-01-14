package Repository.impl;

import Enum.SessionState;
import Model.ParkingSession;
import Repository.ParkingSessionRepository;

import java.time.LocalDateTime;
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
                .filter(s -> s.getUserId().equals(userId))
                .toList();
    }

    @Override
    public List<ParkingSession> findActiveSessionsForVehicle(String plate) {
        return sessions.values().stream()
                .filter(s -> s.getState() == SessionState.OPEN)
                .filter(s -> s.getVehiclePlate().equals(plate))
                .toList();
    }

    @Override
    public void save(ParkingSession session) {
        sessions.put(session.getId(), session);
    }

    @Override
    public void delete(ParkingSession session) {
        sessions.remove(session.getId());
    }

    @Override
    public int getActiveSessionsCountForUser(String userId) {
        int cnt=0;
        for(ParkingSession session : sessions.values()){
            if(session.getUserId().equals(userId)){
                cnt++;
            }
        }
        return cnt;
    }

    @Override
    public int getActiveSessionsCountForVehicle(String plate) {
        int cnt=0;
        for(ParkingSession session : sessions.values()){
            if(session.getVehiclePlate().equals(plate)){
                cnt++;
            }
        }
        return cnt;
    }

    @Override
    public int getSessionsCountForToday(String userId) {
        int cnt=0;
        for(ParkingSession session : sessions.values()){
            if(session.getUserId().equals(userId)){
                if(session.getStartTime().getDayOfYear()== LocalDateTime.now().getDayOfYear() &&
                        session.getStartTime().getYear()== LocalDateTime.now().getYear()){
                    cnt++;
                }
            }
        }
        return cnt;
    }

    @Override
    public int getHoursUsedTodayForUser(String userId) {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        int totalMinutes = 0;

        for(ParkingSession session : sessions.values()){
            if(session.getUserId().equals(userId)){
                if(session.getStartTime().isAfter(todayStart) && session.getStartTime().isBefore(todayEnd)){
                    LocalDateTime start = session.getStartTime();
                    LocalDateTime end = LocalDateTime.now();

                    totalMinutes += (int)java.time.Duration.between(start, end).toMinutes();
                }
            }
        }

        return (int)Math.ceil(totalMinutes/60.0);
    }

    @Override
    public boolean hasUnpaidSessionsForUser(String userId) {
        for (ParkingSession session : sessions.values()){
            if(session.getUserId().equals(userId)){
                if(session.getState() == SessionState.OPEN || session.getState() == SessionState.PAYMENT_PENDING){
                    return true;
                }
            }
        }
        return false;
    }
}