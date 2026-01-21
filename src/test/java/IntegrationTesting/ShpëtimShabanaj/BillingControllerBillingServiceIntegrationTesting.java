package IntegrationTesting.ShpëtimShabanaj;

import Controller.BillingController;
import Dto.Billing.BillingRequest;
import Dto.Billing.BillingResponse;
import Enum.DayType;
import Enum.TimeOfDayBand;
import Enum.ZoneType;
import Model.*;
import Repository.*;
import Service.BillingService;
import Service.impl.DefaultBillingService;
import Service.impl.DefaultDiscountAndCapService;
import Service.impl.DefaultDurationCalculator;
import Service.impl.DefaultPricingService;
import Service.impl.DefaultTaxService;
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
@DisplayName("Pairwise Integration: BillingController -- BillingService")
@MockitoSettings(strictness = Strictness.LENIENT)
class BillingControllerBillingServiceIntegrationTesting {

    private BillingController billingController;

    // REAL: BillingService with all its dependencies
    private BillingService billingService;

    // STUBS: All repositories
    @Mock
    private TariffRepository tariffRepository;
    @Mock
    private DynamicPricingConfigRepository dynamicPricingConfigRepository;
    @Mock
    private BillingRecordRepository billingRecordRepository;
    @Mock
    private ParkingSessionRepository parkingSessionRepository;
    @Mock
    private PenaltyHistoryRepository penaltyHistoryRepository;
    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    private ParkingSession mockSession;
    private Tariff standardTariff;
    private Tariff evTariff;
    private Tariff vipTariff;
    private DynamicPricingConfig dynamicConfig;
    private SubscriptionPlan mockPlan;

    @BeforeEach
    void setUp() {
        // REAL billing service with all dependencies
        billingService = new DefaultBillingService(
                new DefaultDurationCalculator(),
                new DefaultPricingService(),
                new DefaultDiscountAndCapService(),
                new DefaultTaxService()
        );

        // Tariffs
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

        // Dynamic pricing config
        dynamicConfig = new DynamicPricingConfig(1.5, 0.8, 1.3);

        // Session
        mockSession = mock(ParkingSession.class);
        when(mockSession.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 15, 9, 0));
        when(mockSession.getUserId()).thenReturn("U1");

        // Plan
        DiscountInfo mockDiscount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
        mockPlan = new SubscriptionPlan(
                2, 1, 5, 8.0, false, false, false, mockDiscount
        );

        // Repository stubs
        when(parkingSessionRepository.findById(anyString())).thenReturn(Optional.of(mockSession));
        when(tariffRepository.findByZoneType(ZoneType.STANDARD)).thenReturn(standardTariff);
        when(tariffRepository.findByZoneType(ZoneType.EV)).thenReturn(evTariff);
        when(tariffRepository.findByZoneType(ZoneType.VIP)).thenReturn(vipTariff);
        when(dynamicPricingConfigRepository.getActiveConfig()).thenReturn(dynamicConfig);
        when(subscriptionPlanRepository.getPlanForUser(anyString())).thenReturn(Optional.of(mockPlan));
        when(penaltyHistoryRepository.findById(anyString())).thenReturn(null);

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

    // IT-01: Standard weekday parking - complete calculation
    @Test
    @DisplayName("IT-01: Should calculate billing for standard weekday parking correctly")
    void testCalculateBillStandardWeekdayCalculatedCorrectly() {
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 13, 0); // 4 hours
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

        BillingResponse response = billingController.calculateBill(request);

        // 4h * 5.00 = 20.00, tax 4.00, total 24.00
        assertAll("Verify standard weekday billing",
                () -> assertEquals("S1", response.sessionId()),
                () -> assertEquals("U1", response.userId()),
                () -> assertEquals(0, new BigDecimal("20.00").compareTo(response.basePrice())),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(response.discountsTotal())),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(response.penaltiesTotal())),
                () -> assertEquals(0, new BigDecimal("20.00").compareTo(response.netPrice())),
                () -> assertEquals(0, new BigDecimal("4.00").compareTo(response.taxAmount())),
                () -> assertEquals(0, new BigDecimal("24.00").compareTo(response.finalPrice()))
        );
    }

    // IT-02: Peak hour multiplier applied
    @Test
    @DisplayName("IT-02: Should apply peak hour multiplier correctly")
    void testCalculateBillPeakHourMultiplierApplied() {
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 11, 0); // 2 hours
        BillingRequest request = new BillingRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResponse response = billingController.calculateBill(request);

        // 2h * 5.00 * 1.5 = 15.00, tax 3.00, total 18.00
        assertAll("Verify peak hour pricing",
                () -> assertEquals(0, new BigDecimal("15.00").compareTo(response.basePrice())),
                () -> assertEquals(0, new BigDecimal("3.00").compareTo(response.taxAmount())),
                () -> assertEquals(0, new BigDecimal("18.00").compareTo(response.finalPrice()))
        );
    }

    // IT-03: High occupancy multiplier applied
    @Test
    @DisplayName("IT-03: Should apply high occupancy multiplier correctly")
    void testCalculateBillHighOccupancyMultiplierApplied() {
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 11, 0); // 2 hours
        BillingRequest request = new BillingRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.85, // > 0.8
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResponse response = billingController.calculateBill(request);

        // 2h * 5.00 * 1.3 = 13.00, tax 2.60, total 15.60
        assertAll("Verify high occupancy pricing",
                () -> assertEquals(0, new BigDecimal("13.00").compareTo(response.basePrice())),
                () -> assertEquals(0, new BigDecimal("2.60").compareTo(response.taxAmount())),
                () -> assertEquals(0, new BigDecimal("15.60").compareTo(response.finalPrice()))
        );
    }

    // IT-04: Peak and high occupancy combined
    @Test
    @DisplayName("IT-04: Should apply both peak and occupancy multipliers")
    void testCalculateBillPeakAndHighOccupancyBothMultipliersApplied() {
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 11, 0); // 2 hours
        BillingRequest request = new BillingRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.PEAK,
                0.9,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResponse response = billingController.calculateBill(request);

        // 2h * 5.00 * 1.5 * 1.3 = 19.50, tax 3.90, total 23.40
        assertAll("Verify combined multipliers",
                () -> assertEquals(0, new BigDecimal("19.50").compareTo(response.basePrice())),
                () -> assertEquals(0, new BigDecimal("3.90").compareTo(response.taxAmount())),
                () -> assertEquals(0, new BigDecimal("23.40").compareTo(response.finalPrice()))
        );
    }

    // IT-05: Weekend surcharge applied
    @Test
    @DisplayName("IT-05: Should apply weekend surcharge correctly")
    void testCalculateBillWeekendSurchargeApplied() {
        when(mockSession.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 18, 9, 0)); // Sunday

        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 18, 13, 0); // 4 hours
        BillingRequest request = new BillingRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKEND,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResponse response = billingController.calculateBill(request);

        // 4h * 5.00 * 1.20 = 24.00, tax 4.80, total 28.80
        assertAll("Verify weekend surcharge",
                () -> assertEquals(0, new BigDecimal("24.00").compareTo(response.basePrice())),
                () -> assertEquals(0, new BigDecimal("4.80").compareTo(response.taxAmount())),
                () -> assertEquals(0, new BigDecimal("28.80").compareTo(response.finalPrice()))
        );
    }

    // IT-06: Holiday surcharge applied
    @Test
    @DisplayName("IT-06: Should apply holiday surcharge correctly")
    void testCalculateBillHolidaySurchargeApplied() {
        when(mockSession.getStartTime()).thenReturn(LocalDateTime.of(2026, 12, 25, 9, 0)); // Christmas

        LocalDateTime exitTime = LocalDateTime.of(2026, 12, 25, 13, 0); // 4 hours
        BillingRequest request = new BillingRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.HOLIDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResponse response = billingController.calculateBill(request);

        // same weekend/holiday multiplier (20%)
        assertAll("Verify holiday surcharge",
                () -> assertEquals(0, new BigDecimal("24.00").compareTo(response.basePrice())),
                () -> assertEquals(0, new BigDecimal("4.80").compareTo(response.taxAmount())),
                () -> assertEquals(0, new BigDecimal("28.80").compareTo(response.finalPrice()))
        );
    }

    // IT-07: Daily cap applied
    @Test
    @DisplayName("IT-07: Should apply daily cap when price exceeds cap")
    void testCalculateBillExceedsDailyCapCapApplied() {
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 21, 0); // 12 hours
        BillingRequest request = new BillingRequest(
                "S1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.PEAK,
                0.9,
                exitTime,
                BigDecimal.ZERO,
                24
        );

        BillingResponse response = billingController.calculateBill(request);

        // capped at 50.00, tax 10.00, total 60.00
        assertAll("Verify daily cap",
                () -> assertEquals(0, new BigDecimal("50.00").compareTo(response.basePrice())),
                () -> assertEquals(0, new BigDecimal("10.00").compareTo(response.taxAmount())),
                () -> assertEquals(0, new BigDecimal("60.00").compareTo(response.finalPrice()))
        );
    }

    // IT-08: Subscription discount applied
    @Test
    @DisplayName("IT-08: Should apply subscription discount correctly")
    void testCalculateBillSubscriptionDiscountDiscountApplied() {
        DiscountInfo subscriptionDiscount = new DiscountInfo(
                new BigDecimal("0.20"), // 20% subscription discount
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );
        SubscriptionPlan premiumPlan = new SubscriptionPlan(
                3, 2, 10, 12.0, false, true, false, subscriptionDiscount
        );
        when(subscriptionPlanRepository.getPlanForUser("U1")).thenReturn(Optional.of(premiumPlan));

        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 14, 0); // 5 hours
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

        BillingResponse response = billingController.calculateBill(request);

        // Base: 5h * 5.00 = 25.00
        // Discount: 20% = 5.00
        // Net: 20.00, tax 4.00, total 24.00
        assertAll("Verify subscription discount",
                () -> assertEquals(0, new BigDecimal("25.00").compareTo(response.basePrice())),
                () -> assertEquals(0, new BigDecimal("5.00").compareTo(response.discountsTotal())),
                () -> assertEquals(0, new BigDecimal("20.00").compareTo(response.netPrice())),
                () -> assertEquals(0, new BigDecimal("4.00").compareTo(response.taxAmount())),
                () -> assertEquals(0, new BigDecimal("24.00").compareTo(response.finalPrice()))
        );
    }
}
