package Service;

import Model.*;

public interface ExitAuthorizationService {

    ExitDecision authorizeExit(
            User user,
            ParkingSession session,
            String plateAtGate
    );
}
