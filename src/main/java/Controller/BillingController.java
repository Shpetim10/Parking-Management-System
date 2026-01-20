package Controller;

import Dto.Billing.BillingRequest;
import Dto.Billing.BillingResponse;
import Enum.SessionState;
import Model.*;
import Repository.*;
import Service.BillingService;
import Settings.Settings;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Objects;

public class BillingController {

    private final BillingService billingService;
    private final TariffRepository tariffRepository;
    private final DynamicPricingConfigRepository dynamicPricingConfigRepository;
    private final BillingRecordRepository billingRecordRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final PenaltyHistoryRepository penaltyHistoryRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public BillingController(BillingService billingService,
                             TariffRepository tariffRepository,
                             DynamicPricingConfigRepository dynamicPricingConfigRepository,
                             BillingRecordRepository billingRecordRepository,
                             ParkingSessionRepository parkingSessionRepository,
                             PenaltyHistoryRepository penaltyHistoryRepository,
                             SubscriptionPlanRepository subscriptionPlanRepository) {

        this.billingService = Objects.requireNonNull(billingService);
        this.tariffRepository = Objects.requireNonNull(tariffRepository);
        this.dynamicPricingConfigRepository = Objects.requireNonNull(dynamicPricingConfigRepository);
        this.billingRecordRepository = Objects.requireNonNull(billingRecordRepository);
        this.parkingSessionRepository = Objects.requireNonNull(parkingSessionRepository);
        this.penaltyHistoryRepository = Objects.requireNonNull(penaltyHistoryRepository);
        this.subscriptionPlanRepository = Objects.requireNonNull(subscriptionPlanRepository);
    }

    public BillingResponse calculateBill(BillingRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        // 1. Load session or fail if it doesn't exist
        ParkingSession session = parkingSessionRepository
                .findById(request.sessionId())
                .orElseThrow(() -> new NoSuchElementException("Session not found: " + request.sessionId()));

        // 2. Reject already PAID sessions → avoids double billing
        if (session.getState() == SessionState.PAID) {
            throw new IllegalStateException("Session already paid: " + session.getId());
        }

        // 3. Validate that exit time is not before entry time
        if (request.exitTime().isBefore(session.getStartTime())) {
            throw new IllegalArgumentException(
                    "Exit time " + request.exitTime() + " cannot be before start time " + session.getStartTime()
            );
        }

        // 4. Load tariff and dynamic pricing config
        Tariff tariff = tariffRepository.findByZoneType(request.zoneType());
        DynamicPricingConfig dynamicConfig = dynamicPricingConfigRepository.getActiveConfig();

        // 5. Penalties from history (fallback to zero if none)
        PenaltyHistory penaltyHistory = penaltyHistoryRepository.findById(session.getUserId());
        BigDecimal penaltiesTotal = penaltyHistory != null
                ? penaltyHistory.getTotalPenaltyAmount()
                : BigDecimal.ZERO;

        // 6. Subscription plan – used to derive parameters like max duration & discounts
        SubscriptionPlan plan = subscriptionPlanRepository
                .getPlanForUser(session.getUserId())
                .orElseThrow(() ->
                        new NoSuchElementException("Subscription plan not found for user: " + session.getUserId())
                );

        int effectiveMaxDurationHours = request.maxDurationHours();
        if (plan.maxDailyHours > 0 && effectiveMaxDurationHours <= 0) {
            effectiveMaxDurationHours = (int) Math.round(plan.maxDailyHours);
        }

        // 7. Other parameters (tax, price cap)
        BigDecimal maxPriceCap = Settings.MAX_PRICE_CAPACITY;
        BigDecimal taxRate = Settings.TAX_RATIO;

        // 8. Delegate to billing service
        BillingResult result = billingService.calculateBill(
                session.getStartTime(),
                request.exitTime(),
                request.zoneType(),
                request.dayType(),
                request.timeOfDayBand(),
                request.occupancyRatio(),
                tariff,
                dynamicConfig,
                plan.discountInfo,
                penaltiesTotal,
                effectiveMaxDurationHours,
                maxPriceCap,
                taxRate
        );

        // 9. Persist billing record
        BillingRecord record = new BillingRecord(
                request.sessionId(),
                session.getUserId(),
                request.zoneType(),
                session.getStartTime(),
                request.exitTime(),
                result
        );
        billingRecordRepository.save(record);

        // 10. Mark session as PAID
        session.markPaid();

        // 11. Map to BillingResponse (record)
        return new BillingResponse(
                request.sessionId(),
                session.getUserId(),
                result.getBasePrice(),
                result.getDiscountsTotal(),
                result.getPenaltiesTotal(),
                result.getNetPrice(),
                result.getTaxAmount(),
                result.getFinalPrice()
        );
    }
}
