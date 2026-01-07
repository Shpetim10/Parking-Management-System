package Service.impl;

import Service.ExitAuthorizationService;
import Model.*;
import Enum.*;

public class ExitAuthorizationServiceImpl implements ExitAuthorizationService {

    @Override
    public ExitDecision authorizeExit(User user, ParkingSession session, String plate) {

        if (user.getStatus() != UserStatus.ACTIVE)
            return ExitDecision.deny(ExitFailureReason.USER_INACTIVE);

        if (session.getState() == SessionState.CLOSED)
            return ExitDecision.deny(ExitFailureReason.ALREADY_CLOSED);

        if (session.getState() != SessionState.PAID)
            return ExitDecision.deny(ExitFailureReason.SESSION_NOT_PAID);

        if (!session.getVehiclePlate().equals(plate))
            return ExitDecision.deny(ExitFailureReason.VEHICLE_MISMATCH);

        return ExitDecision.allow();
    }
}