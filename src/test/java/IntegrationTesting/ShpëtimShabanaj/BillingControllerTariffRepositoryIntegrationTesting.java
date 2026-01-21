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
@DisplayName("Pairwise Integration: BillingController -- TariffRepository")
@MockitoSettings(strictness = Strictness.LENIENT)
class BillingControllerTariffRepositoryIntegrationTesting {

    private BillingController billingController;

    // Real TariffRepository
    private TariffRepository tariffRepository;

    // Other dependencies as mocks
    @Mock private BillingService billingService;
    @Mock private DynamicPricingConfigRepository dynamicPricingConfigRepository;
    @Mock private BillingRecordRepository billingRecordRepository;
    @Mock private ParkingSessionRepository parkingSessionRepository;
    @Mock private PenaltyHistoryRepository penaltyHistoryRepository;
    @Mock private SubscriptionPlanRepository subscriptionPlanRepository;

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

        // Setup mock objects for other dependencies
        mockSession = mock(ParkingSession.class);
        when(mockSession.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 15, 9, 0));
        when(mockSession.getUserId()).thenReturn("U1");

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

        // Default billing result
        BillingResult mockResult = new BillingResult(
                new BigDecimal("20.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("20.00"),
                new BigDecimal("4.00"),
                new BigDecimal("24.00")
        );
        when(billingService.calculateBill(any(), any(), any(), any(), any(), anyDouble(),
                any(Tariff.class), any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(mockResult);

        // Create controller with real TariffRepository and mocked dependencies
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
            TimeOfDayBand band,
            double occupancy,
            LocalDateTime endTime,
            BigDecimal penalties,
            int maxDurationHours
    ) {
        return new BillingRequest(
                sessionId,
                zoneType,
                dayType,
                band,
                occupancy,
                endTime,
                penalties,
                maxDurationHours
        );
    }

    // TIT-01: Standard zone tariff retrieval
    @Test
    @DisplayName("TIT-01: Should retrieve STANDARD tariff from repository for STANDARD zone")
    void testCalculateBillStandardZoneStandardTariffRetrieved() {
        BillingRequest request = createRequest(
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
    void testCalculateBillEVZoneEVTariffRetrieved() {
        BillingRequest request = createRequest(
                "S2",
                ZoneType.EV,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        billingController.calculateBill(request);

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
    void testCalculateBillVIPZoneVIPTariffRetrieved() {
        BillingRequest request = createRequest(
                "S3",
                ZoneType.VIP,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        billingController.calculateBill(request);

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

    // TIT-04: Tariff not found throws exception
    @Test
    @DisplayName("TIT-04: Should throw IllegalArgumentException when tariff not found for zone type")
    void testCalculateBillTariffNotFoundThrowsException() {
        Map<ZoneType, Tariff> incompleteTariffMap = new EnumMap<>(ZoneType.class);
        incompleteTariffMap.put(ZoneType.EV, evTariff);
        incompleteTariffMap.put(ZoneType.VIP, vipTariff);

        TariffRepository incompleteTariffRepository = new InMemoryTariffRepository(incompleteTariffMap);

        BillingController controllerWithIncompleteTariffs = new BillingController(
                billingService,
                incompleteTariffRepository,
                dynamicPricingConfigRepository,
                billingRecordRepository,
                parkingSessionRepository,
                penaltyHistoryRepository,
                subscriptionPlanRepository
        );

        BillingRequest request = createRequest(
                "S4",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controllerWithIncompleteTariffs.calculateBill(request)
        );

        assertEquals("No tariff configured for zoneType: STANDARD", exception.getMessage());
        verifyNoInteractions(billingService);
    }

    // TIT-05: Tariff retrieval does not depend on other request parameters
    @Test
    @DisplayName("TIT-05: Should retrieve correct tariff regardless of other request parameters")
    void testCalculateBillVariousRequestParamsCorrectTariffRetrieved() {
        BillingRequest weekendRequest = createRequest(
                "S5",
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

        BillingRequest holidayRequest = createRequest(
                "S6",
                ZoneType.VIP,
                DayType.HOLIDAY,
                TimeOfDayBand.OFF_PEAK,
                0.3,
                LocalDateTime.of(2026, 12, 25, 15, 0),
                new BigDecimal("10.00"),
                12
        );

        billingController.calculateBill(holidayRequest);

        verify(billingService).calculateBill(
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

    // TIT-06: Tariff repository called exactly once per request
    @Test
    @DisplayName("TIT-06: Should retrieve tariff from repository exactly once per billing request")
    void testCalculateBillRepositoryAccessCalledOnce() {
        TariffRepository spyRepository = spy(tariffRepository);

        BillingController controllerWithSpy = new BillingController(
                billingService,
                spyRepository,
                dynamicPricingConfigRepository,
                billingRecordRepository,
                parkingSessionRepository,
                penaltyHistoryRepository,
                subscriptionPlanRepository
        );

        BillingRequest request = createRequest(
                "S7",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        controllerWithSpy.calculateBill(request);

        verify(spyRepository, times(1)).findByZoneType(ZoneType.STANDARD);
        verify(spyRepository, never()).findByZoneType(ZoneType.EV);
        verify(spyRepository, never()).findByZoneType(ZoneType.VIP);
    }

    // TIT-07: Tariff retrieved before billing service invocation
    @Test
    @DisplayName("TIT-07: Should retrieve tariff from repository before calling billing service")
    void testCalculateBillExecutionOrderTariffBeforeBillingService() {
        TariffRepository spyRepository = spy(tariffRepository);

        BillingController controllerWithSpies = new BillingController(
                billingService,
                spyRepository,
                dynamicPricingConfigRepository,
                billingRecordRepository,
                parkingSessionRepository,
                penaltyHistoryRepository,
                subscriptionPlanRepository
        );

        BillingRequest request = createRequest(
                "S8",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        controllerWithSpies.calculateBill(request);

        var inOrder = inOrder(spyRepository, billingService);
        inOrder.verify(spyRepository).findByZoneType(ZoneType.STANDARD);
        inOrder.verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(Tariff.class), any(), any(), any(), anyInt(), any(), any()
        );
    }

    // TIT-08: Tariff values are correctly preserved
    @Test
    @DisplayName("TIT-08: Should preserve all tariff values when passing to billing service")
    void testCalculateBillTariffValuesAllValuesPreserved() {
        BillingRequest request = createRequest(
                "S9",
                ZoneType.VIP,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                24
        );

        billingController.calculateBill(request);

        ArgumentCaptor<Tariff> tariffCaptor = ArgumentCaptor.forClass(Tariff.class);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                tariffCaptor.capture(),
                any(), any(), any(), anyInt(), any(), any()
        );

        Tariff capturedTariff = tariffCaptor.getValue();

        assertAll("Verify all VIP tariff properties",
                () -> assertEquals(ZoneType.VIP, capturedTariff.getZoneType()),
                () -> assertEquals(0, new BigDecimal("10.00").compareTo(capturedTariff.getBaseHourlyRate())),
                () -> assertEquals(0, new BigDecimal("80.00").compareTo(capturedTariff.getDailyCap())),
                () -> assertEquals(0, new BigDecimal("25.00").compareTo(capturedTariff.getWeekendOrHolidaySurchargePercent()))
        );
    }
}
