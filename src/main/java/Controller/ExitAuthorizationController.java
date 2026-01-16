package Controller;

import Dto.Exit.*;
import Model.*;
import Repository.ParkingSessionRepository;
import Repository.ParkingZoneRepository;
import Repository.UserRepository;
import Service.ExitAuthorizationService;
import Enum.ExitFailureReason;

import java.time.LocalDateTime;
import java.util.Objects;
public class ExitAuthorizationController {

    private final ExitAuthorizationService exitAuthorizationService;
    private final UserRepository userRepository;
    private final ParkingSessionRepository sessionRepository;
    private final ParkingZoneRepository zoneRepository;

    public ExitAuthorizationController(
            ExitAuthorizationService exitAuthorizationService,
            UserRepository userRepository,
            ParkingSessionRepository sessionRepository,
            ParkingZoneRepository zoneRepository
    ) {
        this.exitAuthorizationService = Objects.requireNonNull(exitAuthorizationService);
        this.userRepository = Objects.requireNonNull(userRepository);
        this.sessionRepository = Objects.requireNonNull(sessionRepository);
        this.zoneRepository = Objects.requireNonNull(zoneRepository);
    }

    public ExitAuthorizationResponseDto authorizeExit(ExitAuthorizationRequestDto dto) {

        User user = userRepository.findById(dto.userId()).orElse(null);
        if (user == null) {
            return new ExitAuthorizationResponseDto(false, ExitFailureReason.USER_INACTIVE);
        }

        ParkingSession session =
                sessionRepository.findById(dto.sessionId()).orElse(null);

        if (session == null) {
            return new ExitAuthorizationResponseDto(false, ExitFailureReason.ALREADY_CLOSED);
        }

        var decision = exitAuthorizationService.authorizeExit(
                user,
                session,
                dto.plateAtGate()
        );

        if (decision.isAllowed()) {

            session.close(LocalDateTime.now());
            sessionRepository.save(session);

            ParkingZone zone = zoneRepository.findById(session.getZoneId());

            zone.getSpots().stream()
                    .filter(s -> s.getSpotId().equals(session.getSpotId()))
                    .findFirst()
                    .ifPresent(spot -> {
                        if (spot.isOccupied()) {
                            spot.release();
                        }
                    });
        }

        return new ExitAuthorizationResponseDto(
                decision.isAllowed(),
                decision.getReason()
        );
    }
}