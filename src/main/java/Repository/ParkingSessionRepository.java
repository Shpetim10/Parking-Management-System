package Repository;

import Model.ParkingSession;

import java.util.List;
import java.util.Optional;

public interface ParkingSessionRepository {

    Optional<ParkingSession> findById(String sessionId);

    List<ParkingSession> findActiveSessionsForUser(String userId);

    List<ParkingSession> findActiveSessionsForVehicle(String plate);

    void save(ParkingSession session);
}