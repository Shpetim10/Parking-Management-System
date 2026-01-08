import Controller.*;
import Dto.Billing.*;
import Dto.Eligibility.*;
import Dto.Exit.*;
import Dto.Monitoring.*;
import Dto.Penalty.*;
import Dto.Zone.SpotAssignmentRequestDto;
import Dto.Zone.SpotAssignmentResponseDto;
import Enum.*;
import Model.*;
import Record.DurationInfo;
import Repository.*;
import Repository.impl.*;
import Service.*;
import Service.Billing.*;
import Service.Billing.impl.*;
import Service.impl.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


    public class Main {

        public static void main(String[] args) {

            // ----------------------------
            // 1. Repositories
            // ----------------------------
            var userRepo = new InMemoryUserRepository();
            var vehicleRepo = new InMemoryVehicleRepository();
            var sessionRepo = new InMemoryParkingSessionRepository();
            var zoneRepo = new InMemoryParkingZoneRepository(new ArrayList<>());
            var penaltyRepo = new InMemoryPenaltyHistoryRepository();

            var discountRepo = new InMemoryDiscountPolicyRepository(
                    new DiscountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0)
            );

            var tariffRepo = new InMemoryTariffRepository(Map.of(
                    ZoneType.STANDARD,
                    new Tariff(
                            ZoneType.STANDARD,
                            BigDecimal.valueOf(3),
                            BigDecimal.valueOf(20),
                            false,
                            null,
                            BigDecimal.valueOf(0.1)
                    )
            ));

            var pricingConfigRepo = new InMemoryDynamicPricingConfigRepository(
                    new DynamicPricingConfig(1.5, 1.0, 0.7, 1.2)
            );

            // ----------------------------
            // 2. Services
            // ----------------------------
            var eligibilityService = new EligibilityServiceImpl();
            var zoneService = new ZoneAllocationServiceImpl();
            var penaltyService = new PenaltyServiceImpl();
            var monitoringService = new MonitoringServiceImpl();
            var exitService = new ExitAuthorizationServiceImpl();

            var billingService = new DefaultBillingService(
                    new DefaultDurationCalculator(),
                    new DefaultPricingService(),
                    new DefaultDiscountAndCapService(),
                    new DefaultTaxService()
            );

            // ----------------------------
            // 3. Controllers
            // ----------------------------
            var eligibilityController = new EligibilityController(
                    eligibilityService,
                    userRepo,
                    vehicleRepo,
                    userId -> new SubscriptionPlan(
                            2, 1, 5, 8,
                            false, null, null,
                            false
                    )
            );

            var zoneController = new ZoneAllocationController(zoneService, zoneRepo);
            var billingController = new BillingController(
                    billingService,
                    tariffRepo,
                    pricingConfigRepo,
                    discountRepo,
                    new InMemoryBillingRecordRepository()
            );

            var penaltyController = new PenaltyController(
                    penaltyService,
                    monitoringService,
                    penaltyRepo
            );

            var exitController = new ExitAuthorizationController(
                    exitService,
                    userRepo,
                    sessionRepo
            );

            // ----------------------------
            // 4. Setup Data
            // ----------------------------
            var user = new User("U1", UserStatus.ACTIVE);
            var vehicle = new Vehicle("AA-123", "U1");

            userRepo.save(user);
            vehicleRepo.save(vehicle);

            var zone = new ParkingZone("Z1", ZoneType.STANDARD, 0.9);
            zone.addSpot(new ParkingSpot("S-1", ZoneType.STANDARD));
            zoneRepo.save(zone);

            System.out.println("✅ User and vehicle registered");

            // ----------------------------
            // 5. Eligibility Check
            // ----------------------------
            var eligibility = eligibilityController.checkEligibility(
                    new EligibilityRequestDto(
                            "U1", "AA-123",
                            0, 0, 0, 0,
                            false,
                            Instant.now()
                    )
            );

            System.out.println("Eligibility allowed: " + eligibility.allowed());
            if (!eligibility.allowed()) return;

            // ----------------------------
            // 6. Spot Assignment
            // ----------------------------
            var spotResponse = zoneController.assignSpot(
                    new SpotAssignmentRequestDto(
                            "U1", ZoneType.STANDARD,
                            false, false,
                            Instant.now(),
                            0.4
                    )
            );

            System.out.println("🅿️ Spot assigned: " + spotResponse.spotId());

            // ----------------------------
            // 7. Start Session
            // ----------------------------
            var session = new ParkingSession("S1", "U1", "AA-123", Instant.now());
            session.setState(SessionState.PAID);
            sessionRepo.save(session);

            System.out.println("🚗 Parking session started");

            // ----------------------------
            // 8. Billing
            // ----------------------------
            var billing = billingController.calculateBill(
                    new BillingRequest(
                            "S1",
                            "U1",
                            ZoneType.STANDARD,
                            DayType.WEEKDAY,
                            TimeOfDayBand.PEAK,
                            0.4,
                            LocalDateTime.now().minusHours(3),
                            LocalDateTime.now(),
                            BigDecimal.ZERO,
                            24,
                            BigDecimal.valueOf(20),
                            BigDecimal.valueOf(0.2)
                    )
            );

            System.out.println("💰 Billing completed");
            System.out.println("Base price: " + billing.basePrice());
            System.out.println("Final price: " + billing.finalPrice());

            // ----------------------------
            // 9. Apply Penalty
            // ----------------------------
            var penaltyResult = penaltyController.applyPenalty(
                    new ApplyPenaltyRequestDto(
                            "U1",
                            PenaltyType.OVERSTAY,
                            BigDecimal.valueOf(5),
                            Instant.now()
                    )
            );

            System.out.println("⚠️ Penalty applied");
            System.out.println("Total penalties: " + penaltyResult.newTotalPenaltyAmount());

            // ----------------------------
            // 10. Exit Authorization
            // ----------------------------
            var exit = exitController.authorizeExit(
                    new ExitAuthorizationRequestDto("U1", "S1", "AA-123")
            );

            System.out.println("🚦 Exit allowed: " + exit.allowed());
            System.out.println("Exit reason: " + exit.reason());
        }
    }