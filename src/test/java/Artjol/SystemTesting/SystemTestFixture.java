package Artjol.SystemTesting;

import Controller.*;
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
                    discountRepo,
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

        // User + vehicle
        userRepo.save(new User("U1", UserStatus.ACTIVE));
        vehicleRepo.save(new Vehicle("AA123BB", "U1"));
        subscriptionRepo.save("U1", defaultPlan());

        // Zone + spot
        ParkingZone zone =
                new ParkingZone("Z1", ZoneType.STANDARD, 0.9);
        zone.addSpot(new ParkingSpot("S1", zone));
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
}