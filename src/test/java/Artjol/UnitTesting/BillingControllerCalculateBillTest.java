package Artjol.UnitTesting;

import Controller.BillingController;
import Dto.Billing.BillingRequest;
import Dto.Billing.BillingResponse;
import Enum.DayType;
import Enum.SessionState;
import Enum.TimeOfDayBand;
import Enum.ZoneType;
import Model.*;
import Repository.*;
import Service.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Unit Tests for M-102: BillingController.calculateBill

class BillingControllerCalculateBillTest {

    private BillingController controller;

    @Mock private BillingService mockBillingService;
    @Mock private TariffRepository mockTariffRepo;
    @Mock private DynamicPricingConfigRepository mockConfigRepo;
    @Mock private BillingRecordRepository mockBillingRecordRepo;
    @Mock private ParkingSessionRepository mockSessionRepo;
    @Mock private PenaltyHistoryRepository mockPenaltyRepo;
    @Mock private SubscriptionPlanRepository mockPlanRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new BillingController(
                mockBillingService,
                mockTariffRepo,
                mockConfigRepo,
                mockBillingRecordRepo,
                mockSessionRepo,
                mockPenaltyRepo,
                mockPlanRepo
        );
    }

    private BillingRequest createRequest(LocalDateTime exitTime) {
        return new BillingRequest(
                "session-1",
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                exitTime,
                new BigDecimal("0.00"),
                24
        );
    }

    @Test
    @DisplayName("calculates bill successfully and maps BillingResult to BillingResponse")
    void testCalculateBill_Success() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 20, 14, 0);
        LocalDateTime start = now.minusHours(2);

        BillingRequest request = createRequest(now);

        ParkingSession mockSession = mock(ParkingSession.class);
        when(mockSession.getUserId()).thenReturn("user-1");
        when(mockSession.getStartTime()).thenReturn(start);
        when(mockSession.getState()).thenReturn(null); // not PAID

        Tariff mockTariff = mock(Tariff.class);
        DynamicPricingConfig mockConfig = mock(DynamicPricingConfig.class);
        DiscountInfo discountInfo = mock(DiscountInfo.class);

        SubscriptionPlan plan = new SubscriptionPlan(
                1, 1, 10, 8.0,
                false, false, false,
                discountInfo
        );

        BillingResult mockResult = new BillingResult(
                new BigDecimal("10.00"), // basePrice
                new BigDecimal("0.00"),  // discountsTotal
                new BigDecimal("0.00"),  // penaltiesTotal
                new BigDecimal("10.00"), // netPrice
                new BigDecimal("2.00"),  // taxAmount
                new BigDecimal("12.00")  // finalPrice
        );

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

        // Basic checks
        assertNotNull(response);
        assertEquals("session-1", response.sessionId());
        assertEquals("user-1", response.userId());

        // Mapping checks
        assertAll("BillingResponse should map from BillingResult",
                () -> assertEquals(new BigDecimal("10.00"), response.basePrice()),
                () -> assertEquals(new BigDecimal("0.00"), response.discountsTotal()),
                () -> assertEquals(new BigDecimal("0.00"), response.penaltiesTotal()),
                () -> assertEquals(new BigDecimal("10.00"), response.netPrice()),
                () -> assertEquals(new BigDecimal("2.00"), response.taxAmount()),
                () -> assertEquals(new BigDecimal("12.00"), response.finalPrice())
        );

        // Side-effects
        verify(mockSession).markPaid();
        verify(mockBillingRecordRepo).save(any(BillingRecord.class));
    }

    @Test
    @DisplayName("throws NullPointerException for null request")
    void testCalculateBill_NullRequest() {
        assertThrows(NullPointerException.class, () -> controller.calculateBill(null));
    }

    @Test
    @DisplayName("throws NoSuchElementException when session not found")
    void testCalculateBill_SessionNotFound() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 20, 14, 0);
        BillingRequest request = createRequest(now);

        when(mockSessionRepo.findById("session-1")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> controller.calculateBill(request));

        verify(mockSessionRepo).findById("session-1");
        verifyNoInteractions(mockBillingService);
        verifyNoInteractions(mockBillingRecordRepo);
    }

    @Test
    @DisplayName("throws IllegalStateException when session is already PAID")
    void testCalculateBill_AlreadyPaidSession() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 20, 14, 0);
        LocalDateTime start = now.minusHours(1);

        BillingRequest request = createRequest(now);

        ParkingSession mockSession = mock(ParkingSession.class);
        when(mockSession.getUserId()).thenReturn("user-1");
        when(mockSession.getStartTime()).thenReturn(start);
        when(mockSession.getState()).thenReturn(SessionState.PAID);

        when(mockSessionRepo.findById("session-1"))
                .thenReturn(Optional.of(mockSession));

        assertThrows(IllegalStateException.class, () -> controller.calculateBill(request));

        verify(mockSessionRepo).findById("session-1");
        verifyNoInteractions(mockBillingService);
        verifyNoInteractions(mockBillingRecordRepo);
    }

    @Test
    @DisplayName("throws IllegalArgumentException when exit time is before start time")
    void testCalculateBill_EndTimeBeforeStartTime() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 20, 14, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 20, 13, 0); // before start

        BillingRequest request = createRequest(end);

        ParkingSession mockSession = mock(ParkingSession.class);
        when(mockSession.getUserId()).thenReturn("user-1");
        when(mockSession.getStartTime()).thenReturn(start);
        when(mockSession.getState()).thenReturn(null); // not PAID

        when(mockSessionRepo.findById("session-1"))
                .thenReturn(Optional.of(mockSession));

        assertThrows(IllegalArgumentException.class, () -> controller.calculateBill(request));

        verifyNoInteractions(mockBillingService);
        verifyNoInteractions(mockBillingRecordRepo);
    }

    @Test
    @DisplayName("throws NoSuchElementException when subscription plan not found")
    void testCalculateBill_NoSubscriptionPlan() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 20, 14, 0);
        LocalDateTime start = now.minusHours(1);

        BillingRequest request = createRequest(now);

        ParkingSession mockSession = mock(ParkingSession.class);
        when(mockSession.getUserId()).thenReturn("user-1");
        when(mockSession.getStartTime()).thenReturn(start);
        when(mockSession.getState()).thenReturn(null);

        Tariff mockTariff = mock(Tariff.class);
        DynamicPricingConfig mockConfig = mock(DynamicPricingConfig.class);

        when(mockSessionRepo.findById("session-1"))
                .thenReturn(Optional.of(mockSession));
        when(mockTariffRepo.findByZoneType(ZoneType.STANDARD))
                .thenReturn(mockTariff);
        when(mockConfigRepo.getActiveConfig())
                .thenReturn(mockConfig);
        when(mockPenaltyRepo.findById("user-1"))
                .thenReturn(null);
        when(mockPlanRepo.getPlanForUser("user-1"))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> controller.calculateBill(request));

        verifyNoInteractions(mockBillingService);
        verifyNoInteractions(mockBillingRecordRepo);
    }
}
