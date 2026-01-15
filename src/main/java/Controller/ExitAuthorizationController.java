package Controller;

import Dto.Exit.*;
import Model.*;
import Repository.*;
import Service.ExitAuthorizationService;

import java.util.Objects;

public class ExitAuthorizationController {

    private final ExitAuthorizationService exitAuthorizationService;
    private final UserRepository userRepository;
    private final ParkingSessionRepository sessionRepository;

    public ExitAuthorizationController(
            ExitAuthorizationService exitAuthorizationService,
            UserRepository userRepository,
            ParkingSessionRepository sessionRepository
    ) {
        this.exitAuthorizationService = Objects.requireNonNull(exitAuthorizationService);
        this.userRepository = Objects.requireNonNull(userRepository);
        this.sessionRepository = Objects.requireNonNull(sessionRepository);
    }

    public ExitAuthorizationResponseDto authorizeExit(ExitAuthorizationRequestDto dto) {

        User user = userRepository.findById(dto.userId()).orElseThrow();
        ParkingSession session = sessionRepository.findById(dto.sessionId()).orElseThrow();

        var decision = exitAuthorizationService.authorizeExit(
                user,
                session,
                dto.plateAtGate()
        );

        if(decision.isAllowed())
            sessionRepository.delete(session);

        return new ExitAuthorizationResponseDto(
                decision.isAllowed(),
                decision.getReason()
        );
    }
}