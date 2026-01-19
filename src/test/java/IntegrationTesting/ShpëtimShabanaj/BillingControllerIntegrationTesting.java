package IntegrationTesting.ShpëtimShabanaj;

import Controller.BillingController;
import Dto.Billing.BillingRequest;
import Dto.Billing.BillingResponse;
import Enum.DayType;
import Enum.SessionState;
import Enum.TimeOfDayBand;
import Enum.ZoneType;
import Enum.PenaltyType;
import Model.*;
import Record.DurationInfo;
import Repository.*;
import Repository.impl.*;
import Service.BillingService;
import Settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Integration Tests - BillingController (Neighbourhood Radius 1)")
class BillingControllerIntegrationTesting {

    private BillingController billingController;

    // STUB: BillingService (direct dependency)
    @Mock
    private BillingService billingService;

    // REAL: All repositories (direct dependencies)
    private TariffRepository tariffRepository;
    private DynamicPricingConfigRepository dynamicPricingConfigRepository;
    private DiscountPolicyRepository discountPolicyRepository;
    private BillingRecordRepository billingRecordRepository;
    private ParkingSessionRepository parkingSessionRepository;
    private PenaltyHistoryRepository penaltyHistoryRepository;
    private SubscriptionPlanRepository subscriptionPlanRepository;

    private Tariff standardTariff;
    private Tariff evTariff;
    private Tariff vipTariff;
    private DynamicPricingConfig dynamicConfig;
    private ParkingSession testSession;
    private SubscriptionPlan testPlan;
    private DiscountInfo defaultDiscount;

    @BeforeEach
    void setUp() {
        // Setup tariffs using correct constructor
        standardTariff = new Tariff(
                ZoneType.STANDARD,
                BigDecimal.valueOf(5.00),
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(20.00)
        );

        evTariff = new Tariff(
                ZoneType.EV,
                BigDecimal.valueOf(3.00),
                BigDecimal.valueOf(30.00),
                BigDecimal.valueOf(15.00)
        );

        vipTariff = new Tariff(
                ZoneType.VIP,
                BigDecimal.valueOf(10.00),
                BigDecimal.valueOf(80.00),
                BigDecimal.valueOf(25.00)
        );

        Map<ZoneType, Tariff> tariffMap = new EnumMap<>(ZoneType.class);
        tariffMap.put(ZoneType.STANDARD, standardTariff);
        tariffMap.put(ZoneType.EV, evTariff);
        tariffMap.put(ZoneType.VIP, vipTariff);

        tariffRepository = new InMemoryTariffRepository(tariffMap);

        // Setup dynamic pricing config using correct constructor
        dynamicConfig = new DynamicPricingConfig(1.5, 0.8, 1.3);
        dynamicPricingConfigRepository = new InMemoryDynamicPricingConfigRepository(dynamicConfig);

        // Setup default discount info
        defaultDiscount = new DiscountInfo(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        // Setup repositories (real in-memory implementations)
        discountPolicyRepository = new InMemoryDiscountPolicyRepository(defaultDiscount);
        billingRecordRepository = new InMemoryBillingRecordRepository();
        parkingSessionRepository = new InMemoryParkingSessionRepository();
        penaltyHistoryRepository = new InMemoryPenaltyHistoryRepository();
        subscriptionPlanRepository = new InMemorySubscriptionPlanRepository();

        // Create controller with stubbed service and real repositories
        billingController = new BillingController(
                billingService,
                tariffRepository,
                dynamicPricingConfigRepository,
                discountPolicyRepository,
                billingRecordRepository,
                parkingSessionRepository,
                penaltyHistoryRepository,
                subscriptionPlanRepository
        );

        // Setup test data
        setupTestSession();
        setupTestSubscriptionPlan();
    }

    private void setupTestSession() {
        testSession = new ParkingSession(
                "session-001",
                "user-001",
                "ABC-123",
                "zone-001",
                "spot-001",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 15, 9, 0)
        );
        parkingSessionRepository.save(testSession);
    }

    private void setupTestSubscriptionPlan() {
        DiscountInfo discountInfo = new DiscountInfo(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        testPlan = new SubscriptionPlan(
                2,      // maxConcurrentSessions
                1,      // maxConcurrentSessionsPerVehicle
                5,      // maxDailySessions
                8.0,    // maxDailyHours
                false,  // weekdayOnly
                false,  // hasEvRights
                false,  // hasVipRights
                discountInfo
        );

        subscriptionPlanRepository.save("user-001", testPlan);
    }

    // IT-01: Happy path - complete billing flow
    @Test
    @DisplayName("IT-01: Should orchestrate complete billing flow with all components")
    void testCalculateBill_StandardSession_CompleteFlow() {
        // Arrange
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 13, 0); // 4 hours
        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("20.00"), // basePrice
                BigDecimal.ZERO,         // discountsTotal
                BigDecimal.ZERO,         // penalties
                new BigDecimal("20.00"), // netPrice
                new BigDecimal("4.00"),  // taxAmount
                new BigDecimal("24.00")  // finalPrice
        );

        when(billingService.calculateBill(
                eq(testSession.getStartTime()),
                eq(exitTime),
                eq(ZoneType.STANDARD),
                eq(DayType.WEEKDAY),
                eq(TimeOfDayBand.OFF_PEAK),
                eq(0.5),
                eq(standardTariff),
                eq(dynamicConfig),
                eq(testPlan.discountInfo),
                eq(BigDecimal.ZERO),
                eq(24),
                any(BigDecimal.class),
                any(BigDecimal.class)
        )).thenReturn(mockResult);

        // Act
        BillingResponse response = billingController.calculateBill(request);

        // Assert
        assertAll("Verify complete billing orchestration",
                () -> assertEquals("session-001", response.sessionId()),
                () -> assertEquals("user-001", response.userId()),
                () -> assertEquals(new BigDecimal("20.00"), response.basePrice()),
                () -> assertEquals(BigDecimal.ZERO, response.discountsTotal()),
                () -> assertEquals(BigDecimal.ZERO.setScale(2), response.penaltiesTotal()),
                () -> assertEquals(new BigDecimal("20.00"), response.netPrice()),
                () -> assertEquals(new BigDecimal("4.00"), response.taxAmount()),
                () -> assertEquals(new BigDecimal("24.00"), response.finalPrice())
        );

        // Verify service called with correct parameters
        verify(billingService, times(1)).calculateBill(
                eq(testSession.getStartTime()),
                eq(exitTime),
                eq(ZoneType.STANDARD),
                eq(DayType.WEEKDAY),
                eq(TimeOfDayBand.OFF_PEAK),
                eq(0.5),
                eq(standardTariff),
                eq(dynamicConfig),
                eq(testPlan.discountInfo),
                eq(BigDecimal.ZERO),
                eq(24),
                any(BigDecimal.class),
                any(BigDecimal.class)
        );

        // Verify billing record was saved
        Optional<BillingRecord> savedRecord = billingRecordRepository.findBySessionId("session-001");
        assertTrue(savedRecord.isPresent());
        assertEquals("user-001", savedRecord.get().getUserId());

        // Verify session was marked as paid
        ParkingSession session = parkingSessionRepository.findById("session-001").orElseThrow();
        assertTrue(session.getState() == SessionState.PAID);
    }

    // IT-02: Correct tariff retrieval from repository
    @Test
    @DisplayName("IT-02: Should retrieve correct tariff from repository based on zone type")
    void testCalculateBill_TariffRetrieval_CorrectTariffUsed() {
        // Arrange
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 12, 0);
        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("15.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("15.00"),
                new BigDecimal("3.00"),
                new BigDecimal("18.00")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(Tariff.class), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Act
        billingController.calculateBill(request);

        // Assert - Verify correct tariff was passed to service
        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyDouble(),
                eq(standardTariff), // Verify STANDARD tariff used
                any(),
                any(),
                any(),
                anyInt(),
                any(),
                any()
        );
    }

    // IT-03: Different zone type uses different tariff
    @Test
    @DisplayName("IT-03: Should use EV tariff for EV zone")
    void testCalculateBill_EVZone_EVTariffUsed() {
        // Arrange
        ParkingSession evSession = new ParkingSession(
                "session-ev",
                "user-001",
                "EV-456",
                "zone-ev",
                "spot-ev",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.EV,
                LocalDateTime.of(2026, 1, 15, 10, 0)
        );
        parkingSessionRepository.save(evSession);

        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 14, 0);
        BillingRequest request = new BillingRequest(
                "session-ev",
                ZoneType.EV,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("12.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("12.00"),
                new BigDecimal("2.40"),
                new BigDecimal("14.40")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(Tariff.class), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Act
        billingController.calculateBill(request);

        // Assert - Verify EV tariff was used
        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyDouble(),
                eq(evTariff), // Verify EV tariff used
                any(),
                any(),
                any(),
                anyInt(),
                any(),
                any()
        );
    }

    // IT-04: Dynamic pricing config retrieved correctly
    @Test
    @DisplayName("IT-04: Should retrieve active dynamic pricing config from repository")
    void testCalculateBill_DynamicConfig_ActiveConfigUsed() {
        // Arrange
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 11, 0);
        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.PEAK,
                0.85,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("19.50"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("19.50"),
                new BigDecimal("3.90"),
                new BigDecimal("23.40")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(DynamicPricingConfig.class), any(), any(), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Act
        billingController.calculateBill(request);

        // Assert - Verify correct dynamic config was used
        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyDouble(),
                any(),
                eq(dynamicConfig), // Verify active config used
                any(),
                any(),
                anyInt(),
                any(),
                any()
        );
    }

    // IT-05: Penalty history integration
    @Test
    @DisplayName("IT-05: Should retrieve and include penalties from penalty history")
    void testCalculateBill_WithPenalties_PenaltiesRetrievedFromRepository() {
        // Arrange
        PenaltyHistory penaltyHistory = penaltyHistoryRepository.getOrCreate("user-001");
        penaltyHistory.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                BigDecimal.valueOf(15.00),
                LocalDateTime.now()
        ));
        penaltyHistoryRepository.save("user-001", penaltyHistory);

        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 11, 0);
        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("10.00"),
                BigDecimal.ZERO,
                new BigDecimal("15.00"),
                new BigDecimal("25.00"),
                new BigDecimal("5.00"),
                new BigDecimal("30.00")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(BigDecimal.class), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Act
        BillingResponse response = billingController.calculateBill(request);

        // Assert
        assertEquals(new BigDecimal("15.00"), response.penaltiesTotal());

        // Verify penalties from history passed to service
        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyDouble(),
                any(),
                any(),
                any(),
                eq(BigDecimal.valueOf(15.00)), // Verify penalties from history
                anyInt(),
                any(),
                any()
        );
    }

    // IT-06: Penalties from request should be ignored (history takes precedence)
    @Test
    @DisplayName("IT-06: Should use penalties from history, not from request")
    void testCalculateBill_PenaltiesInRequest_HistoryTakesPrecedence() {
        // Arrange
        PenaltyHistory penaltyHistory = penaltyHistoryRepository.getOrCreate("user-001");
        penaltyHistory.addPenalty(new Penalty(
                PenaltyType.LOST_TICKET,
                BigDecimal.valueOf(20.00),
                LocalDateTime.now()
        ));
        penaltyHistoryRepository.save("user-001", penaltyHistory);

        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 12, 0);
        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.valueOf(5.00), // Request has penalties but should be ignored
                24
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("15.00"),
                BigDecimal.ZERO,
                new BigDecimal("20.00"),
                new BigDecimal("35.00"),
                new BigDecimal("7.00"),
                new BigDecimal("42.00")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(BigDecimal.class), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Act
        billingController.calculateBill(request);

        // Assert - Verify penalties from history (20.00) used, not from request (5.00)
        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyDouble(),
                any(),
                any(),
                any(),
                eq(BigDecimal.valueOf(20.00)), // History value, not request value
                anyInt(),
                any(),
                any()
        );
    }

    // IT-07: No penalty history for user
    @Test
    @DisplayName("IT-07: Should handle user with no penalty history (zero penalties)")
    void testCalculateBill_NoPenaltyHistory_ZeroPenaltiesUsed() {
        // Arrange - Don't create penalty history
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 12, 0);
        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.valueOf(10.00), // Request has penalties but no history
                24
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("15.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("15.00"),
                new BigDecimal("3.00"),
                new BigDecimal("18.00")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(BigDecimal.class), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Act
        billingController.calculateBill(request);

        // Assert - Verify zero penalties passed (no history)
        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyDouble(),
                any(),
                any(),
                any(),
                eq(BigDecimal.ZERO), // Zero because no history
                anyInt(),
                any(),
                any()
        );
    }

    // IT-08: Subscription plan integration
    @Test
    @DisplayName("IT-08: Should retrieve and use subscription plan discount info")
    void testCalculateBill_SubscriptionPlan_DiscountInfoUsed() {
        // Arrange
        DiscountInfo premiumDiscount = new DiscountInfo(
                BigDecimal.valueOf(0.20),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        SubscriptionPlan premiumPlan = new SubscriptionPlan(
                3,      // maxConcurrentSessions
                2,      // maxConcurrentSessionsPerVehicle
                10,     // maxDailySessions
                12.0,   // maxDailyHours
                false,  // weekdayOnly
                true,   // hasEvRights
                false,  // hasVipRights
                premiumDiscount
        );

        subscriptionPlanRepository.save("user-001", premiumPlan);

        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 14, 0);
        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("25.00"),
                new BigDecimal("5.00"),
                BigDecimal.ZERO,
                new BigDecimal("20.00"),
                new BigDecimal("4.00"),
                new BigDecimal("24.00")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(DiscountInfo.class), any(), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Act
        billingController.calculateBill(request);

        // Assert - Verify discount info from plan was used
        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyDouble(),
                any(),
                any(),
                eq(premiumDiscount), // Verify plan's discount info used
                any(),
                anyInt(),
                any(),
                any()
        );
    }

    // IT-09: Max duration from subscription plan
    @Test
    @DisplayName("IT-09: Should use subscription plan max duration when request has zero or negative")
    void testCalculateBill_PlanMaxDuration_UsedWhenRequestHasZero() {
        // Arrange
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 17, 0);
        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                0 // Zero max duration in request
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("40.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("40.00"),
                new BigDecimal("8.00"),
                new BigDecimal("48.00")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Act
        billingController.calculateBill(request);

        // Assert - Verify plan's max duration (8 hours) was used
        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyDouble(),
                any(),
                any(),
                any(),
                any(),
                eq(8), // Verify plan's max duration used (rounded from 8.0)
                any(),
                any()
        );
    }

    // IT-10: Request max duration takes precedence when provided
    @Test
    @DisplayName("IT-10: Should use request max duration when it's greater than zero")
    void testCalculateBill_RequestMaxDuration_UsedWhenProvided() {
        // Arrange
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 17, 0);
        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                12 // Explicit max duration in request
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("60.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("50.00"),
                new BigDecimal("10.00"),
                new BigDecimal("60.00")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Act
        billingController.calculateBill(request);

        // Assert - Verify request's max duration (12) was used, not plan's (8)
        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyDouble(),
                any(),
                any(),
                any(),
                any(),
                eq(12), // Request value takes precedence
                any(),
                any()
        );
    }

    // IT-11: Billing record persistence
    @Test
    @DisplayName("IT-11: Should save billing record with complete information")
    void testCalculateBill_BillingRecord_SavedCorrectly() {
        // Arrange
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 13, 0);
        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("20.00"),
                new BigDecimal("5.00"),
                new BigDecimal("10.00"),
                new BigDecimal("25.00"),
                new BigDecimal("5.00"),
                new BigDecimal("30.00")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Act
        billingController.calculateBill(request);

        // Assert - Verify billing record saved with correct data
        Optional<BillingRecord> savedRecord = billingRecordRepository.findBySessionId("session-001");
        assertTrue(savedRecord.isPresent());

        BillingRecord record = savedRecord.get();
        assertAll("Verify billing record contents",
                () -> assertEquals("session-001", record.getSessionId()),
                () -> assertEquals("user-001", record.getUserId()),
                () -> assertEquals(ZoneType.STANDARD, record.getZoneType()),
                () -> assertEquals(testSession.getStartTime(), record.getEntryTime()),
                () -> assertEquals(exitTime, record.getExitTime()),
                () -> assertNotNull(record.getBillingResult())
        );
    }

    // IT-12: Session marked as paid
    @Test
    @DisplayName("IT-12: Should mark parking session as paid after billing")
    void testCalculateBill_SessionState_MarkedAsPaid() {
        // Arrange
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 13, 0);
        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("20.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("20.00"),
                new BigDecimal("4.00"),
                new BigDecimal("24.00")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Verify session is not paid initially
        assertFalse(testSession.getState() == SessionState.PAID);

        // Act
        billingController.calculateBill(request);

        // Assert - Verify session marked as paid
        ParkingSession updatedSession = parkingSessionRepository.findById("session-001").orElseThrow();
        assertTrue(updatedSession.getState() == SessionState.PAID);
    }

    // IT-13: Null request validation
    @Test
    @DisplayName("IT-13: Should throw NullPointerException for null request")
    void testCalculateBill_NullRequest_ThrowsException() {
        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            billingController.calculateBill(null);
        });

        assertEquals("request must not be null", exception.getMessage());
        verifyNoInteractions(billingService);
    }

    // IT-14: Session not found
    @Test
    @DisplayName("IT-14: Should throw NoSuchElementException when session not found")
    void testCalculateBill_SessionNotFound_ThrowsException() {
        // Arrange
        BillingRequest request = new BillingRequest(
                "non-existent-session",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            billingController.calculateBill(request);
        });

        verifyNoInteractions(billingService);
    }

    // IT-15: Subscription plan not found
    @Test
    @DisplayName("IT-15: Should throw NoSuchElementException when subscription plan not found")
    void testCalculateBill_PlanNotFound_ThrowsException() {
        // Arrange
        ParkingSession sessionWithoutPlan = new ParkingSession(
                "session-no-plan",
                "user-no-plan",
                "ABC-999",
                "zone-001",
                "spot-001",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 15, 9, 0)
        );
        parkingSessionRepository.save(sessionWithoutPlan);

        BillingRequest request = new BillingRequest(
                "session-no-plan",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            billingController.calculateBill(request);
        });

        verifyNoInteractions(billingService);
    }

    // IT-16: Response mapping correctness
    @Test
    @DisplayName("IT-16: Should correctly map BillingResult to BillingResponse")
    void testCalculateBill_ResponseMapping_AllFieldsMapped() {
        // Arrange
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 13, 0);
        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("25.00"),
                new BigDecimal("7.50"),
                new BigDecimal("12.00"),
                new BigDecimal("29.50"),
                new BigDecimal("5.90"),
                new BigDecimal("35.40")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Act
        BillingResponse response = billingController.calculateBill(request);

        // Assert - Verify all fields mapped correctly
        assertAll("Verify complete response mapping",
                () -> assertEquals("session-001", response.sessionId()),
                () -> assertEquals("user-001", response.userId()),
                () -> assertEquals(new BigDecimal("25.00"), response.basePrice()),
                () -> assertEquals(new BigDecimal("7.50"), response.discountsTotal()),
                () -> assertEquals(new BigDecimal("12.00"), response.penaltiesTotal()),
                () -> assertEquals(new BigDecimal("29.50"), response.netPrice()),
                () -> assertEquals(new BigDecimal("5.90"), response.taxAmount()),
                () -> assertEquals(new BigDecimal("35.40"), response.finalPrice())
        );
    }

    // IT-17: VIP zone tariff retrieval
    @Test
    @DisplayName("IT-17: Should retrieve VIP tariff for VIP zone type")
    void testCalculateBill_VIPZone_VIPTariffUsed() {
        // Arrange
        ParkingSession vipSession = new ParkingSession(
                "session-vip",
                "user-001",
                "VIP-789",
                "zone-vip",
                "spot-vip",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.VIP,
                LocalDateTime.of(2026, 1, 15, 10, 0)
        );
        parkingSessionRepository.save(vipSession);

        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 13, 0);
        BillingRequest request = new BillingRequest(
                "session-vip",
                ZoneType.VIP,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("30.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("30.00"),
                new BigDecimal("6.00"),
                new BigDecimal("36.00")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(Tariff.class), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Act
        billingController.calculateBill(request);

        // Assert - Verify VIP tariff was used
        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyDouble(),
                eq(vipTariff), // Verify VIP tariff used
                any(),
                any(),
                any(),
                anyInt(),
                any(),
                any()
        );
    }

    // IT-18: Weekend billing
    @Test
    @DisplayName("IT-18: Should handle weekend billing correctly")
    void testCalculateBill_WeekendSession_ProcessedCorrectly() {
        // Arrange
        ParkingSession weekendSession = new ParkingSession(
                "session-weekend",
                "user-001",
                "ABC-123",
                "zone-001",
                "spot-001",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKEND,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 18, 10, 0) // Sunday
        );
        parkingSessionRepository.save(weekendSession);

        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 18, 14, 0);
        BillingRequest request = new BillingRequest(
                "session-weekend",
                ZoneType.STANDARD,
                DayType.WEEKEND,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("24.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("24.00"),
                new BigDecimal("4.80"),
                new BigDecimal("28.80")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Act
        BillingResponse response = billingController.calculateBill(request);

        // Assert
        assertAll("Verify weekend billing",
                () -> assertEquals(new BigDecimal("24.00"), response.basePrice()),
                () -> verify(billingService, times(1)).calculateBill(
                        any(),
                        any(),
                        any(),
                        eq(DayType.WEEKEND),
                        any(),
                        anyDouble(),
                        any(),
                        any(),
                        any(),
                        any(),
                        anyInt(),
                        any(),
                        any()
                )
        );
    }

    // IT-19: Holiday billing
    @Test
    @DisplayName("IT-19: Should handle holiday billing correctly")
    void testCalculateBill_HolidaySession_ProcessedCorrectly() {
        // Arrange
        ParkingSession holidaySession = new ParkingSession(
                "session-holiday",
                "user-001",
                "ABC-123",
                "zone-001",
                "spot-001",
                TimeOfDayBand.OFF_PEAK,
                DayType.HOLIDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 12, 25, 10, 0) // Christmas
        );
        parkingSessionRepository.save(holidaySession);

        LocalDateTime exitTime = LocalDateTime.of(2026, 12, 25, 15, 0);
        BillingRequest request = new BillingRequest(
                "session-holiday",
                ZoneType.STANDARD,
                DayType.HOLIDAY,
                TimeOfDayBand.OFF_PEAK,
                0.4,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("30.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("30.00"),
                new BigDecimal("6.00"),
                new BigDecimal("36.00")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Act
        BillingResponse response = billingController.calculateBill(request);

        // Assert
        assertAll("Verify holiday billing",
                () -> assertEquals(new BigDecimal("30.00"), response.basePrice()),
                () -> verify(billingService, times(1)).calculateBill(
                        any(),
                        any(),
                        any(),
                        eq(DayType.HOLIDAY),
                        any(),
                        anyDouble(),
                        any(),
                        any(),
                        any(),
                        any(),
                        anyInt(),
                        any(),
                        any()
                )
        );
    }

    // IT-20: Complex scenario with all features
    @Test
    @DisplayName("IT-20: Should handle complex scenario with multiple integrations")
    void testCalculateBill_ComplexScenario_AllComponentsIntegrated() {
        // Arrange - Complex setup
        PenaltyHistory penaltyHistory = penaltyHistoryRepository.getOrCreate("user-001");
        penaltyHistory.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                BigDecimal.valueOf(10.00),
                LocalDateTime.now()
        ));
        penaltyHistoryRepository.save("user-001", penaltyHistory);

        DiscountInfo premiumDiscount = new DiscountInfo(
                BigDecimal.valueOf(0.15),
                BigDecimal.valueOf(0.10),
                BigDecimal.valueOf(3.00),
                false,
                0
        );

        SubscriptionPlan premiumPlan = new SubscriptionPlan(
                3,
                2,
                10,
                12.0,
                false,
                true,
                true,
                premiumDiscount
        );

        subscriptionPlanRepository.save("user-001", premiumPlan);

        ParkingSession vipWeekendSession = new ParkingSession(
                "session-complex",
                "user-001",
                "VIP-999",
                "zone-vip",
                "spot-vip",
                TimeOfDayBand.PEAK,
                DayType.WEEKEND,
                ZoneType.VIP,
                LocalDateTime.of(2026, 1, 18, 8, 0)
        );
        parkingSessionRepository.save(vipWeekendSession);

        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 18, 15, 0);
        BillingRequest request = new BillingRequest(
                "session-complex",
                ZoneType.VIP,
                DayType.WEEKEND,
                TimeOfDayBand.PEAK,
                0.9,
                exitTime,
                BigDecimal.valueOf(25.00), // Should be ignored, history used
                0 // Should use plan's max duration
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("80.00"),
                new BigDecimal("25.00"),
                new BigDecimal("10.00"),
                new BigDecimal("65.00"),
                new BigDecimal("13.00"),
                new BigDecimal("78.00")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Act
        BillingResponse response = billingController.calculateBill(request);

        // Assert - Verify all integrations
        assertAll("Verify complex scenario integration",
                () -> assertEquals("session-complex", response.sessionId()),
                () -> assertEquals("user-001", response.userId()),
                () -> assertEquals(new BigDecimal("80.00"), response.basePrice()),
                () -> assertEquals(new BigDecimal("25.00"), response.discountsTotal()),
                () -> assertEquals(new BigDecimal("10.00"), response.penaltiesTotal()),
                () -> assertEquals(new BigDecimal("65.00"), response.netPrice()),
                () -> assertEquals(new BigDecimal("13.00"), response.taxAmount()),
                () -> assertEquals(new BigDecimal("78.00"), response.finalPrice())
        );

        // Verify service called with correct integrated values
        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                eq(ZoneType.VIP),
                eq(DayType.WEEKEND),
                eq(TimeOfDayBand.PEAK),
                eq(0.9),
                eq(vipTariff),
                eq(dynamicConfig),
                eq(premiumDiscount),
                eq(BigDecimal.valueOf(10.00)), // Penalties from history
                eq(12), // Max duration from plan
                any(),
                any()
        );

        // Verify session marked as paid
        ParkingSession updatedSession = parkingSessionRepository.findById("session-complex").orElseThrow();
        assertTrue(updatedSession.getState() == SessionState.PAID);

        // Verify billing record saved
        Optional<BillingRecord> savedRecord = billingRecordRepository.findBySessionId("session-complex");
        assertTrue(savedRecord.isPresent());
    }
}