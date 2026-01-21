package IntegrationTesting.ShpëtimShabanaj;

import Controller.BillingController;
import Dto.Billing.BillingRequest;
import Enum.DayType;
import Enum.TimeOfDayBand;
import Enum.ZoneType;
import Model.*;
import Repository.*;
import Repository.impl.InMemoryBillingRecordRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pairwise Integration: BillingController -- BillingRecordRepository")
@MockitoSettings(strictness = Strictness.LENIENT)
class BillingControllerBillingRecordRepositoryIntegrationTesting {

    private BillingController billingController;

    // REAL: BillingRecordRepository
    private BillingRecordRepository billingRecordRepository;

    // STUBS: All other dependencies
    @Mock private BillingService billingService;
    @Mock private TariffRepository tariffRepository;
    @Mock private DynamicPricingConfigRepository dynamicPricingConfigRepository;
    @Mock private ParkingSessionRepository parkingSessionRepository;
    @Mock private PenaltyHistoryRepository penaltyHistoryRepository;
    @Mock private SubscriptionPlanRepository subscriptionPlanRepository;

    private ParkingSession mockSession;
    private Tariff mockTariff;
    private DynamicPricingConfig mockConfig;
    private SubscriptionPlan mockPlan;

    @BeforeEach
    void setUp() {
        // REAL billing record repo
        billingRecordRepository = new InMemoryBillingRecordRepository();

        // Mock session
        mockSession = mock(ParkingSession.class);
        when(mockSession.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 15, 9, 0));
        when(mockSession.getUserId()).thenReturn("U1");

        mockTariff = new Tariff(
                ZoneType.STANDARD,
                new BigDecimal("5.00"),
                new BigDecimal("50.00"),
                new BigDecimal("20.00")
        );

        mockConfig = new DynamicPricingConfig(1.5, 0.8, 1.3);

        DiscountInfo mockDiscount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        mockPlan = new SubscriptionPlan(
                2, 1, 5, 8.0, false, false, false, mockDiscount
        );

        // Basic repo behaviour
        when(parkingSessionRepository.findById(anyString())).thenReturn(Optional.of(mockSession));
        when(tariffRepository.findByZoneType(any())).thenReturn(mockTariff);
        when(dynamicPricingConfigRepository.getActiveConfig()).thenReturn(mockConfig);
        when(subscriptionPlanRepository.getPlanForUser(anyString())).thenReturn(Optional.of(mockPlan));
        when(penaltyHistoryRepository.findById(anyString())).thenReturn(null);

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

        // Controller under test (no DiscountPolicyRepository in ctor anymore)
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

    // IT-01: Billing record saved after successful billing
    @Test
    @DisplayName("IT-01: Should save billing record to repository after successful billing")
    void testCalculateBillSuccessfulBillingRecordSaved() {
        BillingRequest request = new BillingRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        billingController.calculateBill(request);

        Optional<BillingRecord> savedRecord = billingRecordRepository.findBySessionId("S1");
        assertTrue(savedRecord.isPresent(), "Billing record should be saved");
    }

    // IT-02: Billing record contains correct session ID
    @Test
    @DisplayName("IT-02: Should save billing record with correct session ID")
    void testCalculateBillRecordSavedSessionIdCorrect() {
        BillingRequest request = new BillingRequest(
                "S2",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        billingController.calculateBill(request);

        Optional<BillingRecord> savedRecord = billingRecordRepository.findBySessionId("S2");
        assertTrue(savedRecord.isPresent());
        assertEquals("S2", savedRecord.get().getSessionId());
    }

    // IT-03: Billing record contains correct user ID from session
    @Test
    @DisplayName("IT-03: Should save billing record with user ID extracted from session")
    void testCalculateBillRecordSavedUserIdFromSession() {
        when(mockSession.getUserId()).thenReturn("U3");

        BillingRequest request = new BillingRequest(
                "S3",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        billingController.calculateBill(request);

        Optional<BillingRecord> savedRecord = billingRecordRepository.findBySessionId("S3");
        assertTrue(savedRecord.isPresent());
        assertEquals("U3", savedRecord.get().getUserId());
    }

    // IT-04: Billing record contains correct zone type
    @Test
    @DisplayName("IT-04: Should save billing record with correct zone type from request")
    void testCalculateBillRecordSavedZoneTypeCorrect() {
        BillingRequest request = new BillingRequest(
                "S4",
                ZoneType.VIP,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        billingController.calculateBill(request);

        Optional<BillingRecord> savedRecord = billingRecordRepository.findBySessionId("S4");
        assertTrue(savedRecord.isPresent());
        assertEquals(ZoneType.VIP, savedRecord.get().getZoneType());
    }

    // IT-05: Billing record contains correct entry time from session
    @Test
    @DisplayName("IT-05: Should save billing record with entry time from session start time")
    void testCalculateBillRecordSavedEntryTimeFromSession() {
        LocalDateTime sessionStartTime = LocalDateTime.of(2026, 1, 15, 8, 30);
        when(mockSession.getStartTime()).thenReturn(sessionStartTime);

        BillingRequest request = new BillingRequest(
                "S5",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        billingController.calculateBill(request);

        Optional<BillingRecord> savedRecord = billingRecordRepository.findBySessionId("S5");
        assertTrue(savedRecord.isPresent());
        assertEquals(sessionStartTime, savedRecord.get().getEntryTime());
    }

    // IT-06: Billing record contains correct exit time from request
    @Test
    @DisplayName("IT-06: Should save billing record with exit time from request")
    void testCalculateBillRecordSavedExitTimeFromRequest() {
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 14, 45);
        BillingRequest request = new BillingRequest(
                "S6",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        billingController.calculateBill(request);

        Optional<BillingRecord> savedRecord = billingRecordRepository.findBySessionId("S6");
        assertTrue(savedRecord.isPresent());
        assertEquals(exitTime, savedRecord.get().getExitTime());
    }

    // IT-07: Billing record contains billing result from service
    @Test
    @DisplayName("IT-07: Should save billing record with billing result from billing service")
    void testCalculateBillRecordSavedBillingResultIncluded() {
        BillingResult customResult = new BillingResult(
                new BigDecimal("25.00"),
                new BigDecimal("5.00"),
                new BigDecimal("10.00"),
                new BigDecimal("30.00"),
                new BigDecimal("6.00"),
                new BigDecimal("36.00")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(customResult);

        BillingRequest request = new BillingRequest(
                "S7",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        billingController.calculateBill(request);

        Optional<BillingRecord> savedRecord = billingRecordRepository.findBySessionId("S7");
        assertTrue(savedRecord.isPresent());

        BillingResult savedResult = savedRecord.get().getBillingResult();
        assertAll("Verify billing result saved correctly",
                () -> assertEquals(new BigDecimal("25.00"), savedResult.getBasePrice()),
                () -> assertEquals(new BigDecimal("5.00"), savedResult.getDiscountsTotal()),
                () -> assertEquals(new BigDecimal("10.00"), savedResult.getPenaltiesTotal()),
                () -> assertEquals(new BigDecimal("30.00"), savedResult.getNetPrice()),
                () -> assertEquals(new BigDecimal("6.00"), savedResult.getTaxAmount()),
                () -> assertEquals(new BigDecimal("36.00"), savedResult.getFinalPrice())
        );
    }

    // IT-08: Multiple billing records saved independently
    @Test
    @DisplayName("IT-08: Should save multiple billing records for different sessions independently")
    void testCalculateBillMultipleSessionsRecordsSavedIndependently() {
        // Session 1
        ParkingSession session1 = mock(ParkingSession.class);
        when(session1.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 15, 9, 0));
        when(session1.getUserId()).thenReturn("U1");
        when(parkingSessionRepository.findById("S8")).thenReturn(Optional.of(session1));

        BillingRequest request1 = new BillingRequest(
                "S8",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );
        billingController.calculateBill(request1);

        // Session 2
        ParkingSession session2 = mock(ParkingSession.class);
        when(session2.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 15, 10, 0));
        when(session2.getUserId()).thenReturn("U2");
        when(parkingSessionRepository.findById("S9")).thenReturn(Optional.of(session2));

        BillingResult result2 = new BillingResult(
                new BigDecimal("30.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("30.00"),
                new BigDecimal("6.00"),
                new BigDecimal("36.00")
        );
        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(result2);

        BillingRequest request2 = new BillingRequest(
                "S9",
                ZoneType.VIP,
                DayType.WEEKEND,
                TimeOfDayBand.PEAK,
                0.9,
                LocalDateTime.of(2026, 1, 18, 15, 0),
                BigDecimal.ZERO,
                24
        );
        billingController.calculateBill(request2);

        Optional<BillingRecord> record1 = billingRecordRepository.findBySessionId("S8");
        Optional<BillingRecord> record2 = billingRecordRepository.findBySessionId("S9");

        assertAll("Verify both records saved independently",
                () -> assertTrue(record1.isPresent()),
                () -> assertTrue(record2.isPresent()),
                () -> assertEquals("U1", record1.get().getUserId()),
                () -> assertEquals("U2", record2.get().getUserId()),
                () -> assertEquals(ZoneType.STANDARD, record1.get().getZoneType()),
                () -> assertEquals(ZoneType.VIP, record2.get().getZoneType())
        );
    }

    // IT-09: Record saved after billing service completes
    @Test
    @DisplayName("IT-09: Should save billing record after billing service completes")
    void testCalculateBillExecutionOrderRecordSavedAfterBillingService() {
        // Spy only on the REAL repository (allowed)
        BillingRecordRepository spyRepository = spy(billingRecordRepository);

        // Use the existing mocked billingService directly (do NOT spy on a mock)
        BillingController controllerWithSpies = new BillingController(
                billingService,
                tariffRepository,
                dynamicPricingConfigRepository,
                spyRepository,
                parkingSessionRepository,
                penaltyHistoryRepository,
                subscriptionPlanRepository
        );

        BillingRequest request = new BillingRequest(
                "S10",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        // Act
        controllerWithSpies.calculateBill(request);

        // Assert
        var inOrder = inOrder(billingService, spyRepository);
        inOrder.verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()
        );
        inOrder.verify(spyRepository).save(any(BillingRecord.class));
    }


    // IT-10: Repository save called exactly once per request
    @Test
    @DisplayName("IT-10: Should save billing record exactly once per billing request")
    void testCalculateBillRepositoryAccessSaveCalledOnce() {
        BillingRecordRepository spyRepository = spy(billingRecordRepository);

        BillingController controllerWithSpy = new BillingController(
                billingService,
                tariffRepository,
                dynamicPricingConfigRepository,
                spyRepository,
                parkingSessionRepository,
                penaltyHistoryRepository,
                subscriptionPlanRepository
        );

        BillingRequest request = new BillingRequest(
                "S11",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        controllerWithSpy.calculateBill(request);

        verify(spyRepository, times(1)).save(any(BillingRecord.class));
    }

    // IT-11: Record preserves all data integrity
    @Test
    @DisplayName("IT-11: Should preserve complete data integrity in saved billing record")
    void testCalculateBillRecordDataIntegrityAllFieldsPreserved() {
        LocalDateTime sessionStart = LocalDateTime.of(2026, 1, 15, 8, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 16, 30);

        when(mockSession.getStartTime()).thenReturn(sessionStart);
        when(mockSession.getUserId()).thenReturn("U11");

        BillingResult detailedResult = new BillingResult(
                new BigDecimal("42.50"),
                new BigDecimal("8.75"),
                new BigDecimal("12.25"),
                new BigDecimal("46.00"),
                new BigDecimal("9.20"),
                new BigDecimal("55.20")
        );

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(detailedResult);

        BillingRequest request = new BillingRequest(
                "S12",
                ZoneType.EV,
                DayType.WEEKEND,
                TimeOfDayBand.PEAK,
                0.85,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        billingController.calculateBill(request);

        Optional<BillingRecord> savedRecord = billingRecordRepository.findBySessionId("S12");
        assertTrue(savedRecord.isPresent());

        BillingRecord record = savedRecord.get();
        assertAll("Verify complete data integrity",
                () -> assertEquals("S12", record.getSessionId()),
                () -> assertEquals("U11", record.getUserId()),
                () -> assertEquals(ZoneType.EV, record.getZoneType()),
                () -> assertEquals(sessionStart, record.getEntryTime()),
                () -> assertEquals(exitTime, record.getExitTime()),
                () -> assertNotNull(record.getBillingResult()),
                () -> assertEquals(new BigDecimal("42.50"), record.getBillingResult().getBasePrice()),
                () -> assertEquals(new BigDecimal("8.75"), record.getBillingResult().getDiscountsTotal()),
                () -> assertEquals(new BigDecimal("12.25"), record.getBillingResult().getPenaltiesTotal()),
                () -> assertEquals(new BigDecimal("46.00"), record.getBillingResult().getNetPrice()),
                () -> assertEquals(new BigDecimal("9.20"), record.getBillingResult().getTaxAmount()),
                () -> assertEquals(new BigDecimal("55.20"), record.getBillingResult().getFinalPrice())
        );
    }

    // IT-12: Record not saved if billing service throws exception
    @Test
    @DisplayName("IT-12: Should not save billing record if billing service throws exception")
    void testCalculateBillBillingServiceExceptionRecordNotSaved() {
        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenThrow(new RuntimeException("Billing calculation failed"));

        BillingRequest request = new BillingRequest(
                "S13",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        assertThrows(RuntimeException.class, () -> billingController.calculateBill(request));

        Optional<BillingRecord> record = billingRecordRepository.findBySessionId("S13");
        assertFalse(record.isPresent(), "Record should not be saved when billing service fails");
    }
}

