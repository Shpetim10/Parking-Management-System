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
    private static SpotAssignmentResponseDto lastAssignedSpot;
    private static String activeSessionId;


    public static void main(String[] args) {


        // ============================================================
        // REPOSITORIES (FR-1 to FR-14)
        // ============================================================
        UserRepository userRepo = new InMemoryUserRepository();
        VehicleRepository vehicleRepo = new InMemoryVehicleRepository();
        InMemoryParkingSessionRepository sessionRepo = new InMemoryParkingSessionRepository();
        ParkingZoneRepository zoneRepo = new InMemoryParkingZoneRepository();
        PenaltyHistoryRepository penaltyRepo = new InMemoryPenaltyHistoryRepository();
        BillingRecordRepository billingRepo = new InMemoryBillingRecordRepository();
        InMemorySubscriptionPlanRepository subscriptionRepo = new InMemorySubscriptionPlanRepository();


        DiscountPolicyRepository discountRepo = new InMemoryDiscountPolicyRepository(
                new DiscountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0)
        );


        TariffRepository tariffRepo = new InMemoryTariffRepository(Map.of(
                ZoneType.STANDARD, new Tariff(ZoneType.STANDARD, BigDecimal.valueOf(3), BigDecimal.valueOf(25), BigDecimal.valueOf(0.10)),
                ZoneType.EV, new Tariff(ZoneType.EV, BigDecimal.valueOf(2.5), BigDecimal.valueOf(20), BigDecimal.valueOf(0.05)),
                ZoneType.VIP, new Tariff(ZoneType.VIP, BigDecimal.valueOf(5), BigDecimal.valueOf(50), BigDecimal.valueOf(0.15))
        ));


        DynamicPricingConfigRepository pricingRepo = new InMemoryDynamicPricingConfigRepository(
                new DynamicPricingConfig(1.5, 1.0, 0.7)
        );


        // ============================================================
        // SERVICES
        // ============================================================
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
        var userServiceImpl = new UserServiceImpl(userRepo, subscriptionRepo);
        var vehicleService = new VehicleServiceImpl(vehicleRepo, userRepo);

        ZoneOccupancyService occupancyService = new ZoneOccupancyServiceImpl(zoneRepo, sessionRepo);


        // ============================================================
        // CONTROLLERS
        // ============================================================
        var eligibilityController = new EligibilityController(eligibilityService, userRepo, vehicleRepo, subscriptionRepo);
        var zoneController = new ZoneAllocationController(zoneAllocationService, zoneRepo, occupancyService, subscriptionRepo);
        var sessionController = new ParkingSessionController(sessionRepo, zoneRepo);
        var billingController = new BillingController(billingService, tariffRepo, pricingRepo, billingRepo, sessionRepo, penaltyRepo, subscriptionRepo);
        var penaltyController = new PenaltyController(penaltyService, monitoringService, penaltyRepo);
        var exitController = new ExitAuthorizationController(exitService, userRepo, sessionRepo, zoneRepo);
        var monitoringController = new MonitoringController(monitoringService, penaltyRepo, zoneRepo);
        var discountController = new DiscountInfoController(discountRepo,subscriptionRepo);
        var userController= new UserController(userServiceImpl);
        var vehicleController= new VehicleController(vehicleService);

        // ============================================================
        // SEED DATA
        // ============================================================
        seedZones(zoneRepo);
        seedUsersAndVehicles(userRepo, vehicleRepo, subscriptionRepo);


        // ============================================================
        // MAIN MENU LOOP
        // ============================================================
        while (true) {
            printMenu();
            int choice = readInt("Choose option");

            switch (choice) {

                // FR-1: User & Account Management
                case 1 -> createUser(userController);
                case 2 -> createVehicle(vehicleController);
                case 3 -> updateUserStatus(userController);
                case 4 -> updateAccountStanding(userRepo, penaltyRepo);

                // FR-7: Check eligibility
                case 5 -> checkEligibility(eligibilityController, sessionRepo);

                // FR-5: Zones & Spots
                case 6 -> createParkingZone(parkingZoneController);
                case 7 -> addParkingSpot(parkingZoneController);
                case 8 -> updateSpotState(zoneRepo);

                // FR-3, FR-6, FR-8: Session lifecycle
                case 9 -> assignParkingSpot(zoneController);
                case 10 -> startParkingSession(sessionController);
                case 18 -> viewSessionDetails(sessionRepo);

                // FR-9, FR-10: Billing & Discounts
                case 11 -> calculateBilling(billingController, sessionRepo, occupancyService);
                case 12 -> applyDiscount(discountController);
                case 21 -> viewBillingRecords(billingRepo);

                // FR-11, FR-2: Penalties & Blacklist
                case 13 -> applyPenalty(penaltyController, userController);
                case 16 -> viewBlacklistStatus(penaltyRepo, userRepo);
                case 22 -> viewPenaltyHistory(penaltyRepo);

                // FR-13: Monitoring & Reports
                case 14 -> viewPenaltySummary(monitoringController);
                case 15 -> viewZoneOccupancy(occupancyService);

                // FR-14: Exit authorization
                case 17 -> exitParking(exitController);

                // Subscriptions & Configuration
                case 19 -> updateTariff(tariffRepo);
                case 20 -> updateDynamicPricing(pricingRepo);
                case 23 -> registerSubscription(subscriptionRepo);

                // Exit system
                case 0 -> {
                    System.out.println("👋 Goodbye!");
                    return;
                }

                default -> System.out.println("❌ Invalid option");
            }
            System.out.println();
        }
    }


    // ============================================================
    // FR-1: USER & ACCOUNT MANAGEMENT
    // ============================================================
    private static void createUser(UserController userController) {
        try{
            String userId = read("User ID");

            userController.createrUser(userId);

            System.out.println("User created successfully!");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

    }

    private static void createVehicle(VehicleController vehicleController) {
        try{
            String userId = read("User ID");
            String plate = read("Vehicle plate");

            vehicleController.createVehicle(userId, plate);

            System.out.println("Vehicle created successfully and assigned to user!");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private static void updateUserStatus(UserController userController) {
        String userId = read("User ID");
        String statusStr = read("Status (ACTIVE/INACTIVE/BLACKLISTED)").toUpperCase();


        try {
            userController.updateUser(userId, statusStr);

            System.out.println("✅ User status updated to " + statusStr);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void updateAccountStanding(
            UserRepository userRepo,
            PenaltyHistoryRepository penaltyRepo
    ) {
        String userId = read("User ID");


        try {
            User user = userRepo.findById(userId).orElseThrow();
            PenaltyHistory history = penaltyRepo.findById(userId);

            int penaltyCount = history.getPenaltyCount();


            String derivedStanding =
                    penaltyCount == 0 ? "GOOD_STANDING"
                            : penaltyCount < 3 ? "WARNING"
                            : "SUSPENDED";


            System.out.println("ℹ️ ACCOUNT STANDING (Derived)");
            System.out.println("   User ID: " + user.getId());
            System.out.println("   User status: " + user.getStatus());
            System.out.println("   Penalties count: " + penaltyCount);
            System.out.println("   Derived standing: " + derivedStanding);
            System.out.println("⚠️ Account standing is managed automatically by penalties.");


        } catch (Exception e) {
            System.out.println("❌ User or penalty history not found");
        }
    }


    // ============================================================
    // FR-7: ELIGIBILITY TO START SESSION
    // ============================================================
    private static void checkEligibility(EligibilityController eligibilityController, InMemoryParkingSessionRepository sessionRepo) {
        String userId = read("User ID");
        String plate = read("Vehicle plate");


        try {
            EligibilityRequestDto dto = new EligibilityRequestDto(
                    userId, plate,
                    sessionRepo.getActiveSessionsCountForVehicle(plate),
                    sessionRepo.getActiveSessionsCountForUser(userId),
                    sessionRepo.getSessionsCountForToday(userId),
                    sessionRepo.getHoursUsedTodayForUser(userId),
                    sessionRepo.hasUnpaidSessionsForUser(userId),
                    LocalDateTime.now()
            );


            EligibilityResponseDto res = eligibilityController.checkEligibility(dto);
            System.out.println("✅ Eligibility: " + (res.allowed() ? "ALLOWED" : "DENIED"));
            if (!res.allowed()) {
                System.out.println("   Reason: " + res.reason());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            
        }
    }


    // ============================================================
    // FR-5: ZONES & SPOTS
    // ============================================================
    private static void createParkingZone(ParkingZoneController parkingZoneController) {
        String zoneId = read("Zone ID");
        String zoneType = read("Type (STANDARD/EV/VIP)").toUpperCase();
        double threshold = readDouble("Occupancy threshold [0,1]");


        try {
            parkingZoneController.createParkingZone(new ParkingZoneDto(zoneId, zoneType, threshold));
            System.out.println("✅ Zone '" + zoneId + "' created");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }


    private static void addParkingSpot(ParkingZoneController parkingZoneController) {
        String zoneId = read("Zone ID");
        String spotId = read("Spot ID");


        try {
            parkingZoneController.addSpot(new ParkingSpotDto(spotId, zoneId));
            System.out.println("✅ Spot '" + spotId + "' added to zone '" + zoneId + "'");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            
        }
    }


    // remove
    private static void updateSpotState(ParkingZoneRepository zoneRepo) {
        String zoneId = read("Zone ID");
        String spotId = read("Spot ID");
        String stateStr = read("State (FREE/RESERVED/OCCUPIED)").toUpperCase();


        try {
            SpotState state = SpotState.valueOf(stateStr);
            ParkingZone zone = zoneRepo.findById(zoneId);

            ParkingSpot spot = zone.getSpots().stream()
                    .filter(s -> s.getSpotId().equals(spotId))
                    .findFirst()
                    .orElseThrow();


            spot.setState(state);
            zoneRepo.save(zone);

            System.out.println("✅ Spot state updated to " + state);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }


    // ============================================================
    // FR-3 & FR-8: ASSIGN SPOT & START SESSION
    // ============================================================
    private static void assignParkingSpot(ZoneAllocationController zoneController) {
        String userId = read("User ID");
        String zoneTypeInput = read("Zone type (STANDARD/EV/VIP)").toUpperCase();


        try {
            ZoneType requestedZoneType = ZoneType.valueOf(zoneTypeInput);
            lastAssignedSpot = zoneController.assignSpot(
                    new SpotAssignmentRequestDto(userId, requestedZoneType, LocalDateTime.now())
            );


            if (lastAssignedSpot == null) {
                System.out.println("❌ No spot available in " + requestedZoneType);
            } else {
                System.out.println("🅿️ Spot assigned: " + lastAssignedSpot.spotId() + " in zone " + lastAssignedSpot.zoneId());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            
        }
    }


    private static void startParkingSession(ParkingSessionController sessionController) {
        if (lastAssignedSpot == null) {
            System.out.println("❌ Assign a spot first (option 8)");
            return;
        }


        String userId = read("User ID");
        String plate = read("Vehicle plate");
        boolean isHoliday = readBoolean("Is holiday?");


        try {
            StartSessionResponseDto res = sessionController.startSession(
                    new StartSessionRequestDto(
                            userId, plate,
                            lastAssignedSpot.zoneId(),
                            lastAssignedSpot.spotId(),
                            lastAssignedSpot.zoneType(),
                            isHoliday,
                            LocalDateTime.now()
                    )
            );


            activeSessionId = res.sessionId();
            System.out.println("🚗 Session started: " + activeSessionId);
            System.out.println("   Entry time: " + LocalDateTime.now());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }


    // ============================================================
    // FR-9 & FR-10: BILLING CALCULATION
    // ============================================================
    private static void calculateBilling(BillingController billingController, InMemoryParkingSessionRepository sessionRepo, ZoneOccupancyService occupancyService) {
       String sessionId = read("Session ID");
        try {

            ParkingSession session = sessionRepo.findById(sessionId).orElseThrow();
            double occupancy = occupancyService.calculateOccupancyRatioForZone(session.getZoneId());


            BillingResponse bill = billingController.calculateBill(
                    new BillingRequest(
                            sessionId,
                            session.getZoneType(),
                            session.getDayType(),
                            session.getTimeOfDayBand(),
                            occupancy,
                            LocalDateTime.now(),
                            BigDecimal.ZERO,
                            24
                    )
            );


            System.out.println("💰 BILLING DETAILS");
            System.out.println("   Base price: $" + bill.basePrice());
            System.out.println("   Discounts: -$" + bill.discountsTotal());
            System.out.println("   Penalties: +$" + bill.penaltiesTotal());
            System.out.println("   Net price: $" + bill.netPrice());
            System.out.println("   Tax: +$" + bill.taxAmount());
            System.out.println("   FINAL PRICE: $" + bill.finalPrice());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }


    // ============================================================
    // FR-10: DISCOUNTS
    // ============================================================
    private static void applyDiscount(DiscountInfoController discountController) {
        String userId = read("User ID");
        BigDecimal subscriptionPercent = readBigDecimal("Subscription discount %");
        BigDecimal promoPercent = readBigDecimal("Promo discount %");
        BigDecimal promoFixed = readBigDecimal("Promo fixed amount");
        try{
            discountController.saveDiscountForUser(userId,
                    new DiscountInfoDto(subscriptionPercent, promoPercent, promoFixed, false, 0)
            );

            System.out.println("✅ Discount saved for user " + userId);
        }catch(Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }


    // ============================================================
    // FR-11 & FR-2: PENALTIES & BLACKLIST
    // ============================================================
    private static void applyPenalty(PenaltyController penaltyController, UserController userController) {
        String userId = read("User ID");
        String penaltyTypeStr = read("Penalty type (OVERSTAY/LOST_TICKET/MISUSE)").toUpperCase();
        BigDecimal amount = readBigDecimal("Penalty amount");


        try {
            PenaltyType type = PenaltyType.valueOf(penaltyTypeStr);

            ApplyPenaltyResponseDto res =
                    penaltyController.applyPenalty(
                            new ApplyPenaltyRequestDto(
                                    userId,
                                    type,
                                    amount,
                                    LocalDateTime.now()
                            )
                    );


            System.out.println("⚠️ Penalty applied");
            System.out.println("   Blacklist status: " + res.blacklistStatus());

            if (res.blacklistStatus() == BlacklistStatus.BLACKLISTED) {
                userController.updateUser(userId, "BLACKLISTED");
            }

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }


    private static void viewBlacklistStatus(
            PenaltyHistoryRepository penaltyRepo,
            UserRepository userRepo
    ) {
        String userId = read("User ID");


        try {
            User user = userRepo.findById(userId).orElseThrow();
            PenaltyHistory history = penaltyRepo.findById(userId);


            boolean blacklisted = history.getPenaltyCount() >= 3;


            System.out.println("📋 USER STATUS");
            System.out.println("   User ID: " + user.getId());
            System.out.println("   User status: " + user.getStatus());
            System.out.println("   Penalties count: " + history.getPenaltyCount());
            System.out.println("   Total penalties: $" + history.getTotalPenaltyAmount());
            System.out.println("   Blacklisted: " + (blacklisted ? "YES ❌" : "NO ✅"));


        } catch (Exception e) {
            System.out.println("❌ User or penalty history not found");
        }
    }


    // ============================================================
    // FR-13: MONITORING & REPORTS
    // ============================================================
    private static void viewPenaltySummary(MonitoringController monitoringController) {
        try {
            PenaltySummaryResponseDto summary =
                    monitoringController.generatePenaltySummary();


            BigDecimal total = summary.totalOverstay()
                    .add(summary.totalLostTicket())
                    .add(summary.totalMisuse());


            System.out.println("📊 PENALTY SUMMARY");
            System.out.println("   Overstay: $" + summary.totalOverstay());
            System.out.println("   Lost ticket: $" + summary.totalLostTicket());
            System.out.println("   Misuse: $" + summary.totalMisuse());
            System.out.println("   TOTAL: $" + total);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }


    private static void viewZoneOccupancy(ZoneOccupancyService zoneOccupancyService) {
        String zoneId = read("Zone ID");


        try {

            double ratio= zoneOccupancyService.calculateOccupancyRatioForZone(zoneId);

            System.out.println("📍 ZONE OCCUPANCY");
            System.out.println("   Zone ID: " + zoneId);
            System.out.printf("   Occupancy ratio: %.1f%%%n", ratio * 100);
        } catch (Exception e) {
            System.out.println("❌ Zone not found");
        }
    }


    // ============================================================
    // FR-14: EXIT AUTHORIZATION
    // ============================================================
    private static void exitParking(ExitAuthorizationController exitController) {
        if (activeSessionId == null) {
            System.out.println("❌ No active session");
            return;
        }


        String userId = read("User ID");
        String plateAtGate = read("Plate at gate");


        try {
            ExitAuthorizationResponseDto exit =
                    exitController.authorizeExit(
                            new ExitAuthorizationRequestDto(
                                    userId,
                                    activeSessionId,
                                    plateAtGate
                            )
                    );


            System.out.println("🚦 EXIT AUTHORIZATION");
            System.out.println("   Allowed: " + exit.allowed());
            System.out.println("   Reason: " + exit.reason());


            if (exit.allowed()) {
                activeSessionId = null;
                lastAssignedSpot = null;
                System.out.println("✅ Session closed");
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }


    // ============================================================
    // FR-6: SESSION MANAGEMENT
    // ============================================================
    private static void viewSessionDetails(InMemoryParkingSessionRepository sessionRepo) {
        String sessionId = read("Session ID");


        try {
            ParkingSession session = sessionRepo.findById(sessionId).orElseThrow();


            System.out.println("🚗 SESSION DETAILS");
            System.out.println("   ID: " + session.getId());
            System.out.println("   User: " + session.getUserId());
            System.out.println("   Vehicle: " + session.getVehiclePlate());
            System.out.println("   Zone: " + session.getZoneId());
            System.out.println("   Spot: " + session.getSpotId());
            System.out.println("   Start: " + session.getStartTime());
            System.out.println("   End: " + session.getEndTime());
            System.out.println("   State: " + session.getState());
        } catch (Exception e) {
            System.out.println("❌ Session not found");
        }
    }


    // ============================================================
    // FR-4: TARIFF & PRICING CONFIGURATION
    // ============================================================
    private static void updateTariff(TariffRepository tariffRepo) {
        String zoneTypeStr = read("Zone type (STANDARD/EV/VIP)").toUpperCase();
        BigDecimal baseRate = readBigDecimal("Base hourly rate");
        BigDecimal dailyCap = readBigDecimal("Daily cap");
        BigDecimal surcharge = readBigDecimal("Weekend surcharge %");


        try {
            ZoneType zoneType = ZoneType.valueOf(zoneTypeStr);
            Tariff tariff = new Tariff(zoneType, baseRate, dailyCap, surcharge);
            tariffRepo.save(tariff);
            System.out.println("✅ Tariff updated for " + zoneType);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }


    private static void updateDynamicPricing(DynamicPricingConfigRepository pricingRepo) {
        double peakMultiplier = readDouble("Peak hour multiplier");
        double offPeakMultiplier = readDouble("Off-peak multiplier");
        double highOccupancyThreshold = readDouble("High occupancy threshold [0-1]");


        try {
            DynamicPricingConfig config = new DynamicPricingConfig(peakMultiplier, offPeakMultiplier, highOccupancyThreshold);
            pricingRepo.save(config);
            System.out.println("✅ Dynamic pricing updated");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }


    // ============================================================
    // FR-12: BILLING RECORDS
    // ============================================================
    private static void viewBillingRecords(BillingRecordRepository billingRepo) {
        String sessionId = read("Session ID");


        try {
            BillingRecord record =
                    billingRepo.findBySessionId(sessionId).orElseThrow();


            BillingResult result = record.getBillingResult();


            System.out.println("💳 BILLING RECORD");
            System.out.println("   Session: " + record.getSessionId());
            System.out.println("   User: " + record.getUserId());
            System.out.println("   Base price: $" + result.getBasePrice());
            System.out.println("   Discounts: $" + result.getDiscountsTotal());
            System.out.println("   Penalties: $" + result.getPenaltiesTotal());
            System.out.println("   Tax: $" + result.getTaxAmount());
            System.out.println("   Final price: $" + result.getFinalPrice());


        } catch (Exception e) {
            System.out.println("❌ Billing record not found");
        }
    }


    private static void viewPenaltyHistory(PenaltyHistoryRepository penaltyRepo) {
        String userId = read("User ID");


        try {
            PenaltyHistory history = penaltyRepo.findById(userId);


            System.out.println("⚠️ PENALTY HISTORY");
            System.out.println("   Count: " + history.getPenaltyCount());
            System.out.println("   Total: $" + history.getTotalPenaltyAmount());


            for (Penalty p : history.getPenalties()) {
                System.out.println("   - " + p.getType()
                        + " | $" + p.getAmount()
                        + " | " + p.getTimestamp());
            }


        } catch (Exception e) {
            System.out.println("❌ No penalty history found");
        }
    }


    private static void registerSubscription(InMemorySubscriptionPlanRepository subscriptionRepo) {
        String userId = read("User ID");
        int choice = readInt("Subscription choice (1-Standard/2- Ev/3- Vip)");

        switch (choice) {
            case 1:
                subscriptionRepo.save(userId,SubscriptionPlan.defaultPlan());
                System.out.println("✅ Subscription registered for " + userId);
                break;

            case 2:
                subscriptionRepo.save(userId,SubscriptionPlan.evZonePlan());
                System.out.println("✅ Subscription registered for " + userId);
                break;

            case 3:
                subscriptionRepo.save(userId,SubscriptionPlan.vipZonePlan());
                System.out.println("✅ Subscription registered for " + userId);
                break;

            default:
                System.out.println("This plan does not exist! ");
                break;
        }
    }






    // ============================================================
    // HELPERS
    // ============================================================
    private static void seedZones(ParkingZoneRepository zoneRepo) {
        ParkingZone standardZone = new ParkingZone("Z-STANDARD", ZoneType.STANDARD, 0.9);
        standardZone.addSpot(new ParkingSpot("S-1", standardZone));
        standardZone.addSpot(new ParkingSpot("S-2", standardZone));


        ParkingZone evZone = new ParkingZone("Z-EV", ZoneType.EV, 0.8);
        evZone.addSpot(new ParkingSpot("EV-1", evZone));
        evZone.addSpot(new ParkingSpot("EV-2", evZone));


        ParkingZone vipZone = new ParkingZone("Z-VIP", ZoneType.VIP, 0.7);
        vipZone.addSpot(new ParkingSpot("VIP-1", vipZone));


        zoneRepo.save(standardZone);
        zoneRepo.save(evZone);
        zoneRepo.save(vipZone);


        System.out.println("✅ Zones & spots seeded");
    }


    private static void seedUsersAndVehicles(UserRepository userRepo, VehicleRepository vehicleRepo, InMemorySubscriptionPlanRepository subscriptionRepo) {
        userRepo.save(new User("U1", UserStatus.ACTIVE));
        vehicleRepo.save(new Vehicle("AA-111", "U1"));
        subscriptionRepo.save("U1", SubscriptionPlan.defaultPlan());


        userRepo.save(new User("U2", UserStatus.ACTIVE));
        vehicleRepo.save(new Vehicle("EV-222", "U2"));
        subscriptionRepo.save("U2", SubscriptionPlan.defaultPlan());


        userRepo.save(new User("U3", UserStatus.ACTIVE));
        vehicleRepo.save(new Vehicle("VIP-333", "U3"));
        subscriptionRepo.save("U3", SubscriptionPlan.defaultPlan());


        System.out.println("✅ Users, vehicles & subscriptions seeded");
    }


    private static void printMenu() {
        System.out.println("""
           ═══════════════════════════════════════════════════
                PARKING MANAGEMENT SYSTEM - FULL MENU
           ═══════════════════════════════════════════════════
           FR-1: USER & ACCOUNT MANAGEMENT
             1. Register user
             2. Register vehicle
             3. Update user status
             4. Update account standing

           FR-7: ELIGIBILITY
             5. Check eligibility

           FR-5: ZONES & SPOTS
             6. Create parking zone
             7. Add parking spot
             8. Update spot state

           FR-3, FR-6, FR-8: SESSION LIFECYCLE
             9. Assign parking spot
            10. Start parking session
            18. View session details

           FR-9, FR-10: BILLING & DISCOUNTS
            11. Calculate billing
            12. Apply discount
            21. View billing records

           FR-11, FR-2: PENALTIES & BLACKLIST
            13. Apply penalty
            16. View blacklist status
            22. View penalty history

           FR-13: MONITORING & REPORTS
            14. View penalty summary
            15. View zone occupancy

           FR-14: EXIT AUTHORIZATION
            17. Exit parking

           SUBSCRIPTIONS & CONFIGURATION
            19. Update tariff
            20. Update dynamic pricing
            23. Register subscription

            0. Exit system
           ═══════════════════════════════════════════════════
           """);
    }


    // ============================================================
// INPUT HELPERS
// ============================================================
    private static String read(String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
    }


    private static int readInt(String label) {
        System.out.print(label + ": ");
        return Integer.parseInt(scanner.nextLine().trim());
    }


    private static double readDouble(String label) {
        System.out.print(label + ": ");
        return Double.parseDouble(scanner.nextLine().trim());
    }


    private static BigDecimal readBigDecimal(String label) {
        System.out.print(label + ": ");
        return new BigDecimal(scanner.nextLine().trim());
    }


    private static boolean readBoolean(String label) {
        System.out.print(label + " (true/false | yes/no): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("true") || input.equals("yes") || input.equals("y");
    }
}