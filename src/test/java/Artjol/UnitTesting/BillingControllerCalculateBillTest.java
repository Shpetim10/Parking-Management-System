package Artjol.UnitTesting;

import Controller.BillingController;
import Dto.Billing.BillingRequest;
import Dto.Billing.BillingResponse;
import Enum.*;
import Model.*;
import Repository.*;
import Service.BillingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

// Unit Tests for M-102: BillingController.calculateBill

class BillingControllerCalculateBillTest {

    private BillingController controller;

    @Mock
    private BillingService mockBillingService;
    @Mock
    private TariffRepository mockTariffRepo;
    @Mock
    private DynamicPricingConfigRepository mockConfigRepo;
    @Mock
    private DiscountPolicyRepository mockDiscountRepo;
    @Mock
    private BillingRecordRepository mockBillingRecordRepo;
    @Mock
    private ParkingSessionRepository mockSessionRepo;
    @Mock
    private PenaltyHistoryRepository mockPenaltyRepo;
    @Mock
    private SubscriptionPlanRepository mockPlanRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new BillingController(
                mockBillingService, mockTariffRepo, mockConfigRepo,
                mockDiscountRepo, mockBillingRecordRepo, mockSessionRepo,
                mockPenaltyRepo, mockPlanRepo
        );
    }

    @Test
    @DisplayName("calculates bill successfully")
    void testCalculateBill_Success() {
        LocalDateTime now = LocalDateTime.now();

        BillingRequest request = new BillingRequest(
                "session-1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                now,
                BigDecimal.ZERO,
                24
        );

        ParkingSession mockSession = mock(ParkingSession.class);
        when(mockSession.getUserId()).thenReturn("user-1");
        when(mockSession.getStartTime()).thenReturn(now.minusHours(2));

        Tariff mockTariff = mock(Tariff.class);
        DynamicPricingConfig mockConfig = mock(DynamicPricingConfig.class);

        DiscountInfo discountInfo = mock(DiscountInfo.class);

        SubscriptionPlan plan = new SubscriptionPlan(
                1, 1, 10, 8.0,
                false, false, false,
                discountInfo
        );

        BillingResult mockResult = mock(BillingResult.class);
        when(mockResult.getBasePrice()).thenReturn(BigDecimal.TEN);
        when(mockResult.getDiscountsTotal()).thenReturn(BigDecimal.ZERO);
        when(mockResult.getPenaltiesTotal()).thenReturn(BigDecimal.ZERO);
        when(mockResult.getNetPrice()).thenReturn(BigDecimal.TEN);
        when(mockResult.getTaxAmount()).thenReturn(new BigDecimal("2.00"));
        when(mockResult.getFinalPrice()).thenReturn(new BigDecimal("12.00"));

        when(mockSessionRepo.findById("session-1"))
                .thenReturn(Optional.of(mockSession));
        when(mockTariffRepo.findByZoneType(ZoneType.STANDARD))
                .thenReturn(mockTariff);
        when(mockConfigRepo.getActiveConfig())
                .thenReturn(mockConfig);
        when(mockPenaltyRepo.findById("user-1"))
                .thenReturn(null);
        when(mockPlanRepo.getPlanForUser("user-1"))
                .thenReturn(Optional.of(plan));

        when(mockBillingService.calculateBill(
                any(), any(), any(), any(), any(),
                anyDouble(), any(), any(), any(),
                any(), anyInt(), any(), any()
        )).thenReturn(mockResult);

        BillingResponse response = controller.calculateBill(request);

        assertNotNull(response);
        assertEquals("session-1", response.sessionId());
        assertEquals("user-1", response.userId());

        verify(mockSession).markPaid();
        verify(mockBillingRecordRepo).save(any(BillingRecord.class));
    }

    @Test
    @DisplayName("throws exception for null request")
    void testCalculateBill_NullRequest() {
        assertThrows(NullPointerException.class, () -> {
            controller.calculateBill(null);
        });
    }

    @Test
    @DisplayName("retrieves session from repository")
    void testCalculateBill_RetrievesSession() {
        LocalDateTime now = LocalDateTime.now();

        BillingRequest request = new BillingRequest(
                "session-1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                now,
                BigDecimal.ZERO,
                24
        );

        when(mockSessionRepo.findById("session-1")).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> {
            controller.calculateBill(request);
        });

        verify(mockSessionRepo).findById("session-1");
    }
}
