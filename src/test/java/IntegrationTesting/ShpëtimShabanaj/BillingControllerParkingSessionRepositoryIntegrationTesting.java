package IntegrationTesting.ShpëtimShabanaj;

import Controller.BillingController;
import Dto.Billing.BillingRequest;
import Dto.Billing.BillingResponse;
import Enum.SessionState;
import Enum.DayType;
import Enum.TimeOfDayBand;
import Enum.ZoneType;
import Model.*;
import Repository.*;
import Repository.impl.InMemoryParkingSessionRepository;
import Service.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pairwise Integration: BillingController -- ParkingSessionRepository")
@MockitoSettings(strictness = Strictness.LENIENT)
class BillingControllerParkingSessionRepositoryIntegrationTesting {

    private BillingController billingController;

    // Real ParkingSessionRepository
    private ParkingSessionRepository parkingSessionRepository;

    // Stub all other dependencies
    @Mock private BillingService billingService;
    @Mock private TariffRepository tariffRepository;
    @Mock private DynamicPricingConfigRepository dynamicPricingConfigRepository;
    @Mock private BillingRecordRepository billingRecordRepository;
    @Mock private PenaltyHistoryRepository penaltyHistoryRepository;
    @Mock private SubscriptionPlanRepository subscriptionPlanRepository;

    private Tariff mockTariff;
    private DynamicPricingConfig mockConfig;
    private SubscriptionPlan mockPlan;

    @BeforeEach
    void setUp() {
        // Real parking session repo
        parkingSessionRepository = new InMemoryParkingSessionRepository();

        // Tariff, config, plan
        mockTariff = new Tariff(
                ZoneType.STANDARD,
                new BigDecimal("5.00"),
                new BigDecimal("50.00"),
                new BigDecimal("20.00")
        );

        mockConfig = new DynamicPricingConfig(1.5, 0.8, 1.3);

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

        // Repository behaviors used by controller
        when(tariffRepository.findByZoneType(any())).thenReturn(mockTariff);
        when(dynamicPricingConfigRepository.getActiveConfig()).thenReturn(mockConfig);
        when(subscriptionPlanRepository.getPlanForUser(anyString())).thenReturn(Optional.of(mockPlan));
        when(penaltyHistoryRepository.findById(anyString())).thenReturn(null);

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

    private BillingRequest createRequest(String sessionId,
                                         ZoneType zoneType,
                                         DayType dayType,
                                         TimeOfDayBand band,
                                         double occupancy,
                                         LocalDateTime endTime) {
        return new BillingRequest(
                sessionId,
                zoneType,
                dayType,
                band,
                occupancy,
                endTime,
                new BigDecimal("0.00"),
                24
        );
    }

    // IT-01: Session found and both times passed correctly
    @Test
    @DisplayName("IT-01: Should retrieve parking session and pass correct entry/exit times")
    void testCalculateBillSessionExistsTimesPassedCorrectly() {
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 15, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 15, 13, 0);

        ParkingSession session = new ParkingSession(
                "S1",
                "U1",
                "AA111",
                "Z1",
                "SP1",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                startTime
        );
        parkingSessionRepository.save(session);

        BillingRequest request = createRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                endTime
        );

        billingController.calculateBill(request);

        verify(billingService).calculateBill(
                eq(startTime),
                eq(endTime),
                any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()
        );
    }

    // IT-02: Session not found throws exception
    @Test
    @DisplayName("IT-02: Should throw NoSuchElementException when session not found")
    void testCalculateBillSessionNotFoundThrowsException() {
        BillingRequest request = createRequest(
                "S2",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0)
        );

        assertThrows(NoSuchElementException.class, () -> billingController.calculateBill(request));

        verifyNoInteractions(billingService);
    }

    // IT-03: User ID extracted from session
    @Test
    @DisplayName("IT-03: Should extract user ID from session for penalty and plan lookups")
    void testCalculateBillUserIdFromSessionUsedForLookups() {
        ParkingSession session = new ParkingSession(
                "S3",
                "U3",
                "AA222",
                "Z3",
                "SP3",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 15, 9, 0)
        );
        parkingSessionRepository.save(session);

        BillingRequest request = createRequest(
                "S3",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0)
        );

        billingController.calculateBill(request);

        verify(penaltyHistoryRepository).findById("U3");
        verify(subscriptionPlanRepository).getPlanForUser("U3");
    }

    // IT-04: Session marked as paid after billing
    @Test
    @DisplayName("IT-04: Should mark session as paid after successful billing")
    void testCalculateBillSuccessfulBillingSessionMarkedPaid() {
        ParkingSession session = new ParkingSession(
                "S4",
                "U4",
                "AA333",
                "Z4",
                "SP4",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 15, 9, 0)
        );
        parkingSessionRepository.save(session);

        assertNotEquals(SessionState.PAID, session.getState(), "Session should not be paid initially");

        BillingRequest request = createRequest(
                "S4",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0)
        );

        billingController.calculateBill(request);

        ParkingSession updatedSession = parkingSessionRepository.findById("S4").orElseThrow();
        assertEquals(SessionState.PAID, updatedSession.getState(), "Session should be marked as paid");
    }

    // IT-05: Different sessions for different users
    @Test
    @DisplayName("IT-05: Should handle different sessions for different users independently")
    void testCalculateBillMultipleSessionsIndependentHandling() {
        ParkingSession session1 = new ParkingSession(
                "S5",
                "U5",
                "AA444",
                "Z5",
                "SP5",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 15, 9, 0)
        );
        parkingSessionRepository.save(session1);

        ParkingSession session2 = new ParkingSession(
                "S6",
                "U6",
                "AA555",
                "Z6",
                "SP6",
                TimeOfDayBand.PEAK,
                DayType.WEEKEND,
                ZoneType.VIP,
                LocalDateTime.of(2026, 1, 18, 10, 0)
        );
        parkingSessionRepository.save(session2);

        // First session
        BillingRequest request1 = createRequest(
                "S5",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0)
        );

        billingController.calculateBill(request1);

        verify(billingService).calculateBill(
                eq(LocalDateTime.of(2026, 1, 15, 9, 0)),
                any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()
        );
        verify(subscriptionPlanRepository).getPlanForUser("U5");//User id in session

        // Reset interaction history; re-stub billingService and subscriptionPlan if needed
        reset(billingService, subscriptionPlanRepository);
        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(new BillingResult(
                        new BigDecimal("30.00"),
                        new BigDecimal("0.00"),
                        new BigDecimal("0.00"),
                        new BigDecimal("30.00"),
                        new BigDecimal("6.00"),
                        new BigDecimal("36.00")
                ));
        when(subscriptionPlanRepository.getPlanForUser(anyString())).thenReturn(Optional.of(mockPlan));

        // Second session
        BillingRequest request2 = createRequest(
                "S6",
                ZoneType.VIP,
                DayType.WEEKEND,
                TimeOfDayBand.PEAK,
                0.9,
                LocalDateTime.of(2026, 1, 18, 15, 0)
        );

        billingController.calculateBill(request2);

        verify(billingService).calculateBill(
                eq(LocalDateTime.of(2026, 1, 18, 10, 0)),
                any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()
        );
        verify(subscriptionPlanRepository).getPlanForUser("U6");
    }

    // IT-06: Session properties preserved
    @Test
    @DisplayName("IT-06: Should preserve all session properties during billing")
    void testCalculateBillSessionPropertiesAllPreserved() {
        ParkingSession session = new ParkingSession(
                "S7",
                "U7",
                "AA666",
                "Z7",
                "SP7",
                TimeOfDayBand.PEAK,
                DayType.HOLIDAY,
                ZoneType.VIP,
                LocalDateTime.of(2026, 12, 25, 10, 30)
        );
        parkingSessionRepository.save(session);

        BillingRequest request = createRequest(
                "S7",
                ZoneType.VIP,
                DayType.HOLIDAY,
                TimeOfDayBand.PEAK,
                0.7,
                LocalDateTime.of(2026, 12, 25, 15, 30)
        );

        billingController.calculateBill(request);

        ParkingSession retrievedSession = parkingSessionRepository.findById("S7").orElseThrow();
        assertAll("Verify session properties preserved",
                () -> assertEquals("S7", retrievedSession.getId()),
                () -> assertEquals("U7", retrievedSession.getUserId()),
                () -> assertEquals("AA666", retrievedSession.getVehiclePlate()),
                () -> assertEquals("Z7", retrievedSession.getZoneId()),
                () -> assertEquals("SP7", retrievedSession.getSpotId()),
                () -> assertEquals(TimeOfDayBand.PEAK, retrievedSession.getTimeOfDayBand()),
                () -> assertEquals(DayType.HOLIDAY, retrievedSession.getDayType()),
                () -> assertEquals(ZoneType.VIP, retrievedSession.getZoneType()),
                () -> assertEquals(LocalDateTime.of(2026, 12, 25, 10, 30), retrievedSession.getStartTime()),
                () -> assertEquals(SessionState.PAID, retrievedSession.getState())
        );
    }

    // IT-07: BillingResponse mapping from BillingResult
    @Test
    @DisplayName("IT-07: Should map BillingResult to BillingResponse correctly")
    void testCalculateBillReturnsMappedResponse() {
        ParkingSession session = new ParkingSession(
                "S8",
                "U8",
                "AA777",
                "Z8",
                "SP8",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 16, 9, 0)
        );
        parkingSessionRepository.save(session);

        BillingResult customResult = new BillingResult(
                new BigDecimal("10.00"), // basePrice
                new BigDecimal("2.00"),  // discountsTotal
                new BigDecimal("1.50"),  // penaltiesTotal
                new BigDecimal("11.50"), // netPrice
                new BigDecimal("2.30"),  // taxAmount
                new BigDecimal("13.80")  // finalPrice
        );
        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(customResult);

        BillingRequest request = createRequest(
                "S8",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 16, 13, 0)
        );

        BillingResponse response = billingController.calculateBill(request);

        assertAll("BillingResponse should reflect BillingResult",
                () -> assertEquals(new BigDecimal("10.00"), response.basePrice()),
                () -> assertEquals(new BigDecimal("2.00"), response.discountsTotal()),
                () -> assertEquals(new BigDecimal("1.50"), response.penaltiesTotal()),
                () -> assertEquals(new BigDecimal("11.50"), response.netPrice()),
                () -> assertEquals(new BigDecimal("2.30"), response.taxAmount()),
                () -> assertEquals(new BigDecimal("13.80"), response.finalPrice())
        );
    }

    // IT-08: BillingRecord is saved correctly after billing
    @Test
    @DisplayName("IT-08: Should save billing record with correct data after billing")
    void testBillingRecordSavedWithCorrectData() {
        ParkingSession session = new ParkingSession(
                "S9",
                "U9",
                "AA888",
                "Z9",
                "SP9",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 17, 9, 0)
        );
        parkingSessionRepository.save(session);

        BillingResult result = new BillingResult(
                new BigDecimal("15.00"),
                new BigDecimal("0.00"),
                new BigDecimal("3.00"),
                new BigDecimal("18.00"),
                new BigDecimal("3.60"),
                new BigDecimal("21.60")
        );
        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(result);

        BillingRequest request = createRequest(
                "S9",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 17, 13, 0)
        );

        billingController.calculateBill(request);

        ArgumentCaptor<BillingRecord> recordCaptor = ArgumentCaptor.forClass(BillingRecord.class);
        verify(billingRecordRepository).save(recordCaptor.capture());

        BillingRecord record = recordCaptor.getValue();
        assertAll("BillingRecord fields",
                () -> assertEquals("S9", record.getSessionId()),
                () -> assertEquals("U9", record.getUserId()),
                () -> assertEquals(ZoneType.STANDARD, record.getZoneType()),
                () -> assertEquals(new BigDecimal("21.60"), record.getBillingResult().getFinalPrice())
        );
    }

    // IT-09: Already PAID session should not be billed again
    @Test
    @DisplayName("IT-09: Should not bill an already PAID session")
    void testAlreadyPaidSessionNotRebilled() {
        ParkingSession session = new ParkingSession(
                "S10",
                "U10",
                "AA999",
                "Z10",
                "SP10",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 18, 9, 0)
        );
        session.setState(SessionState.PAID);
        parkingSessionRepository.save(session);

        BillingRequest request = createRequest(
                "S10",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 18, 13, 0)
        );

        assertThrows(IllegalStateException.class, () -> billingController.calculateBill(request));
        verifyNoInteractions(billingService);
        verifyNoInteractions(billingRecordRepository);
    }

    // IT-10: Billing service failure should not commit changes
    @Test
    @DisplayName("IT-10: Should not mark session as paid or save record when billing fails")
    void testBillingServiceFailureDoesNotCommit() {
        ParkingSession session = new ParkingSession(
                "S11",
                "U11",
                "BB111",
                "Z11",
                "SP11",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 19, 9, 0)
        );
        parkingSessionRepository.save(session);

        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()))
                .thenThrow(new RuntimeException("Billing engine failure"));

        BillingRequest request = createRequest(
                "S11",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 19, 13, 0)
        );

        assertThrows(RuntimeException.class, () -> billingController.calculateBill(request));

        verifyNoInteractions(billingRecordRepository);
        ParkingSession persisted = parkingSessionRepository.findById("S11").orElseThrow();
        assertNotEquals(SessionState.PAID, persisted.getState());
    }

    // IT-11: End time before start time should be rejected
    @Test
    @DisplayName("IT-11: Should throw when end time is before session start time")
    void testEndTimeBeforeStartTimeRejected() {
        ParkingSession session = new ParkingSession(
                "S12",
                "U12",
                "BB222",
                "Z12",
                "SP12",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.of(2026, 1, 20, 13, 0) // start later
        );
        parkingSessionRepository.save(session);

        BillingRequest request = createRequest(
                "S12",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 20, 9, 0) // end earlier
        );

        assertThrows(IllegalArgumentException.class, () -> billingController.calculateBill(request));
        verifyNoInteractions(billingService);
    }
}
