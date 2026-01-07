package Controller;

import Dto.Eligibility.*;
import Model.*;
import Repository.*;
import Service.EligibilityService;

import java.util.Objects;

public class EligibilityController {

    private final EligibilityService eligibilityService;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final SubscriptionPlanRepository planRepository;

    public EligibilityController(
            EligibilityService eligibilityService,
            UserRepository userRepository,
            VehicleRepository vehicleRepository,
            SubscriptionPlanRepository planRepository
    ) {
        this.eligibilityService = Objects.requireNonNull(eligibilityService);
        this.userRepository = Objects.requireNonNull(userRepository);
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository);
        this.planRepository = Objects.requireNonNull(planRepository);
    }

    public EligibilityResponseDto checkEligibility(EligibilityRequestDto dto) {

        User user = userRepository.findById(dto.userId()).orElseThrow();
        Vehicle vehicle = vehicleRepository.findByPlate(dto.vehiclePlate()).orElseThrow();
        SubscriptionPlan plan = planRepository.getPlanForUser(dto.userId());

        var result = eligibilityService.canStartSession(
                user,
                vehicle,
                dto.activeSessionsForVehicle(),
                dto.totalActiveSessionsForUser(),
                dto.sessionsStartedToday(),
                dto.hoursUsedToday(),
                plan,
                dto.hasUnpaidSessions(),
                dto.now()
        );

        return new EligibilityResponseDto(result.isAllowed(), result.getReason());
    }
}