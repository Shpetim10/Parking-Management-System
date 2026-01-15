import Controller.*;
import Dto.Billing.*;
import Dto.Eligibility.*;
import Dto.Exit.*;
import Dto.Monitoring.*;
import Dto.Penalty.*;
import Dto.Zone.*;
import Enum.*;
import Model.*;
import Repository.impl.*;
import Service.impl.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        // ===============================
        // Infrastructure
        // ===============================
        var userRepo = new InMemoryUserRepository();
        var vehicleRepo = new InMemoryVehicleRepository();
        var sessionRepo = new InMemoryParkingSessionRepository();
        var zoneRepo = new InMemoryParkingZoneRepository(new ArrayList<>());
        var penaltyRepo = new InMemoryPenaltyHistoryRepository();
        var billingRepo = new InMemoryBillingRecordRepository();
        var subscriptionRepo= new InMemorySubscriptionPlanRepository();
        var discountRepo = new InMemoryDiscountPolicyRepository(
                new DiscountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0)
        );

        var tariffRepo = new InMemoryTariffRepository(Map.of(
                ZoneType.STANDARD,
                new Tariff(
                        ZoneType.STANDARD,
                        BigDecimal.valueOf(3),
                        BigDecimal.valueOf(25),
                        BigDecimal.valueOf(0.10)
                )
        ));

        var pricingConfigRepo = new InMemoryDynamicPricingConfigRepository(
                new DynamicPricingConfig(1.5, 0.7, 1.2)
        );

        // ===============================
        // Services
        // ===============================
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

        // ===============================
        // Controllers
        // ===============================
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
                billingRepo,
                sessionRepo,
                penaltyRepo,
                subscriptionRepo
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

        var monitoringController = new MonitoringController(
                monitoringService,
                penaltyRepo,
                zoneRepo
        );

        // ===============================
        // Initial zones
        // ===============================
        var zone = new ParkingZone("Z1", ZoneType.STANDARD, 0.9);
        zone.addSpot(new ParkingSpot("S-1", ZoneType.STANDARD));
        zone.addSpot(new ParkingSpot("S-2", ZoneType.STANDARD));
        zoneRepo.save(zone);

        // ===============================
        // Runtime state
        // ===============================
        ParkingSession activeSession = null;

        // ===============================
        // Console loop
        // ===============================
        while (true) {
            printMenu();
            int choice = readInt("Choose option");

            switch (choice) {

                case 1 -> {
                    String userId = read("User ID");
                    String plate = read("Vehicle plate");
                    userRepo.save(new User(userId, UserStatus.ACTIVE));
                    vehicleRepo.save(new Vehicle(plate, userId));
                    System.out.println("✅ User and vehicle registered");
                }

                case 2 -> {
                    String userId = read("User ID");
                    String plate = read("Vehicle plate");

                    var result = eligibilityController.checkEligibility(
                            new EligibilityRequestDto(
                                    userId, plate,
                                    0, 0, 0, 0,
                                    false,
                                    LocalDateTime.now()
                            )
                    );
                    System.out.println("Eligibility allowed: " + result.allowed());
                    if (!result.allowed()) {
                        System.out.println("Reason: " + result.reason());
                    }
                }

                case 3 -> {
                    String userId = read("User ID");
                    var response = zoneController.assignSpot(
                            new SpotAssignmentRequestDto(
                                    userId,
                                    ZoneType.STANDARD,
                                    false,
                                    false,
                                    Instant.now(),
                                    0.3
                            )
                    );

                    if (response == null) {
                        System.out.println("❌ No spot available");
                    } else {
                        System.out.println("🅿️ Spot assigned: " + response.spotId());
                    }
                }

                case 4 -> {
                    String sessionId = UUID.randomUUID().toString();
                    String userId = read("User ID");
                    String plate = read("Vehicle plate");

                    activeSession = new ParkingSession(
                            sessionId,
                            userId,
                            plate,
                            LocalDateTime.now()
                    );
                    activeSession.setState(SessionState.PAID);
                    sessionRepo.save(activeSession);

                    System.out.println("🚗 Parking session started");
                    System.out.println("Session ID: " + sessionId);
                }

                case 5 -> {
                    try{
                    String sessionId = read("Session ID");

                    var bill = billingController.calculateBill(
                            new BillingRequest(
                                    sessionId,
                                    ZoneType.STANDARD,
                                    DayType.WEEKDAY,
                                    TimeOfDayBand.PEAK,
                                    0.3,
                                    LocalDateTime.now(),
                                    BigDecimal.ZERO,
                                    24
                            )
                    );

                    System.out.println("💰 Billing complete");
                    System.out.println("Final price: " + bill.finalPrice());
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                }

                case 6 -> {
                    String userId = read("User ID");
                    var penalty = penaltyController.applyPenalty(
                            new ApplyPenaltyRequestDto(
                                    userId,
                                    PenaltyType.OVERSTAY,
                                    BigDecimal.valueOf(5),
                                    LocalDateTime.now()
                            )
                    );

                    System.out.println("⚠️ Penalty applied");
                    System.out.println("Total penalties: " + penalty.newTotalPenaltyAmount());
                    System.out.println("Blacklist status: " + penalty.blacklistStatus());
                }

                case 7 -> {
                    if (activeSession == null) {
                        System.out.println("❌ No active session");
                        break;
                    }

                    var exit = exitController.authorizeExit(
                            new ExitAuthorizationRequestDto(
                                    activeSession.getUserId(),
                                    activeSession.getVehiclePlate(),
                                    activeSession.getVehiclePlate()
                            )
                    );

                    System.out.println("🚦 Exit allowed: " + exit.allowed());
                    System.out.println("Reason: " + exit.reason());
                }

                case 8 -> {
                    var summary = monitoringController.generatePenaltySummary();
                    System.out.println("📊 Monitoring summary");
                    System.out.println("Overstay penalties: " + summary.totalOverstay());
                }

                case 9 -> {
                    System.out.println("👋 Exiting system");
                    return;
                }

                default -> System.out.println("❌ Invalid option");
            }

            System.out.println();
        }
    }

    // ===============================
    // Helpers
    // ===============================
    private static void printMenu() {
        System.out.println("""
                ===========================
                PARKING MANAGEMENT SYSTEM
                ===========================
                1. Register user & vehicle
                2. Check eligibility
                3. Assign parking spot
                4. Start parking session
                5. Calculate billing
                6. Apply penalty
                7. Exit parking
                8. View monitoring summary
                9. Exit
                10. Add Discount Info
                11. Register Subscription
                """);
    }

    private static String read(String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
    }

    private static int readInt(String label) {
        System.out.print(label + ": ");
        return Integer.parseInt(scanner.nextLine().trim());
    }
}