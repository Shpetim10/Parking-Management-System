package IntegrationTesting.ShpëtimShabanaj;

import Controller.BillingController;
import Dto.Billing.BillingRequest;
import Enum.DayType;
import Enum.TimeOfDayBand;
import Enum.ZoneType;
import Model.*;
import Repository.*;
import Repository.impl.InMemoryTariffRepository;
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
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pairwise Integration: BillingController ↔ TariffRepository")
@MockitoSettings(strictness = Strictness.LENIENT)
class BillingControllerTariffRepositoryIntegrationTest {

    private BillingController billingController;

    // Real TariffRepository
    private TariffRepository tariffRepository;

    // Use other dependencies as mocks
    @Mock
    private BillingService billingService;
    @Mock
    private DynamicPricingConfigRepository dynamicPricingConfigRepository;
    @Mock
    private DiscountPolicyRepository discountPolicyRepository;
    @Mock
    private BillingRecordRepository billingRecordRepository;
    @Mock
    private ParkingSessionRepository parkingSessionRepository;
    @Mock
    private PenaltyHistoryRepository penaltyHistoryRepository;
    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    private Tariff standardTariff;
    private Tariff evTariff;
    private Tariff vipTariff;
    private ParkingSession mockSession;
    private SubscriptionPlan mockPlan;
    private DynamicPricingConfig mockConfig;

    @BeforeEach
    void setUp() {
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

        // Setup mock objects for other dependencies
        mockSession = mock(ParkingSession.class);
        when(mockSession.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 15, 9, 0));
        when(mockSession.getUserId()).thenReturn("user-001");

        DiscountInfo mockDiscount = new DiscountInfo(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        mockPlan = new SubscriptionPlan(
                2, 1, 5, 8.0, false, false, false, mockDiscount
        );

        mockConfig = new DynamicPricingConfig(1.5, 0.8, 1.3);

        // Setup mock repository behaviors
        when(parkingSessionRepository.findById(anyString())).thenReturn(Optional.of(mockSession));
        when(subscriptionPlanRepository.getPlanForUser(anyString())).thenReturn(Optional.of(mockPlan));
        when(dynamicPricingConfigRepository.getActiveConfig()).thenReturn(mockConfig);
        when(penaltyHistoryRepository.findById(anyString())).thenReturn(null);

        // Default billing result, because i am not testing for this in the moment
        BillingResult mockResult = new BillingResult(
                BigDecimal.valueOf(20.00),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(20.00),
                BigDecimal.valueOf(4.00),
                BigDecimal.valueOf(24.00)
        );
        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(Tariff.class), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Create controller with real TariffRepository and mocked dependencies
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
    }

    // Helper to build a generic BillingRequest
    private BillingRequest createRequest(
            String sessionId,
            ZoneType zoneType,
            DayType dayType,
            TimeOfDayBand band,
            double occupancy,
            LocalDateTime endTime,
            BigDecimal penalties,
            int freeMinutes
    ) {
        return new BillingRequest(
                sessionId,
                zoneType,
                dayType,
                band,
                occupancy,
                endTime,
                penalties,
                freeMinutes
        );
    }

    // TIT-01: Standard zone tariff retrieval
    @Test
    @DisplayName("TIT-01: Should retrieve STANDARD tariff from repository for STANDARD zone")
    void testCalculateBill_StandardZone_StandardTariffRetrieved() {
        // Arrange
        BillingRequest request = createRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        // Act
        billingController.calculateBill(request);

        // Assert - Verify STANDARD tariff was passed to billing service
        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                eq(ZoneType.STANDARD),
                any(),
                any(),
                anyDouble(),
                same(standardTariff),
                any(),
                any(),
                any(),
                anyInt(),
                any(),
                any()
        );
    }

    // TIT-02: EV zone tariff retrieval
    @Test
    @DisplayName("TIT-02: Should retrieve EV tariff from repository for EV zone")
    void testCalculateBill_EVZone_EVTariffRetrieved() {
        // Arrange
        BillingRequest request = createRequest(
                "session-001",
                ZoneType.EV,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        // Act
        billingController.calculateBill(request);

        // Assert - Verify EV tariff was passed to billing service
        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                eq(ZoneType.EV),
                any(),
                any(),
                anyDouble(),
                same(evTariff),
                any(),
                any(),
                any(),
                anyInt(),
                any(),
                any()
        );
    }

    // TIT-03: VIP zone tariff retrieval
    @Test
    @DisplayName("TIT-03: Should retrieve VIP tariff from repository for VIP zone")
    void testCalculateBill_VIPZone_VIPTariffRetrieved() {
        // Arrange
        BillingRequest request = createRequest(
                "session-001",
                ZoneType.VIP,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        // Act
        billingController.calculateBill(request);

        // Assert - Verify VIP tariff was passed to billing service
        verify(billingService, times(1)).calculateBill(
                any(),
                any(),
                eq(ZoneType.VIP),
                any(),
                any(),
                anyDouble(),
                same(vipTariff),
                any(),
                any(),
                any(),
                anyInt(),
                any(),
                any()
        );
    }

    // TIT-04: Tariff retrieved matches request zone type for multiple requests
    @Test
    @DisplayName("TIT-04: Should always retrieve tariff matching the request zone type")
    void testCalculateBill_MultipleRequests_CorrectTariffForEach() {
        // STANDARD
        BillingRequest standardRequest = createRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );
        billingController.calculateBill(standardRequest);

        verify(billingService).calculateBill(
                any(), any(), eq(ZoneType.STANDARD), any(), any(), anyDouble(),
                same(standardTariff), any(), any(), any(), anyInt(), any(), any()
        );

        // EV
        BillingRequest evRequest = createRequest(
                "session-002",
                ZoneType.EV,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 14, 0),
                BigDecimal.ZERO,
                24
        );
        billingController.calculateBill(evRequest);

        verify(billingService).calculateBill(
                any(), any(), eq(ZoneType.EV), any(), any(), anyDouble(),
                same(evTariff), any(), any(), any(), anyInt(), any(), any()
        );

        // VIP
        BillingRequest vipRequest = createRequest(
                "session-003",
                ZoneType.VIP,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 15, 0),
                BigDecimal.ZERO,
                24
        );
        billingController.calculateBill(vipRequest);

        verify(billingService).calculateBill(
                any(), any(), eq(ZoneType.VIP), any(), any(), anyDouble(),
                same(vipTariff), any(), any(), any(), anyInt(), any(), any()
        );
    }

    // TIT-05: Tariff not found throws exception
    @Test
    @DisplayName("TIT-05: Should throw IllegalArgumentException when tariff not found for zone type")
    void testCalculateBill_TariffNotFound_ThrowsException() {
        // Arrange - Create repository with missing STANDARD tariff
        Map<ZoneType, Tariff> incompleteTariffMap = new EnumMap<>(ZoneType.class);
        incompleteTariffMap.put(ZoneType.EV, evTariff);
        incompleteTariffMap.put(ZoneType.VIP, vipTariff);

        TariffRepository incompleteTariffRepository = new InMemoryTariffRepository(incompleteTariffMap);

        BillingController controllerWithIncompleteTariffs = new BillingController(
                billingService,
                incompleteTariffRepository,
                dynamicPricingConfigRepository,
                discountPolicyRepository,
                billingRecordRepository,
                parkingSessionRepository,
                penaltyHistoryRepository,
                subscriptionPlanRepository
        );

        BillingRequest request = createRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controllerWithIncompleteTariffs.calculateBill(request)
        );

        assertEquals("No tariff configured for zoneType: STANDARD", exception.getMessage());
        verifyNoInteractions(billingService);
    }

    // TIT-06: Tariff object identity
    @Test
    @DisplayName("TIT-06: Should pass the exact tariff object from repository to billing service")
    void testCalculateBill_TariffObjectIdentity_SameObjectPassed() {
        // Arrange
        BillingRequest request = createRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        // Act
        billingController.calculateBill(request);

        // Assert - Verify the exact same object reference is passed
        verify(billingService, times(1)).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                same(standardTariff),
                any(), any(), any(), anyInt(), any(), any()
        );
    }

    // TIT-07: Tariff retrieval does not depend on other request parameters
    @Test
    @DisplayName("TIT-07: Should retrieve correct tariff regardless of other request parameters")
    void testCalculateBill_VariousRequestParams_CorrectTariffRetrieved() {
        // EV, WEEKEND, PEAK, high occupancy
        BillingRequest weekendRequest = createRequest(
                "session-001",
                ZoneType.EV,
                DayType.WEEKEND,
                TimeOfDayBand.PEAK,
                0.9,
                LocalDateTime.of(2026, 1, 18, 13, 0),
                BigDecimal.ZERO,
                24
        );

        billingController.calculateBill(weekendRequest);

        verify(billingService).calculateBill(
                any(), any(), eq(ZoneType.EV), any(), any(), anyDouble(),
                same(evTariff), any(), any(), any(), anyInt(), any(), any()
        );

        // VIP, HOLIDAY, OFF_PEAK, low occupancy, with penalties
        BillingRequest holidayRequest = createRequest(
                "session-002",
                ZoneType.VIP,
                DayType.HOLIDAY,
                TimeOfDayBand.OFF_PEAK,
                0.3,
                LocalDateTime.of(2026, 12, 25, 15, 0),
                BigDecimal.valueOf(10.00),
                12
        );

        billingController.calculateBill(holidayRequest);

        verify(billingService).calculateBill(
                any(), any(), eq(ZoneType.VIP), any(), any(), anyDouble(),
                same(vipTariff), any(), any(), any(), anyInt(), any(), any()
        );
    }

    // TIT-08: Tariff repository called exactly once per request
    @Test
    @DisplayName("TIT-08: Should retrieve tariff from repository exactly once per billing request")
    void testCalculateBill_RepositoryAccess_CalledOnce() {
        // Arrange - Spy on the real repository to verify access count
        TariffRepository spyRepository = spy(tariffRepository);

        BillingController controllerWithSpy = new BillingController(
                billingService,
                spyRepository,
                dynamicPricingConfigRepository,
                discountPolicyRepository,
                billingRecordRepository,
                parkingSessionRepository,
                penaltyHistoryRepository,
                subscriptionPlanRepository
        );

        BillingRequest request = createRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        // Act
        controllerWithSpy.calculateBill(request);

        // Assert - Verify repository accessed exactly once
        verify(spyRepository, times(1)).findByZoneType(ZoneType.STANDARD);
        verify(spyRepository, never()).findByZoneType(ZoneType.EV);
        verify(spyRepository, never()).findByZoneType(ZoneType.VIP);
    }

    // TIT-09: Tariff retrieved before billing service invocation
    @Test
    @DisplayName("TIT-09: Should retrieve tariff from repository before calling billing service")
    void testCalculateBill_ExecutionOrder_TariffBeforeBillingService() {
        // Arrange
        TariffRepository spyRepository = spy(tariffRepository);

        BillingController controllerWithSpies = new BillingController(
                billingService, // still a mock
                spyRepository,
                dynamicPricingConfigRepository,
                discountPolicyRepository,
                billingRecordRepository,
                parkingSessionRepository,
                penaltyHistoryRepository,
                subscriptionPlanRepository
        );

        BillingRequest request = createRequest(
                "session-001",
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

        // Assert - Verify order of operations
        var inOrder = inOrder(spyRepository, billingService);
        inOrder.verify(spyRepository).findByZoneType(ZoneType.STANDARD);
        inOrder.verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(Tariff.class), any(), any(), any(), anyInt(), any(), any()
        );
    }

    // TIT-10: Tariff values are correctly preserved
    @Test
    @DisplayName("TIT-10: Should preserve all tariff values when passing to billing service")
    void testCalculateBill_TariffValues_AllValuesPreserved() {
        // Arrange
        BillingRequest request = createRequest(
                "session-001",
                ZoneType.VIP,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        // Act
        billingController.calculateBill(request);

        // Assert - Capture tariff and check fields
        ArgumentCaptor<Tariff> tariffCaptor = ArgumentCaptor.forClass(Tariff.class);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                tariffCaptor.capture(),
                any(), any(), any(), anyInt(), any(), any()
        );

        Tariff capturedTariff = tariffCaptor.getValue();

        assertAll("Verify all VIP tariff properties",
                () -> assertEquals(ZoneType.VIP, capturedTariff.getZoneType()),
                () -> assertEquals(0, BigDecimal.valueOf(10.00).compareTo(capturedTariff.getBaseHourlyRate())),
                () -> assertEquals(0, BigDecimal.valueOf(80.00).compareTo(capturedTariff.getDailyCap())),
                () -> assertEquals(0, BigDecimal.valueOf(25.00).compareTo(capturedTariff.getWeekendOrHolidaySurchargePercent()))
        );
    }
}
