import Controller.*;
import Dto.Billing.*;
import Dto.DiscountInfo.DiscountInfoDto;
import Dto.Eligibility.*;
import Dto.Exit.*;
import Dto.Monitoring.*;
import Dto.Penalty.*;
import Dto.Session.*;
import Dto.Zone.*;
import Enum.*;
import Model.*;
import Repository.*;
import Repository.impl.*;
import Service.ZoneOccupancyService;
import Service.impl.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    // Runtime state (console simulation)
    private static SpotAssignmentResponseDto lastAssignedSpot;
    private static String activeSessionId;

    public static void main(String[] args) {

        // ==========================================================
        // REPOSITORIES
        // ==========================================================
        UserRepository userRepo = new InMemoryUserRepository();
        VehicleRepository vehicleRepo = new InMemoryVehicleRepository();
        InMemoryParkingSessionRepository sessionRepo = new InMemoryParkingSessionRepository();
        ParkingZoneRepository zoneRepo = new InMemoryParkingZoneRepository();
        PenaltyHistoryRepository penaltyRepo = new InMemoryPenaltyHistoryRepository();
        BillingRecordRepository billingRepo = new InMemoryBillingRecordRepository();
        InMemorySubscriptionPlanRepository subscriptionRepo = new InMemorySubscriptionPlanRepository();

        DiscountPolicyRepository discountRepo =
                new InMemoryDiscountPolicyRepository(
                        new DiscountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0)
                );

        TariffRepository tariffRepo = new InMemoryTariffRepository(Map.of(
                ZoneType.STANDARD,
                new Tariff(
                        ZoneType.STANDARD,
                        BigDecimal.valueOf(3),
                        BigDecimal.valueOf(25),
                        BigDecimal.valueOf(0.10)
                )
        ));

        DynamicPricingConfigRepository pricingRepo =
                new InMemoryDynamicPricingConfigRepository(
                        new DynamicPricingConfig(1.5, 1.0, 0.7)
                );

        // ==========================================================
        // SERVICES
        // ==========================================================
        var eligibilityService = new EligibilityServiceImpl();
        var zoneAllocationService = new ZoneAllocationServiceImpl();
        var penaltyService = new PenaltyServiceImpl();
        var monitoringService = new MonitoringServiceImpl();
        var exitService = new ExitAuthorizationServiceImpl();
        var parkingZoneController = new ParkingZoneController(zoneRepo);
        var billingService = new DefaultBillingService(
                new DefaultDurationCalculator(),
                new DefaultPricingService(),
                new DefaultDiscountAndCapService(),
                new DefaultTaxService()
        );

        ZoneOccupancyService occupancyService =
                new ZoneOccupancyServiceImpl(zoneRepo, sessionRepo);

        // ==========================================================
        // CONTROLLERS
        // ==========================================================
        var eligibilityController = new EligibilityController(
                eligibilityService,
                userRepo,
                vehicleRepo,
                subscriptionRepo
        );

        var zoneController = new ZoneAllocationController(
                zoneAllocationService,
                zoneRepo,
                occupancyService,
                subscriptionRepo
        );

        var sessionController =
                new ParkingSessionController(sessionRepo, zoneRepo);

        var billingController = new BillingController(
                billingService,
                tariffRepo,
                pricingRepo,
                discountRepo,
                billingRepo,
                sessionRepo,
                penaltyRepo,
                subscriptionRepo
        );

        var penaltyController =
                new PenaltyController(penaltyService, monitoringService, penaltyRepo);

        var exitController =
                new ExitAuthorizationController(exitService, userRepo, sessionRepo, zoneRepo);

        var monitoringController =
                new MonitoringController(monitoringService, penaltyRepo, zoneRepo);

        var discountController =
                new DiscountInfoController(discountRepo);

        // ==========================================================
        // INITIAL DATA
        // ==========================================================
        seedZones(zoneRepo);
        seedUsersAndVehicles(userRepo, vehicleRepo, subscriptionRepo);

        // ==========================================================
        // CONSOLE LOOP (11 OPTIONS)
        // ==========================================================
        while (true) {
            printMenu();
            int choice = readInt("Choose option");

            switch (choice) {

                // 1️⃣ Register user & vehicle
                case 1 -> {
                    String userId = read("User ID");
                    String plate = read("Vehicle plate");

                    userRepo.save(new User(userId, UserStatus.ACTIVE));
                    vehicleRepo.save(new Vehicle(plate, userId));

                    if (subscriptionRepo.getPlanForUser(userId) == null) {
                        subscriptionRepo.save(userId, defaultPlan());
                    }

                    System.out.println("✅ User and vehicle registered");
                }

                // 2️⃣ Eligibility check
                case 2 -> {
                    String userId = read("User ID");
                    String plate = read("Vehicle plate");

                    try{
                        EligibilityRequestDto dto =
                                new EligibilityRequestDto(
                                        userId,
                                        plate,
                                        sessionRepo.getActiveSessionsCountForVehicle(plate),
                                        sessionRepo.getActiveSessionsCountForUser(userId),
                                        sessionRepo.getSessionsCountForToday(userId),
                                        sessionRepo.getHoursUsedTodayForUser(userId),
                                        sessionRepo.hasUnpaidSessionsForUser(userId),
                                        LocalDateTime.now()
                                );

                        EligibilityResponseDto res =
                                eligibilityController.checkEligibility(dto);

                        System.out.println("Eligibility allowed: " + res.allowed());
                        if (!res.allowed()) {
                            System.out.println("Reason: " + res.reason());
                        }
                    }catch (Exception e){
                        System.out.println("No such user with this vehicle exists!");
                    }

                }

                // 3️⃣ Assign parking spot
                case 3 -> {
                    String userId = read("User ID");
                    String zoneTypeInput = read("Zone type (STANDARD / EV / VIP)").toUpperCase();

                    ZoneType requestedZoneType;
                    try {
                        requestedZoneType = ZoneType.valueOf(zoneTypeInput);
                    } catch (IllegalArgumentException e) {
                        System.out.println("❌ Invalid zone type");
                        break; // exits case 3 safely
                    }

                    lastAssignedSpot =
                            zoneController.assignSpot(
                                    new SpotAssignmentRequestDto(
                                            userId,
                                            requestedZoneType,
                                            LocalDateTime.now()
                                    )
                            );

                    if (lastAssignedSpot == null) {
                        System.out.println("❌ No spot available in " + requestedZoneType);
                    } else {
                        System.out.println("🅿️ Spot assigned: " + lastAssignedSpot.spotId());
                    }
                }
                // TODO: PETRI NET FOR MAIN
                // 4️⃣ Start parking session
                case 4 -> {
                    if (lastAssignedSpot == null) {
                        System.out.println("❌ Assign a spot first");
                        break;
                    }

                    String userId = read("User ID");
                    String plate = read("Vehicle plate");
                    boolean isHoliday= readBoolean("IsHoliday()");

                    StartSessionResponseDto res =
                            sessionController.startSession(
                                    new StartSessionRequestDto(
                                            userId,
                                            plate,
                                            lastAssignedSpot.zoneId(),
                                            lastAssignedSpot.spotId(),
                                            lastAssignedSpot.zoneType(),
                                            isHoliday,
                                            LocalDateTime.now()
                                    )
                            );

                    activeSessionId = res.sessionId();
                    System.out.println("🚗 Session started: " + activeSessionId);
                }

                // 5️⃣ Calculate billing
                case 5 -> {
                    if (activeSessionId == null) {
                        System.out.println("❌ No active session");
                        break;
                    }

                    ParkingSession session =
                            sessionRepo.findById(activeSessionId).orElseThrow();

                    double occupancy =
                            occupancyService.calculateOccupancyRatioForZone(session.getZoneId());

                    BillingResponse bill =
                            billingController.calculateBill(
                                    new BillingRequest(
                                            activeSessionId,
                                            session.getZoneType(),
                                            session.getDayType(),
                                            session.getTimeOfDayBand(),
                                            occupancy,
                                            LocalDateTime.now(),
                                            BigDecimal.ZERO,
                                            24
                                    )
                            );

                    System.out.println("💰 Final price: " + bill.finalPrice());
                }

                // 6️⃣ Apply penalty
                case 6 -> {
                    String userId = read("User ID");
                    BigDecimal amount = readBigDecimal("Penalty amount");

                    ApplyPenaltyResponseDto res =
                            penaltyController.applyPenalty(
                                    new ApplyPenaltyRequestDto(
                                            userId,
                                            PenaltyType.OVERSTAY,
                                            amount,
                                            LocalDateTime.now()
                                    )
                            );

                    System.out.println("⚠️ Penalty applied");
                    System.out.println("Blacklist status: " + res.blacklistStatus());
                }

                // 7️⃣ Exit parking
                case 7 -> {
                    if (activeSessionId == null) {
                        System.out.println("❌ No active session");
                        break;
                    }

                    String userId = read("User ID");
                    String plate = read("Plate at gate");

                    ExitAuthorizationResponseDto exit =
                            exitController.authorizeExit(
                                    new ExitAuthorizationRequestDto(
                                            userId,
                                            activeSessionId,
                                            plate
                                    )
                            );

                    System.out.println("🚦 Exit allowed: " + exit.allowed());
                    System.out.println("Reason: " + exit.reason());

                    if (exit.allowed()) {
                        activeSessionId = null;
                        lastAssignedSpot = null;
                    }
                }

                // 8️⃣ Monitoring summary
                case 8 -> {
                    PenaltySummaryResponseDto summary =
                            monitoringController.generatePenaltySummary();

                    System.out.println("📊 Penalty summary");
                    System.out.println("Overstay: " + summary.totalOverstay());
                }

                // 9️⃣ Exit system
                case 9 -> {
                    System.out.println("👋 Goodbye");
                    return;
                }

                // 🔟 Add discount info
                case 10 -> {
                    String userId = read("User ID");
                    BigDecimal percent = readBigDecimal("Subscription discount %");

                    discountController.saveDiscountForUser(
                            userId,
                            new DiscountInfoDto(
                                    percent,
                                    BigDecimal.ZERO,
                                    BigDecimal.ZERO,
                                    false,
                                    0
                            )
                    );

                    System.out.println("✅ Discount saved");
                }

                // 1️⃣1️⃣ Register subscription
                case 11 -> {
                    String userId = read("User ID");
                    subscriptionRepo.save(userId, defaultPlan());
                    System.out.println("✅ Subscription registered");
                }
                // Create zone
                case 12 -> {
                    String zoneId = read("Zone ID");
                    String zoneType = read("Type(STANDARD, EV, VIP)").toUpperCase();
                    double threshold= readDouble("Limit [0,1]: ");
                    try{
                        parkingZoneController.createParkingZone(new ParkingZoneDto(zoneId, zoneType, threshold));
                    }catch(Exception e){
                        System.out.println(e.getMessage());
                    }

                    System.out.println("Zone added!");
                }

                // add spot
                case 13 -> {
                    String zoneId = read("Zone ID");
                    String spotId = read("Spot ID");

                    try{
                        parkingZoneController.addSpot(new ParkingSpotDto(zoneId,spotId));
                    }catch(Exception e){
                        System.out.println(e.getMessage());
                    }
                }



                default -> System.out.println("❌ Invalid option");
            }
            System.out.println();
        }
    }

    // ==========================================================
    // HELPERS
    // ==========================================================
    private static void seedZones(ParkingZoneRepository zoneRepo) {

        // STANDARD ZONE
        ParkingZone standardZone = new ParkingZone("Z-STANDARD", ZoneType.STANDARD, 0.9);
        ParkingSpot s1 = new ParkingSpot("S-1", standardZone);
        ParkingSpot s2 = new ParkingSpot("S-2", standardZone);
        standardZone.addSpot(s1);
        standardZone.addSpot(s2);

        // EV ZONE
        ParkingZone evZone = new ParkingZone("Z-EV", ZoneType.EV, 0.8);
        ParkingSpot ev1 = new ParkingSpot("EV-1", evZone);
        ParkingSpot ev2 = new ParkingSpot("EV-2", evZone);
        evZone.addSpot(ev1);
        evZone.addSpot(ev2);

        // VIP ZONE
        ParkingZone vipZone = new ParkingZone("Z-VIP", ZoneType.VIP, 0.7);
        ParkingSpot vip1 = new ParkingSpot("VIP-1", vipZone);
        vipZone.addSpot(vip1);

        zoneRepo.save(standardZone);
        zoneRepo.save(evZone);
        zoneRepo.save(vipZone);

        System.out.println("✅ Zones & spots seeded");
    }


    private static void seedUsersAndVehicles(
            UserRepository userRepo,
            VehicleRepository vehicleRepo,
            InMemorySubscriptionPlanRepository subscriptionRepo
    ) {
        // User 1
        userRepo.save(new User("U1", UserStatus.ACTIVE));
        vehicleRepo.save(new Vehicle("AA-111", "U1"));
        subscriptionRepo.save("U1", defaultPlan());

        // User 2 (EV user)
        userRepo.save(new User("U2", UserStatus.ACTIVE));
        vehicleRepo.save(new Vehicle("EV-222", "U2"));
        subscriptionRepo.save("U2", defaultPlan());

        // User 3 (VIP user)
        userRepo.save(new User("U3", UserStatus.ACTIVE));
        vehicleRepo.save(new Vehicle("VIP-333", "U3"));
        subscriptionRepo.save("U3", defaultPlan());

        System.out.println("✅ Users, vehicles & subscriptions seeded");
    }

    private static SubscriptionPlan defaultPlan() {
        return new SubscriptionPlan(
                1,
                1,
                5,
                8,
                false,
                false,
                false,
                new DiscountInfo(new BigDecimal("0.1"),
                        new BigDecimal("0"),
                        new BigDecimal("0"),
                        false,
                        0));
    }

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
                12. Add parking zone
                13. Add parking spot
                """);
    }

    private static String read(String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
    }

    private static int readInt(String label) {
        System.out.print(label + ": ");
        return Integer.parseInt(scanner.nextLine());
    }

    private static BigDecimal readBigDecimal(String label) {
        System.out.print(label + ": ");
        return new BigDecimal(scanner.nextLine());
    }

    private static double readDouble(String label) {
        System.out.print(label + ": ");
        return Double.parseDouble(scanner.nextLine());
    }

    private static boolean readBoolean(String label) {
        while (true) {
            System.out.print(label + " (true/false | yes/no | y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "true", "t", "yes", "y", "1" -> {
                    return true;
                }
                case "false", "f", "no", "n", "0" -> {
                    return false;
                }
                default -> System.out.println("❌ Invalid input. Please enter yes/no or true/false.");
            }
        }
    }

    private static ZoneType readZoneType() {
        System.out.print("Zone type (STANDARD/EV/VIP): ");
        return ZoneType.valueOf(scanner.nextLine().toUpperCase());
    }

}