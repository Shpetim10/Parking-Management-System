package IntegrationTesting.ShpëtimShabanaj;

import Controller.BillingController;
import Dto.Billing.BillingRequest;
import Enum.DayType;
import Enum.TimeOfDayBand;
import Enum.ZoneType;
import Model.BillingResult;
import Model.DiscountInfo;
import Model.DynamicPricingConfig;
import Model.ParkingSession;
import Model.SubscriptionPlan;
import Model.Tariff;
import Repository.*;
import Repository.impl.InMemorySubscriptionPlanRepository;
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
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pairwise Integration: BillingController -- SubscriptionPlanRepository")
@MockitoSettings(strictness = Strictness.LENIENT)
class BillingControllerSubscriptionPlanRepositoryIntegrationTesting {

    private BillingController billingController;

    // Real SubscriptionPlanRepository
    private SubscriptionPlanRepository subscriptionPlanRepository;

    // Use all other dependencies as mocks
    @Mock private BillingService billingService;
    @Mock private TariffRepository tariffRepository;
    @Mock private DynamicPricingConfigRepository dynamicPricingConfigRepository;
    @Mock private BillingRecordRepository billingRecordRepository;
    @Mock private ParkingSessionRepository parkingSessionRepository;
    @Mock private PenaltyHistoryRepository penaltyHistoryRepository;

    private ParkingSession mockSession;
    private Tariff mockTariff;
    private DynamicPricingConfig mockConfig;

    @BeforeEach
    void setUp() {
        // Real subscription repo
        subscriptionPlanRepository = new InMemorySubscriptionPlanRepository();

        // Session stub
        mockSession = mock(ParkingSession.class);
        when(mockSession.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 15, 9, 0));
        when(mockSession.getUserId()).thenReturn("U1");

        // Other domain stubs
        mockTariff = new Tariff(
                ZoneType.STANDARD,
                new BigDecimal("5.00"),
                new BigDecimal("50.00"),
                new BigDecimal("20.00")
        );

        mockConfig = new DynamicPricingConfig(1.5, 0.8, 1.3);

        // Repository behaviors needed for controller to run
        when(parkingSessionRepository.findById(anyString())).thenReturn(Optional.of(mockSession));
        when(tariffRepository.findByZoneType(any())).thenReturn(mockTariff);
        when(dynamicPricingConfigRepository.getActiveConfig()).thenReturn(mockConfig);
        when(penaltyHistoryRepository.findById(anyString())).thenReturn(null);

        // Billing service default result
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

        // Controller under test
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

    private BillingRequest createRequest(String sessionId, int maxDurationHours) {
        return new BillingRequest(
                sessionId,
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                BigDecimal.ZERO,
                maxDurationHours
        );
    }

    // IT-01: Plan with no discounts
    @Test
    @DisplayName("IT-01: Should retrieve plan with zero discounts and pass to billing service")
    void testCalculateBillPlanWithNoDiscountsDiscountInfoPassed() {
        DiscountInfo noDiscount = new DiscountInfo(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        SubscriptionPlan basicPlan = new SubscriptionPlan(
                2, 1, 5, 8.0, false, false, false, noDiscount
        );

        subscriptionPlanRepository.save("U1", basicPlan);

        BillingRequest request = createRequest("S1", 24);

        billingController.calculateBill(request);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(),
                argThat(discountInfo ->
                        discountInfo.getSubscriptionDiscountPercent().compareTo(BigDecimal.ZERO) == 0 &&
                                discountInfo.getPromoDiscountPercent().compareTo(BigDecimal.ZERO) == 0 &&
                                discountInfo.getPromoDiscountFixed().compareTo(BigDecimal.ZERO) == 0
                ),
                any(), anyInt(), any(), any()
        );
    }

    // IT-02: Plan with subscription discount
    @Test
    @DisplayName("IT-02: Should retrieve plan with subscription discount and pass to billing service")
    void testCalculateBillPlanWithSubscriptionDiscountDiscountInfoPassed() {
        DiscountInfo subscriptionDiscount = new DiscountInfo(
                new BigDecimal("0.20"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        SubscriptionPlan premiumPlan = new SubscriptionPlan(
                3, 2, 10, 12.0, false, true, false, subscriptionDiscount
        );

        subscriptionPlanRepository.save("U1", premiumPlan);

        BillingRequest request = createRequest("S1", 24);

        billingController.calculateBill(request);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(),
                argThat(discountInfo ->
                        discountInfo.getSubscriptionDiscountPercent()
                                .compareTo(new BigDecimal("0.20")) == 0
                ),
                any(), anyInt(), any(), any()
        );
    }

    // IT-03: Plan with all discount types
    @Test
    @DisplayName("IT-03: Should retrieve plan with multiple discounts and pass all to billing service")
    void testCalculateBillPlanWithAllDiscountsAllDiscountsPassed() {
        DiscountInfo fullDiscount = new DiscountInfo(
                new BigDecimal("0.15"),
                new BigDecimal("0.10"),
                new BigDecimal("5.00"),
                true,
                2
        );

        SubscriptionPlan vipPlan = new SubscriptionPlan(
                5, 3, 20, 24.0, false, true, true, fullDiscount
        );

        subscriptionPlanRepository.save("U1", vipPlan);

        BillingRequest request = createRequest("S1", 24);

        billingController.calculateBill(request);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(),
                argThat(discountInfo ->
                        discountInfo.getSubscriptionDiscountPercent()
                                .compareTo(new BigDecimal("0.15")) == 0 &&
                                discountInfo.getPromoDiscountPercent()
                                        .compareTo(new BigDecimal("0.10")) == 0 &&
                                discountInfo.getPromoDiscountFixed()
                                        .compareTo(new BigDecimal("5.00")) == 0 &&
                                discountInfo.isSubscriptionHasFreeHours() &&
                                discountInfo.getFreeHoursPerDay() == 2
                ),
                any(), anyInt(), any(), any()
        );
    }

    // IT-04: Plan not found throws exception
    @Test
    @DisplayName("IT-04: Should throw NoSuchElementException when subscription plan not found")
    void testCalculateBillPlanNotFoundThrowsException() {
        BillingRequest request = createRequest("S1", 24);

        assertThrows(NoSuchElementException.class, () -> billingController.calculateBill(request));

        verifyNoInteractions(billingService);
    }

    // IT-05: Max duration from plan when request is zero
    @Test
    @DisplayName("IT-05: Should use plan's max duration when request max duration is zero")
    void testCalculateBillRequestMaxDurationZeroPlanMaxDurationUsed() {
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );

        SubscriptionPlan planWith6Hours = new SubscriptionPlan(
                2, 1, 5, 6.0, false, false, false, discount
        );

        subscriptionPlanRepository.save("U1", planWith6Hours);

        BillingRequest request = createRequest("S1", 0);

        billingController.calculateBill(request);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(),
                eq(6),
                any(), any()
        );
    }

    // IT-06: Max duration from plan when request is negative
    @Test
    @DisplayName("IT-06: Should use plan's max duration when request max duration is negative")
    void testCalculateBillRequestMaxDurationNegativePlanMaxDurationUsed() {
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );

        SubscriptionPlan planWith10Hours = new SubscriptionPlan(
                2, 1, 5, 10.0, false, false, false, discount
        );

        subscriptionPlanRepository.save("U1", planWith10Hours);

        BillingRequest request = createRequest("S1", -5);

        billingController.calculateBill(request);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(),
                eq(10),
                any(), any()
        );
    }

    // IT-07: Request max duration takes precedence when positive
    @Test
    @DisplayName("IT-07: Should use request's max duration when it's positive (overrides plan)")
    void testCalculateBillRequestMaxDurationPositiveRequestUsed() {
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );

        SubscriptionPlan planWith8Hours = new SubscriptionPlan(
                2, 1, 5, 8.0, false, false, false, discount
        );

        subscriptionPlanRepository.save("U1", planWith8Hours);

        BillingRequest request = createRequest("S1", 12);

        billingController.calculateBill(request);

        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(),
                eq(12),
                any(), any()
        );
    }

    // IT-08: Plan max duration rounded correctly
    @Test
    @DisplayName("IT-08: Should round plan's max duration from double to int correctly")
    void testCalculateBillPlanMaxDurationDecimalRoundedCorrectly() {
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );

        SubscriptionPlan planWith7Point5Hours = new SubscriptionPlan(
                2, 1, 5, 7.5, false, false, false, discount
        );

        subscriptionPlanRepository.save("U1", planWith7Point5Hours);

        BillingRequest request = createRequest("S1", 0);

        billingController.calculateBill(request);

        // assuming 7.5 rounds to 8
        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(),
                eq(8),
                any(), any()
        );
    }

    // IT-09: Different users have independent plans
    @Test
    @DisplayName("IT-09: Should retrieve correct plan for different users independently")
    void testCalculateBillMultipleUsersIndependentPlans() {
        // U1 plan
        DiscountInfo basicDiscount = new DiscountInfo(
                new BigDecimal("0.10"), BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan basicPlan = new SubscriptionPlan(
                2, 1, 5, 8.0, false, false, false, basicDiscount
        );
        subscriptionPlanRepository.save("U1", basicPlan);

        // U2 plan
        DiscountInfo premiumDiscount = new DiscountInfo(
                new BigDecimal("0.25"), BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan premiumPlan = new SubscriptionPlan(
                5, 3, 15, 20.0, false, true, true, premiumDiscount
        );
        subscriptionPlanRepository.save("U2", premiumPlan);

        // Two different sessions/users
        ParkingSession sessionUser1 = mock(ParkingSession.class);
        when(sessionUser1.getUserId()).thenReturn("U1");
        when(sessionUser1.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 15, 9, 0));

        ParkingSession sessionUser2 = mock(ParkingSession.class);
        when(sessionUser2.getUserId()).thenReturn("U2");
        when(sessionUser2.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 15, 10, 0));

        when(parkingSessionRepository.findById("S1")).thenReturn(Optional.of(sessionUser1));
        when(parkingSessionRepository.findById("S2")).thenReturn(Optional.of(sessionUser2));

        BillingRequest request1 = createRequest("S1", 24);
        BillingRequest request2 = createRequest("S2", 24);

        billingController.calculateBill(request1);
        billingController.calculateBill(request2);

        // one call with 0.10 and one with 0.25
        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(),
                argThat(discount ->
                        discount.getSubscriptionDiscountPercent()
                                .compareTo(new BigDecimal("0.10")) == 0
                ),
                any(), anyInt(), any(), any()
        );
        verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(),
                argThat(discount ->
                        discount.getSubscriptionDiscountPercent()
                                .compareTo(new BigDecimal("0.25")) == 0
                ),
                any(), anyInt(), any(), any()
        );
    }

    // IT-10: Plan accessed exactly once per request
    @Test
    @DisplayName("IT-10: Should access subscription plan repository exactly once per billing request")
    void testCalculateBillRepositoryAccessCalledOnce() {
        SubscriptionPlanRepository spyRepository = spy(subscriptionPlanRepository);

        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(
                2, 1, 5, 8.0, false, false, false, discount
        );
        spyRepository.save("U1", plan);

        BillingController controllerWithSpy = new BillingController(
                billingService,
                tariffRepository,
                dynamicPricingConfigRepository,
                billingRecordRepository,
                parkingSessionRepository,
                penaltyHistoryRepository,
                spyRepository
        );

        BillingRequest request = createRequest("S1", 24);

        controllerWithSpy.calculateBill(request);

        verify(spyRepository, times(1)).getPlanForUser("U1");
    }

    // IT-11: Plan retrieved before billing service call
    @Test
    @DisplayName("IT-11: Should retrieve subscription plan before calling billing service")
    void testCalculateBillExecutionOrderPlanBeforeBillingService() {
        SubscriptionPlanRepository spyRepository = spy(subscriptionPlanRepository);

        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        SubscriptionPlan plan = new SubscriptionPlan(
                2, 1, 5, 8.0, false, false, false, discount
        );
        spyRepository.save("U1", plan);

        BillingController controllerWithSpies = new BillingController(
                billingService,
                tariffRepository,
                dynamicPricingConfigRepository,
                billingRecordRepository,
                parkingSessionRepository,
                penaltyHistoryRepository,
                spyRepository
        );

        BillingRequest request = createRequest("S1", 24);

        controllerWithSpies.calculateBill(request);

        var inOrder = inOrder(spyRepository, billingService);
        inOrder.verify(spyRepository).getPlanForUser("U1");
        inOrder.verify(billingService).calculateBill(
                any(), any(), any(), any(), any(), anyDouble(),
                any(), any(), any(), any(), anyInt(), any(), any()
        );
    }
}
