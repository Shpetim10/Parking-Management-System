package Repository;

import Model.ParkingSession;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ParkingSessionRepository {

    Optional<ParkingSession> findById(String sessionId);
    Collection<ParkingSession> findAll();

    List<ParkingSession> findActiveSessionsForUser(String userId);

    List<ParkingSession> findActiveSessionsForVehicle(String plate);

    void save(ParkingSession session);

    void delete(ParkingSession session);

    int getActiveSessionsCountForUser(String userId);
    int getActiveSessionsCountForVehicle(String plate);
    int getSessionsCountForToday(String userId);
    int getHoursUsedTodayForUser(String userId);
    boolean hasUnpaidSessionsForUser(String userId);

}