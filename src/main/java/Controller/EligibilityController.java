package Controller;

import Dto.Eligibility.*;
import Model.*;
import Repository.*;
import Service.EligibilityService;

import java.util.NoSuchElementException;
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
        Objects.requireNonNull(dto);

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + dto.userId()));

        Vehicle vehicle = vehicleRepository.findByPlate(dto.vehiclePlate())
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found: " + dto.vehiclePlate()));

        SubscriptionPlan plan = planRepository.getPlanForUser(dto.userId())
                .orElseThrow(() -> new NoSuchElementException("No subscription plan for user: " + dto.userId()));


        EligibilityResult result = eligibilityService.canStartSession(
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

        Objects.requireNonNull(result, "Something went wrong!");

        return new EligibilityResponseDto(result.isAllowed(), result.getReason());
    }
}