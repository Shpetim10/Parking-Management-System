package SystemTesting;

import Controller.*;
import Dto.Session.StartSessionRequestDto;
import Dto.Session.StartSessionResponseDto;
import Dto.Zone.SpotAssignmentRequestDto;
import Dto.Zone.SpotAssignmentResponseDto;
import Enum.*;
import Model.*;
import Repository.*;
import Repository.impl.*;
import Service.ZoneOccupancyService;
import Service.impl.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class SystemTestFixture {

    // ===================== REPOSITORIES =====================
    public final UserRepository userRepo = new InMemoryUserRepository();
    public final VehicleRepository vehicleRepo = new InMemoryVehicleRepository();
    public final InMemoryParkingSessionRepository sessionRepo =
            new InMemoryParkingSessionRepository();
    public final ParkingZoneRepository zoneRepo =
            new InMemoryParkingZoneRepository();
    public final BillingRecordRepository billingRepo =
            new InMemoryBillingRecordRepository();
    public final PenaltyHistoryRepository penaltyRepo =
            new InMemoryPenaltyHistoryRepository();
    public final InMemorySubscriptionPlanRepository subscriptionRepo =
            new InMemorySubscriptionPlanRepository();
    public final DiscountPolicyRepository discountRepo =
            new InMemoryDiscountPolicyRepository(
                    new DiscountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0)
            );

    public final TariffRepository tariffRepo =
            new InMemoryTariffRepository(
                    Map.of(
                            ZoneType.STANDARD,
                            new Tariff(
                                    ZoneType.STANDARD,
                                    BigDecimal.valueOf(2.0),
                                    BigDecimal.valueOf(20.0),
                                    BigDecimal.ZERO
                            )
                    )
            );

    public final DynamicPricingConfigRepository pricingRepo =
            new InMemoryDynamicPricingConfigRepository(
                    new DynamicPricingConfig(1.5, 1.0, 0.9)
            );

    // ===================== SERVICES =====================
    public final EligibilityServiceImpl eligibilityService =
            new EligibilityServiceImpl();

    public final ZoneAllocationServiceImpl zoneAllocationService =
            new ZoneAllocationServiceImpl();

    public final PenaltyServiceImpl penaltyService =
            new PenaltyServiceImpl();

    public final MonitoringServiceImpl monitoringService =
            new MonitoringServiceImpl();

    public final ExitAuthorizationServiceImpl exitService =
            new ExitAuthorizationServiceImpl();

    public final DefaultBillingService billingService =
            new DefaultBillingService(
                    new DefaultDurationCalculator(),
                    new DefaultPricingService(),
                    new DefaultDiscountAndCapService(),
                    new DefaultTaxService()
            );

    public final ZoneOccupancyService occupancyService =
            new ZoneOccupancyServiceImpl(zoneRepo, sessionRepo);

    // ===================== CONTROLLERS =====================
    public final EligibilityController eligibilityController =
            new EligibilityController(
                    eligibilityService,
                    userRepo,
                    vehicleRepo,
                    subscriptionRepo
            );

    public final ZoneAllocationController zoneController =
            new ZoneAllocationController(
                    zoneAllocationService,
                    zoneRepo,
                    occupancyService,
                    subscriptionRepo
            );

    public final ParkingSessionController sessionController =
            new ParkingSessionController(sessionRepo, zoneRepo);

    public final BillingController billingController =
            new BillingController(
                    billingService,
                    tariffRepo,
                    pricingRepo,
                    billingRepo,
                    sessionRepo,
                    penaltyRepo,
                    subscriptionRepo
            );

    public final PenaltyController penaltyController =
            new PenaltyController(
                    penaltyService,
                    monitoringService,
                    penaltyRepo
            );

    public final ExitAuthorizationController exitController =
            new ExitAuthorizationController(
                    exitService,
                    userRepo,
                    sessionRepo,
                    zoneRepo
            );

    // ===================== SEED DATA =====================
    public void seedBaseData() {

        // ===== USERS + VEHICLES =====
        userRepo.save(new User("U1", UserStatus.ACTIVE));
        vehicleRepo.save(new Vehicle("AA123BB", "U1"));
        subscriptionRepo.save("U1", defaultPlan());

        userRepo.save(new User("U2", UserStatus.ACTIVE));
        vehicleRepo.save(new Vehicle("BB234CC", "U2"));
        subscriptionRepo.save("U2", defaultPlan());

        userRepo.save(new User("U3", UserStatus.ACTIVE));
        vehicleRepo.save(new Vehicle("CC345DD", "U3"));
        subscriptionRepo.save("U3", defaultPlan());

        // ===== ZONE + MULTIPLE SPOTS =====
        ParkingZone zone = new ParkingZone("Z1", ZoneType.STANDARD, 0.9);
        zone.addSpot(new ParkingSpot("S1", zone));
        zone.addSpot(new ParkingSpot("S2", zone));
        zone.addSpot(new ParkingSpot("S3", zone));
        zoneRepo.save(zone);
    }

    private SubscriptionPlan defaultPlan() {
        return new SubscriptionPlan(
                1, 1, 10, 8,
                false, false, false,
                new DiscountInfo(
                        BigDecimal.valueOf(0.10),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        false,
                        0
                )
        );
    }

    public ParkingSession createActiveSession(
            String userId,
            String vehiclePlate,
            ZoneType zoneType
    ) {
        // 1️Assign parking spot
        SpotAssignmentResponseDto assignment =
                zoneController.assignSpot(
                        new SpotAssignmentRequestDto(
                                userId,
                                zoneType,
                                LocalDateTime.now()
                        )
                );

        if (assignment == null) {
            throw new IllegalStateException("No spot available for zone: " + zoneType);
        }

        // 2️Start parking session
        StartSessionResponseDto startResponse =
                sessionController.startSession(
                        new StartSessionRequestDto(
                                userId,
                                vehiclePlate,
                                assignment.zoneId(),
                                assignment.spotId(),
                                assignment.zoneType(),
                                false,
                                LocalDateTime.now()
                        )
                );

        // 3️⃣ Return active session from repository
        return sessionRepo.findById(startResponse.sessionId())
                .orElseThrow(() ->
                        new IllegalStateException("Session was not created properly"));
    }

    public final MonitoringController monitoringController =
            new MonitoringController(
                    monitoringService,
                    penaltyRepo,
                    zoneRepo
            );
}

