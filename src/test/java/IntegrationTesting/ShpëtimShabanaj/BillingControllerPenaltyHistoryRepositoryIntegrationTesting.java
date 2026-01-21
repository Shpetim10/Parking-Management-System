package IntegrationTesting.ShpëtimShabanaj;

import Controller.BillingController;
import Dto.Billing.BillingRequest;
import Enum.DayType;
import Enum.PenaltyType;
import Enum.TimeOfDayBand;
import Enum.ZoneType;
import Model.*;
import Repository.*;
import Repository.impl.InMemoryPenaltyHistoryRepository;
import Service.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pairwise Integration: BillingController -- PenaltyHistoryRepository")
@MockitoSettings(strictness = Strictness.LENIENT)
class BillingControllerPenaltyHistoryRepositoryIntegrationTesting {

    private BillingController billingController;

    // Real PenaltyHistoryRepository
    private PenaltyHistoryRepository penaltyHistoryRepository;

    // Other dependencies as mocks
    @Mock private BillingService billingService;
    @Mock private TariffRepository tariffRepository;
    @Mock private DynamicPricingConfigRepository dynamicPricingConfigRepository;
    @Mock private DiscountPolicyRepository discountPolicyRepository;
    @Mock private BillingRecordRepository billingRecordRepository;
    @Mock private ParkingSessionRepository parkingSessionRepository;
    @Mock private SubscriptionPlanRepository subscriptionPlanRepository;

    private ParkingSession mockSession;
    private SubscriptionPlan mockPlan;
    private Tariff mockTariff;
    private DynamicPricingConfig mockConfig;

    @BeforeEach
    void setUp() {
        // Real penalty history repo
        penaltyHistoryRepository = new InMemoryPenaltyHistoryRepository();

        // Session & user
        mockSession = mock(ParkingSession.class);
        when(mockSession.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 15, 9, 0));
        when(mockSession.getUserId()).thenReturn("U1");

        DiscountInfo mockDiscount = new DiscountInfo(
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                false,
                0
        );

        mockPlan = new SubscriptionPlan(
                2, 1, 5, 8.0, false, false, false, mockDiscount
        );

        mockTariff = new Tariff(
                ZoneType.STANDARD,
                new BigDecimal("5.00"),
                new BigDecimal("50.00"),
                new BigDecimal("20.00")
        );

        mockConfig = new DynamicPricingConfig(1.5, 0.8, 1.3);

        // Basic repository behaviour so controller can run
        when(parkingSessionRepository.findById(anyString())).thenReturn(Optional.of(mockSession));
        when(subscriptionPlanRepository.getPlanForUser(anyString())).thenReturn(Optional.of(mockPlan));
        when(tariffRepository.findByZoneType(any())).thenReturn(mockTariff);
        when(dynamicPricingConfigRepository.getActiveConfig()).thenReturn(mockConfig);

        // Billing service result – contents not important for these tests
        BillingResult mockResult = new BillingResult(
                new BigDecimal("20.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("20.00"),
                new BigDecimal("4.00"),
                new BigDecimal("24.00")
        );
        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Controller under test (new constructor with discountPolicyRepository)
        billingController = new BillingController(
                billingService,
                tariffRepository,
                dynamicPricingConfigRepository,
                billingRecordRepository,
                parkingSessionRepository,
                penaltyHistoryRepository,
                subscriptionPlanRepository
        );
    }

    private BillingRequest createRequest(
            String sessionId,
            ZoneType zoneType,
            DayType dayType,
            TimeOfDayBand band
    ) {
        return new BillingRequest(
                sessionId,
                zoneType,
                dayType,
                band,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );
    }

    // IT-01: No penalty history exists for user
    @Test
    @DisplayName("IT-01: Should pass zero penalties when no penalty history exists for user")
    void testCalculateBillNoPenaltyHistoryZeroPenaltiesPassed() {
        BillingRequest request = createRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK
        );

        billingController.calculateBill(request);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(),
                eq(BigDecimal.ZERO),
                anyInt(), any(), any()
        );
    }

    // IT-02: Penalty history with single penalty
    @Test
    @DisplayName("IT-02: Should retrieve and pass penalty total from history with single penalty")
    void testCalculateBillSinglePenaltyTotalPassedToBillingService() {
        PenaltyHistory history = penaltyHistoryRepository.getOrCreate("U1");
        history.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("15.00"),
                LocalDateTime.of(2026, 1, 14, 18, 0)
        ));
        penaltyHistoryRepository.save("U1", history);

        BillingRequest request = createRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK
        );

        billingController.calculateBill(request);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(),
                eq(new BigDecimal("15.00")),
                anyInt(), any(), any()
        );
    }

    // IT-03: Penalty history with multiple penalties
    @Test
    @DisplayName("IT-03: Should calculate and pass total of multiple penalties from history")
    void testCalculateBillMultiplePenaltiesTotalCalculatedAndPassed() {
        PenaltyHistory history = penaltyHistoryRepository.getOrCreate("U1");
        history.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("15.00"),
                LocalDateTime.of(2026, 1, 10, 18, 0)
        ));
        history.addPenalty(new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("25.00"),
                LocalDateTime.of(2026, 1, 12, 14, 0)
        ));
        history.addPenalty(new Penalty(
                PenaltyType.MISUSE,
                new BigDecimal("10.50"),
                LocalDateTime.of(2026, 1, 14, 11, 30)
        ));
        penaltyHistoryRepository.save("U1", history);

        BillingRequest request = createRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK
        );

        billingController.calculateBill(request);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(),
                eq(new BigDecimal("50.50")), // 15.00 + 25.00 + 10.50
                anyInt(), any(), any()
        );
    }

    // IT-04: Request penalties ignored when history exists
    @Test
    @DisplayName("IT-04: Should use penalties from history, ignoring penalties in request")
    void testCalculateBillPenaltiesInRequestHistoryTakesPrecedence() {
        PenaltyHistory history = penaltyHistoryRepository.getOrCreate("U1");
        history.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("20.00"),
                LocalDateTime.of(2026, 1, 14, 18, 0)
        ));
        penaltyHistoryRepository.save("U1", history);

        BillingRequest request = new BillingRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                new BigDecimal("5.00"),
                24
        );

        billingController.calculateBill(request);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(),
                eq(new BigDecimal("20.00")),
                anyInt(), any(), any()
        );
    }

    // IT-05: Request penalties ignored when no history exists
    @Test
    @DisplayName("IT-05: Should use zero penalties when no history exists, even if request has penalties")
    void testCalculateBillNoPenaltyHistoryButRequestHasPenalties_ZeroUsed() {
        BillingRequest request = new BillingRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                new BigDecimal("50.00"),
                24
        );

        billingController.calculateBill(request);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(),
                eq(BigDecimal.ZERO),
                anyInt(), any(), any()
        );
    }

    // IT-06: Different users have independent penalty histories
    @Test
    @DisplayName("IT-06: Should retrieve correct penalties for different users independently")
    void testCalculateBillMultipleUsersIndependentPenaltyHistories() {
        // U1 penalties
        PenaltyHistory history1 = penaltyHistoryRepository.getOrCreate("U1");
        history1.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("15.00"),
                LocalDateTime.now()
        ));
        penaltyHistoryRepository.save("U1", history1);

        // U2 penalties
        PenaltyHistory history2 = penaltyHistoryRepository.getOrCreate("U2");
        history2.addPenalty(new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("30.00"),
                LocalDateTime.now()
        ));
        penaltyHistoryRepository.save("U2", history2);

        // Make repository return different sessions per sessionId
        ParkingSession sessionU1 = mock(ParkingSession.class);
        when(sessionU1.getUserId()).thenReturn("U1");
        when(sessionU1.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 15, 9, 0));

        ParkingSession sessionU2 = mock(ParkingSession.class);
        when(sessionU2.getUserId()).thenReturn("U2");
        when(sessionU2.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 15, 10, 0));

        when(parkingSessionRepository.findById("S1")).thenReturn(Optional.of(sessionU1));
        when(parkingSessionRepository.findById("S2")).thenReturn(Optional.of(sessionU2));

        BillingRequest request1 = createRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK
        );

        BillingRequest request2 = createRequest(
                "S2",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK
        );

        billingController.calculateBill(request1);
        billingController.calculateBill(request2);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(),
                eq(new BigDecimal("15.00")),
                anyInt(), any(), any()
        );
        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(),
                eq(new BigDecimal("30.00")),
                anyInt(), any(), any()
        );
    }

    // IT-07: Penalty history accessed exactly once per request
    @Test
    @DisplayName("IT-07: Should access penalty history repository exactly once per billing request")
    void testCalculateBillRepositoryAccessCalledOnce() {
        PenaltyHistoryRepository spyRepository = spy(penaltyHistoryRepository);

        BillingController controllerWithSpy = new BillingController(
                billingService,
                tariffRepository,
                dynamicPricingConfigRepository,
                billingRecordRepository,
                parkingSessionRepository,
                spyRepository,
                subscriptionPlanRepository
        );

        PenaltyHistory history = spyRepository.getOrCreate("U1");
        history.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("15.00"),
                LocalDateTime.now()
        ));
        spyRepository.save("U1", history);

        BillingRequest request = createRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK
        );

        controllerWithSpy.calculateBill(request);

        verify(spyRepository, times(1)).findById("U1");
    }

    // IT-08: Zero penalty amount in history
    @Test
    @DisplayName("IT-08: Should handle penalty history with zero total amount")
    void testCalculateBillZeroPenaltyAmountZeroPassed() {
        PenaltyHistory history = penaltyHistoryRepository.getOrCreate("U1");
        penaltyHistoryRepository.save("U1", history); // no penalties

        BillingRequest request = createRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK
        );

        billingController.calculateBill(request);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(),
                eq(BigDecimal.ZERO),
                anyInt(), any(), any()
        );
    }

    // IT-09: Large penalty amount
    @Test
    @DisplayName("IT-09: Should handle large penalty amounts correctly")
    void testCalculateBillLargePenaltyAmountCorrectlyPassed() {
        PenaltyHistory history = penaltyHistoryRepository.getOrCreate("U1");
        history.addPenalty(new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("999.99"),
                LocalDateTime.now()
        ));
        history.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("500.50"),
                LocalDateTime.now()
        ));
        penaltyHistoryRepository.save("U1", history);

        BillingRequest request = createRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK
        );

        billingController.calculateBill(request);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(),
                eq(new BigDecimal("1500.49")), // 999.99 + 500.50
                anyInt(), any(), any()
        );
    }

    // IT-10: Penalty history retrieved before billing service call
    @Test
    @DisplayName("IT-10: Should retrieve penalties from history before calling billing service")
    void testCalculateBillExecutionOrderPenaltyHistoryBeforeBillingService() {
        PenaltyHistoryRepository spyRepository = spy(penaltyHistoryRepository);

        BillingController controllerWithSpies = new BillingController(
                billingService,
                tariffRepository,
                dynamicPricingConfigRepository,
                billingRecordRepository,
                parkingSessionRepository,
                spyRepository,
                subscriptionPlanRepository
        );

        PenaltyHistory history = spyRepository.getOrCreate("U1");
        history.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("15.00"),
                LocalDateTime.now()
        ));
        spyRepository.save("U1", history);

        BillingRequest request = createRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK
        );

        controllerWithSpies.calculateBill(request);

        var inOrder = inOrder(spyRepository, billingService);
        inOrder.verify(spyRepository).findById("U1");
        inOrder.verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()
        );
    }

    // IT-11: Penalty types are preserved in history
    @Test
    @DisplayName("IT-11: Should correctly sum penalties of different types from history")
    void testCalculateBillMultiplePenaltyTypesAllTypesIncludedInTotal() {
        PenaltyHistory history = penaltyHistoryRepository.getOrCreate("U1");
        history.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("10.00"),
                LocalDateTime.now()
        ));
        history.addPenalty(new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("20.00"),
                LocalDateTime.now()
        ));
        history.addPenalty(new Penalty(
                PenaltyType.MISUSE,
                new BigDecimal("15.00"),
                LocalDateTime.now()
        ));
        penaltyHistoryRepository.save("U1", history);

        BillingRequest request = createRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK
        );

        billingController.calculateBill(request);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(),
                eq(new BigDecimal("45.00")), // 10 + 20 + 15
                anyInt(), any(), any()
        );
    }

    // IT-12: Penalty decimal precision preserved
    @Test
    @DisplayName("IT-12: Should preserve decimal precision in penalty amounts")
    void testCalculateBillDecimalPenaltiesPrecisionPreserved() {
        PenaltyHistory history = penaltyHistoryRepository.getOrCreate("U1");
        history.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("12.50"),
                LocalDateTime.now()
        ));
        history.addPenalty(new Penalty(
                PenaltyType.MISUSE,
                new BigDecimal("7.75"),
                LocalDateTime.now()
        ));
        penaltyHistoryRepository.save("U1", history);

        BillingRequest request = createRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK
        );

        billingController.calculateBill(request);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(),
                eq(new BigDecimal("20.25")), // 12.50 + 7.75
                anyInt(), any(), any()
        );
    }
}
