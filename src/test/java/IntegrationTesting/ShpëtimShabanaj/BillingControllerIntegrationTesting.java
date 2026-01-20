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
import Repository.*;
import Repository.impl.*;
import Service.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

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

    @BeforeEach
    void setUp() {
        // Setup tariffs
        standardTariff = new Tariff(
                ZoneType.STANDARD,
                new BigDecimal("5.00"),
                new BigDecimal("50.00"),
                new BigDecimal("20.00")
        );

        evTariff = new Tariff(
                ZoneType.EV,
                new BigDecimal("3.00"),
                new BigDecimal("30.00"),
                new BigDecimal("15.00")
        );

        vipTariff = new Tariff(
                ZoneType.VIP,
                new BigDecimal("10.00"),
                new BigDecimal("80.00"),
                new BigDecimal("25.00")
        );

        Map<ZoneType, Tariff> tariffMap = new EnumMap<>(ZoneType.class);
        tariffMap.put(ZoneType.STANDARD, standardTariff);
        tariffMap.put(ZoneType.EV, evTariff);
        tariffMap.put(ZoneType.VIP, vipTariff);

        tariffRepository = new InMemoryTariffRepository(tariffMap);

        // Dynamic pricing config
        dynamicConfig = new DynamicPricingConfig(1.5, 0.8, 1.3);
        dynamicPricingConfigRepository = new InMemoryDynamicPricingConfigRepository(dynamicConfig);

        // Real in-memory repositories
        billingRecordRepository = new InMemoryBillingRecordRepository();
        parkingSessionRepository = new InMemoryParkingSessionRepository();
        penaltyHistoryRepository = new InMemoryPenaltyHistoryRepository();
        subscriptionPlanRepository = new InMemorySubscriptionPlanRepository();

        // Controller with stubbed service + real repos
        billingController = new BillingController(
                billingService,
                tariffRepository,
                dynamicPricingConfigRepository,
                billingRecordRepository,
                parkingSessionRepository,
                penaltyHistoryRepository,
                subscriptionPlanRepository
        );

        setupTestSession();
        setupTestSubscriptionPlan();
    }

    private void setupTestSession() {
        testSession = new ParkingSession(
                "S1",
                "U1",
                "AA111",
                "Z1",
                "S1",
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
                2,
                1,
                5,
                8.0,
                false,
                false,
                false,
                discountInfo
        );

        subscriptionPlanRepository.save("U1", testPlan);
    }

    // IT-01: Happy path - complete billing flow
    @Test
    @DisplayName("IT-01: Should orchestrate complete billing flow with all components")
    void testCalculateBillStandardSessionCompleteFlow() {
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 13, 0);
        BillingRequest request = new BillingRequest(
                "S1",
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

        BillingResponse response = billingController.calculateBill(request);

        assertAll("Verify complete billing orchestration",
                () -> assertEquals("S1", response.sessionId()),
                () -> assertEquals("U1", response.userId()),
                () -> assertEquals(new BigDecimal("20.00"), response.basePrice()),
                () -> assertEquals(BigDecimal.ZERO, response.discountsTotal()),
                () -> assertEquals(BigDecimal.ZERO, response.penaltiesTotal()),
                () -> assertEquals(new BigDecimal("20.00"), response.netPrice()),
                () -> assertEquals(new BigDecimal("4.00"), response.taxAmount()),
                () -> assertEquals(new BigDecimal("24.00"), response.finalPrice())
        );

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

        Optional<BillingRecord> savedRecord = billingRecordRepository.findBySessionId("S1");
        assertTrue(savedRecord.isPresent());
        assertEquals("U1", savedRecord.get().getUserId());

        ParkingSession session = parkingSessionRepository.findById("S1").orElseThrow();
        assertEquals(SessionState.PAID, session.getState());
    }

    // IT-02: Correct tariff retrieval from repository
    @Test
    @DisplayName("IT-02: Should retrieve correct tariff from repository based on zone type")
    void testCalculateBillTariffRetrievalCorrectTariffUsed() {
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 12, 0);
        BillingRequest request = new BillingRequest(
                "S1",
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

        billingController.calculateBill(request);

        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyDouble(),
                eq(standardTariff),
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
    void testCalculateBillEVZoneEVTariffUsed() {
        ParkingSession evSession = new ParkingSession(
                "S2",
                "U1",
                "AA222",
                "Z2",
                "S2",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.EV,
                LocalDateTime.of(2026, 1, 15, 10, 0)
        );
        parkingSessionRepository.save(evSession);

        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 14, 0);
        BillingRequest request = new BillingRequest(
                "S2",
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

        billingController.calculateBill(request);

        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyDouble(),
                eq(evTariff),
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
    void testCalculateBillDynamicConfigActiveConfigUsed() {
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 11, 0);
        BillingRequest request = new BillingRequest(
                "S1",
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

        billingController.calculateBill(request);

        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyDouble(),
                any(),
                eq(dynamicConfig),
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
    void testCalculateBillWithPenaltiesPenaltiesRetrievedFromRepository() {
        PenaltyHistory penaltyHistory = penaltyHistoryRepository.getOrCreate("U1");
        penaltyHistory.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("15.00"),
                LocalDateTime.now()
        ));
        penaltyHistoryRepository.save("U1", penaltyHistory);

        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 11, 0);
        BillingRequest request = new BillingRequest(
                "S1",
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

        BillingResponse response = billingController.calculateBill(request);

        assertEquals(new BigDecimal("15.00"), response.penaltiesTotal());

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
                eq(new BigDecimal("15.00")),
                anyInt(),
                any(),
                any()
        );
    }

    // IT-06: Penalties from request should be ignored (history takes precedence)
    @Test
    @DisplayName("IT-06: Should use penalties from history, not from request")
    void testCalculateBillPenaltiesInRequestHistoryTakesPrecedence() {
        PenaltyHistory penaltyHistory = penaltyHistoryRepository.getOrCreate("U1");
        penaltyHistory.addPenalty(new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("20.00"),
                LocalDateTime.now()
        ));
        penaltyHistoryRepository.save("U1", penaltyHistory);

        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 12, 0);
        BillingRequest request = new BillingRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                new BigDecimal("5.00"),
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

        billingController.calculateBill(request);

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
                eq(new BigDecimal("20.00")),
                anyInt(),
                any(),
                any()
        );
    }

    // IT-07: No penalty history for user
    @Test
    @DisplayName("IT-07: Should handle user with no penalty history (zero penalties)")
    void testCalculateBillNoPenaltyHistoryZeroPenaltiesUsed() {
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 12, 0);
        BillingRequest request = new BillingRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                new BigDecimal("10.00"),
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

        billingController.calculateBill(request);

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
                eq(BigDecimal.ZERO),
                anyInt(),
                any(),
                any()
        );
    }

    // IT-08: Subscription plan integration
    @Test
    @DisplayName("IT-08: Should retrieve and use subscription plan discount info")
    void testCalculateBillSubscriptionPlanDiscountInfoUsed() {
        DiscountInfo premiumDiscount = new DiscountInfo(
                new BigDecimal("0.20"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
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
                false,
                premiumDiscount
        );

        subscriptionPlanRepository.save("U1", premiumPlan);

        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 14, 0);
        BillingRequest request = new BillingRequest(
                "S1",
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

        billingController.calculateBill(request);

        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyDouble(),
                any(),
                any(),
                eq(premiumDiscount),
                any(),
                anyInt(),
                any(),
                any()
        );
    }

    // IT-09: Max duration from subscription plan
    @Test
    @DisplayName("IT-09: Should use subscription plan max duration when request has zero or negative")
    void testCalculateBillPlanMaxDurationUsedWhenRequestHasZero() {
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 17, 0);
        BillingRequest request = new BillingRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                0
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

        billingController.calculateBill(request);

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
                eq(8),
                any(),
                any()
        );
    }

    // IT-10: Request max duration takes precedence when provided
    @Test
    @DisplayName("IT-10: Should use request max duration when it's greater than zero")
    void testCalculateBillRequestMaxDurationUsedWhenProvided() {
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 17, 0);
        BillingRequest request = new BillingRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                12
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

        billingController.calculateBill(request);

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
                eq(12),
                any(),
                any()
        );
    }

    // IT-11: Billing record persistence
    @Test
    @DisplayName("IT-11: Should save billing record with complete information")
    void testCalculateBillBillingRecordSavedCorrectly() {
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 13, 0);
        BillingRequest request = new BillingRequest(
                "S1",
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

        billingController.calculateBill(request);

        Optional<BillingRecord> savedRecord = billingRecordRepository.findBySessionId("S1");
        assertTrue(savedRecord.isPresent());

        BillingRecord record = savedRecord.get();
        assertAll("Verify billing record contents",
                () -> assertEquals("S1", record.getSessionId()),
                () -> assertEquals("U1", record.getUserId()),
                () -> assertEquals(ZoneType.STANDARD, record.getZoneType()),
                () -> assertEquals(testSession.getStartTime(), record.getEntryTime()),
                () -> assertEquals(exitTime, record.getExitTime()),
                () -> assertNotNull(record.getBillingResult())
        );
    }

    // IT-12: Session marked as paid
    @Test
    @DisplayName("IT-12: Should mark parking session as paid after billing")
    void testCalculateBillSessionStateMarkedAsPaid() {
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 13, 0);
        BillingRequest request = new BillingRequest(
                "S1",
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

        assertNotEquals(SessionState.PAID, testSession.getState());

        billingController.calculateBill(request);

        ParkingSession updatedSession = parkingSessionRepository.findById("S1").orElseThrow();
        assertEquals(SessionState.PAID, updatedSession.getState());
    }

    // IT-13: Null request validation
    @Test
    @DisplayName("IT-13: Should throw NullPointerException for null request")
    void testCalculateBillNullRequestThrowsException() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            billingController.calculateBill(null);
        });

        assertEquals("request must not be null", exception.getMessage());
        verifyNoInteractions(billingService);
    }

    // IT-14: Session not found
    @Test
    @DisplayName("IT-14: Should throw NoSuchElementException when session not found")
    void testCalculateBillSessionNotFoundThrowsException() {
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

        assertThrows(NoSuchElementException.class, () -> billingController.calculateBill(request));

        verifyNoInteractions(billingService);
    }

    // IT-15: Subscription plan not found
    @Test
    @DisplayName("IT-15: Should throw NoSuchElementException when subscription plan not found")
    void testCalculateBillPlanNotFoundThrowsException() {
        ParkingSession sessionWithoutPlan = new ParkingSession(
                "session-no-plan",
                "user-no-plan",
                "AA999",
                "Z1",
                "S1",
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

        assertThrows(NoSuchElementException.class, () -> billingController.calculateBill(request));

        verifyNoInteractions(billingService);
    }

    // IT-16: Response mapping correctness
    @Test
    @DisplayName("IT-16: Should correctly map BillingResult to BillingResponse")
    void testCalculateBillResponseMappingAllFieldsMapped() {
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 13, 0);
        BillingRequest request = new BillingRequest(
                "S1",
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

        BillingResponse response = billingController.calculateBill(request);

        assertAll("Verify complete response mapping",
                () -> assertEquals("S1", response.sessionId()),
                () -> assertEquals("U1", response.userId()),
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
    void testCalculateBillVIPZoneVIPTariffUsed() {
        ParkingSession vipSession = new ParkingSession(
                "S3",
                "U1",
                "AA333",
                "Z3",
                "S3",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.VIP,
                LocalDateTime.of(2026, 1, 15, 10, 0)
        );
        parkingSessionRepository.save(vipSession);

        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 13, 0);
        BillingRequest request = new BillingRequest(
                "S3",
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

        billingController.calculateBill(request);

        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyDouble(),
                eq(vipTariff),
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
    void testCalculateBillWeekendSessionProcessedCorrectly() {
        ParkingSession weekendSession = new ParkingSession(
                "session-weekend",
                "U1",
                "AA111",
                "Z1",
                "S1",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKEND,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 18, 10, 0)
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

        BillingResponse response = billingController.calculateBill(request);

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
    void testCalculateBillHolidaySessionProcessedCorrectly() {
        ParkingSession holidaySession = new ParkingSession(
                "session-holiday",
                "U1",
                "AA111",
                "Z1",
                "S1",
                TimeOfDayBand.OFF_PEAK,
                DayType.HOLIDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 12, 25, 10, 0)
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

        BillingResponse response = billingController.calculateBill(request);

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
    void testCalculateBillComplexScenarioAllComponentsIntegrated() {
        PenaltyHistory penaltyHistory = penaltyHistoryRepository.getOrCreate("U1");
        penaltyHistory.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("10.00"),
                LocalDateTime.now()
        ));
        penaltyHistoryRepository.save("U1", penaltyHistory);

        DiscountInfo premiumDiscount = new DiscountInfo(
                new BigDecimal("0.15"),
                new BigDecimal("0.10"),
                new BigDecimal("3.00"),
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

        subscriptionPlanRepository.save("U1", premiumPlan);

        ParkingSession vipWeekendSession = new ParkingSession(
                "S5",
                "U1",
                "AA333",
                "Z3",
                "S3",
                TimeOfDayBand.PEAK,
                DayType.WEEKEND,
                ZoneType.VIP,
                LocalDateTime.of(2026, 1, 18, 8, 0)
        );
        parkingSessionRepository.save(vipWeekendSession);

        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 18, 15, 0);
        BillingRequest request = new BillingRequest(
                "S5",
                ZoneType.VIP,
                DayType.WEEKEND,
                TimeOfDayBand.PEAK,
                0.9,
                exitTime,
                new BigDecimal("25.00"),
                0
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

        BillingResponse response = billingController.calculateBill(request);

        assertAll("Verify complex scenario integration",
                () -> assertEquals("S5", response.sessionId()),
                () -> assertEquals("U1", response.userId()),
                () -> assertEquals(new BigDecimal("80.00"), response.basePrice()),
                () -> assertEquals(new BigDecimal("25.00"), response.discountsTotal()),
                () -> assertEquals(new BigDecimal("10.00"), response.penaltiesTotal()),
                () -> assertEquals(new BigDecimal("65.00"), response.netPrice()),
                () -> assertEquals(new BigDecimal("13.00"), response.taxAmount()),
                () -> assertEquals(new BigDecimal("78.00"), response.finalPrice())
        );

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
                eq(new BigDecimal("10.00")),
                eq(12),
                any(),
                any()
        );

        ParkingSession updatedSession = parkingSessionRepository.findById("S5").orElseThrow();
        assertEquals(SessionState.PAID, updatedSession.getState());

        Optional<BillingRecord> savedRecord = billingRecordRepository.findBySessionId("S5");
        assertTrue(savedRecord.isPresent());
    }
}