package IntegrationTesting.ShpëtimShabanaj;

import Controller.BillingController;
import Dto.Billing.BillingRequest;
import Enum.DayType;
import Enum.TimeOfDayBand;
import Enum.ZoneType;
import Model.*;
import Repository.*;
import Repository.impl.InMemoryDynamicPricingConfigRepository;
import Service.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pairwise Integration: BillingController -- DynamicPricingConfigRepository")
class BillingControllerDynamicPricingConfigRepositoryIntegrationTesting {

    private BillingController billingController;

    // REAL: DynamicPricingConfigRepository
    private DynamicPricingConfigRepository dynamicPricingConfigRepository;

    // STUBS: All other dependencies
    @Mock
    private BillingService billingService;
    @Mock
    private TariffRepository tariffRepository;
    @Mock
    private BillingRecordRepository billingRecordRepository;
    @Mock
    private ParkingSessionRepository parkingSessionRepository;
    @Mock
    private PenaltyHistoryRepository penaltyHistoryRepository;
    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    private ParkingSession mockSession;
    private Tariff mockTariff;
    private SubscriptionPlan mockPlan;

    @BeforeEach
    void setUp() {
        // Setup REAL dynamic pricing config repository with initial config
        DynamicPricingConfig initialConfig = new DynamicPricingConfig(1.5, 0.8, 1.3);
        dynamicPricingConfigRepository = new InMemoryDynamicPricingConfigRepository(initialConfig);

        // Setup mock objects
        mockSession = mock(ParkingSession.class);
        when(mockSession.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 15, 9, 0));
        when(mockSession.getUserId()).thenReturn("user-001");

        mockTariff = new Tariff(
                ZoneType.STANDARD,
                new BigDecimal("5.00"),
                new BigDecimal("50.00"),
                new BigDecimal("20.00")
        );

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

        // Setup mock repository behaviors
        when(parkingSessionRepository.findById(anyString())).thenReturn(Optional.of(mockSession));
        when(tariffRepository.findByZoneType(any())).thenReturn(mockTariff);
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

        // Create controller
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

    // IT-01: Active config retrieved and passed to billing service
    @Test
    @DisplayName("IT-01: Should retrieve active dynamic pricing config and pass to billing service")
    void testCalculateBillActiveConfigRetrievedAndPassed() {
        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                new BigDecimal("0.00"),
                24
        );

        billingController.calculateBill(request);

        verify(billingService, times(1)).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(),
                argThat(config ->
                        config.getPeakHourMultiplier() == 1.5 &&
                                config.getHighOccupancyThreshold() == 0.8 &&
                                config.getHighOccupancyMultiplier() == 1.3
                ),
                any(), any(), anyInt(), any(), any()
        );
    }

    // IT-02: Config with different peak hour multiplier
    @Test
    @DisplayName("IT-02: Should retrieve config with custom peak hour multiplier")
    void testCalculateBillCustomPeakMultiplierPassedCorrectly() {
        DynamicPricingConfig customConfig = new DynamicPricingConfig(2.0, 0.8, 1.3);
        dynamicPricingConfigRepository.save(customConfig);

        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                new BigDecimal("0.00"),
                24
        );

        billingController.calculateBill(request);

        verify(billingService, times(1)).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(),
                argThat(config -> config.getPeakHourMultiplier() == 2.0),
                any(), any(), anyInt(), any(), any()
        );
    }

    // IT-03: Config with different occupancy threshold
    @Test
    @DisplayName("IT-03: Should retrieve config with custom occupancy threshold")
    void testCalculateBillCustomOccupancyThresholdPassedCorrectly() {
        DynamicPricingConfig customConfig = new DynamicPricingConfig(1.5, 0.7, 1.3);
        dynamicPricingConfigRepository.save(customConfig);

        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.75,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                new BigDecimal("0.00"),
                24
        );

        billingController.calculateBill(request);

        verify(billingService, times(1)).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(),
                argThat(config -> config.getHighOccupancyThreshold() == 0.7),
                any(), any(), anyInt(), any(), any()
        );
    }

    // IT-04: Config with different occupancy multiplier
    @Test
    @DisplayName("IT-04: Should retrieve config with custom occupancy multiplier")
    void testCalculateBillCustomOccupancyMultiplierPassedCorrectly() {
        DynamicPricingConfig customConfig = new DynamicPricingConfig(1.5, 0.8, 1.5);
        dynamicPricingConfigRepository.save(customConfig);

        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.85,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                new BigDecimal("0.00"),
                24
        );

        billingController.calculateBill(request);

        verify(billingService, times(1)).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(),
                argThat(config -> config.getHighOccupancyMultiplier() == 1.5),
                any(), any(), anyInt(), any(), any()
        );
    }

    // IT-05: Config with all custom values
    @Test
    @DisplayName("IT-05: Should retrieve config with all custom values")
    void testCalculateBillAllCustomValuesPassedCorrectly() {
        DynamicPricingConfig fullyCustomConfig = new DynamicPricingConfig(1.8, 0.75, 1.4);
        dynamicPricingConfigRepository.save(fullyCustomConfig);

        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.PEAK,
                0.9,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                new BigDecimal("0.00"),
                24
        );

        billingController.calculateBill(request);

        verify(billingService, times(1)).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(),
                argThat(config ->
                        config.getPeakHourMultiplier() == 1.8 &&
                                config.getHighOccupancyThreshold() == 0.75 &&
                                config.getHighOccupancyMultiplier() == 1.4
                ),
                any(), any(), anyInt(), any(), any()
        );
    }

    // IT-06: Config changes reflected in subsequent calls
    @Test
    @DisplayName("IT-06: Should retrieve updated config for subsequent billing requests")
    void testCalculateBillConfigUpdatedNewConfigUsed() {
        BillingRequest request1 = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                new BigDecimal("0.00"),
                24
        );

        billingController.calculateBill(request1);

        verify(billingService, times(1)).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(),
                argThat(config -> config.getPeakHourMultiplier() == 1.5),
                any(), any(), anyInt(), any(), any()
        );

        DynamicPricingConfig updatedConfig = new DynamicPricingConfig(2.5, 0.85, 1.6);
        dynamicPricingConfigRepository.save(updatedConfig);

        reset(billingService);
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

        BillingRequest request2 = new BillingRequest(
                "session-002",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.PEAK,
                0.9,
                LocalDateTime.of(2026, 1, 15, 14, 0),
                new BigDecimal("0.00"),
                24
        );

        billingController.calculateBill(request2);

        verify(billingService, times(1)).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(),
                argThat(config -> config.getPeakHourMultiplier() == 2.5),
                any(), any(), anyInt(), any(), any()
        );
    }

    // IT-07: Repository accessed exactly once per request
    @Test
    @DisplayName("IT-07: Should access config repository exactly once per billing request")
    void testCalculateBillRepositoryAccessCalledOnce() {
        DynamicPricingConfigRepository spyRepository = spy(dynamicPricingConfigRepository);

        BillingController controllerWithSpy = new BillingController(
                billingService,
                tariffRepository,
                spyRepository,
                billingRecordRepository,
                parkingSessionRepository,
                penaltyHistoryRepository,
                subscriptionPlanRepository
        );

        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                new BigDecimal("0.00"),
                24
        );

        controllerWithSpy.calculateBill(request);

        verify(spyRepository, times(1)).getActiveConfig();
    }

    // IT-08: Config retrieved before billing service call
    @Test
    @DisplayName("IT-08: Should retrieve config before calling billing service")
    void testCalculateBillExecutionOrderConfigBeforeBillingService() {
        DynamicPricingConfigRepository spyRepository = spy(dynamicPricingConfigRepository);

        BillingController controllerWithSpies = new BillingController(
                billingService,
                tariffRepository,
                spyRepository,
                billingRecordRepository,
                parkingSessionRepository,
                penaltyHistoryRepository,
                subscriptionPlanRepository
        );

        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                new BigDecimal("0.00"),
                24
        );

        controllerWithSpies.calculateBill(request);

        var inOrder = inOrder(spyRepository, billingService);
        inOrder.verify(spyRepository).getActiveConfig();
        inOrder.verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()
        );
    }

    // IT-09: Config object identity preserved
    @Test
    @DisplayName("IT-09: Should pass the exact config object from repository to billing service")
    void testCalculateBillConfigObjectIdentitySameObjectPassed() {
        DynamicPricingConfig exactConfig = new DynamicPricingConfig(1.5, 0.8, 1.3);
        dynamicPricingConfigRepository.save(exactConfig);

        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                new BigDecimal("0.00"),
                24
        );

        billingController.calculateBill(request);

        verify(billingService, times(1)).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(),
                same(exactConfig),
                any(), any(), anyInt(), any(), any()
        );
    }

    // IT-10: Config independent of request parameters
    @Test
    @DisplayName("IT-10: Should retrieve same config regardless of request parameters")
    void testCalculateBillDifferentRequestsSameConfigRetrieved() {
        DynamicPricingConfig sharedConfig = new DynamicPricingConfig(1.6, 0.75, 1.35);
        dynamicPricingConfigRepository.save(sharedConfig);

        BillingRequest standardRequest = new BillingRequest(
                "session-001", ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, LocalDateTime.of(2026, 1, 15, 13, 0), new BigDecimal("0.00"), 24
        );

        billingController.calculateBill(standardRequest);

        verify(billingService, times(1)).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), same(sharedConfig), any(), any(), anyInt(), any(), any()
        );

        reset(billingService);
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

        BillingRequest weekendRequest = new BillingRequest(
                "session-002", ZoneType.VIP, DayType.WEEKEND, TimeOfDayBand.PEAK,
                0.9, LocalDateTime.of(2026, 1, 18, 14, 0), new BigDecimal("0.00"), 24
        );

        billingController.calculateBill(weekendRequest);

        verify(billingService, times(1)).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), same(sharedConfig), any(), any(), anyInt(), any(), any()
        );
    }

    // IT-11: Config with boundary values
    @Test
    @DisplayName("IT-11: Should handle config with boundary values correctly")
    void testCalculateBill_BoundaryValues_PassedCorrectly() {
        DynamicPricingConfig boundaryConfig = new DynamicPricingConfig(1.0, 0.0, 1.0);
        dynamicPricingConfigRepository.save(boundaryConfig);

        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.0,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                new BigDecimal("0.00"),
                24
        );

        billingController.calculateBill(request);

        verify(billingService, times(1)).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(),
                argThat(config ->
                        config.getPeakHourMultiplier() == 1.0 &&
                                config.getHighOccupancyThreshold() == 0.0 &&
                                config.getHighOccupancyMultiplier() == 1.0
                ),
                any(), any(), anyInt(), any(), any()
        );
    }

    // IT-12: Config with maximum values
    @Test
    @DisplayName("IT-12: Should handle config with high multiplier values")
    void testCalculateBill_HighMultipliers_PassedCorrectly() {
        DynamicPricingConfig highConfig = new DynamicPricingConfig(3.0, 0.95, 2.5);
        dynamicPricingConfigRepository.save(highConfig);

        BillingRequest request = new BillingRequest(
                "session-001",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.PEAK,
                0.98,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                new BigDecimal("0.00"),
                24
        );

        billingController.calculateBill(request);

        verify(billingService, times(1)).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(),
                argThat(config ->
                        config.getPeakHourMultiplier() == 3.0 &&
                                config.getHighOccupancyThreshold() == 0.95 &&
                                config.getHighOccupancyMultiplier() == 2.5
                ),
                any(), any(), anyInt(), any(), any()
        );
    }
}
