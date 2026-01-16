package Controller;

import Dto.Billing.BillingRequest;
import Dto.Billing.BillingResponse;
import Model.*;
import Repository.*;
import Service.BillingService;
import Settings.Settings;

import java.math.BigDecimal;
import java.util.Objects;

public class BillingController {

    private final BillingService billingService;
    private final TariffRepository tariffRepository;
    private final DynamicPricingConfigRepository dynamicPricingConfigRepository;
    private final DiscountPolicyRepository discountPolicyRepository;
    private final BillingRecordRepository billingRecordRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final PenaltyHistoryRepository penaltyHistoryRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public BillingController(BillingService billingService,
                             TariffRepository tariffRepository,
                             DynamicPricingConfigRepository dynamicPricingConfigRepository,
                             DiscountPolicyRepository discountPolicyRepository,
                             BillingRecordRepository billingRecordRepository, ParkingSessionRepository parkingSessionRepository, PenaltyHistoryRepository penaltyHistoryRepository, SubscriptionPlanRepository subscriptionPlanRepository) {
        this.billingService = Objects.requireNonNull(billingService);
        this.tariffRepository = Objects.requireNonNull(tariffRepository);
        this.dynamicPricingConfigRepository = Objects.requireNonNull(dynamicPricingConfigRepository);
        this.discountPolicyRepository = Objects.requireNonNull(discountPolicyRepository);
        this.billingRecordRepository = Objects.requireNonNull(billingRecordRepository);
        this.parkingSessionRepository = parkingSessionRepository;
        this.penaltyHistoryRepository = penaltyHistoryRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    public BillingResponse calculateBill(BillingRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        ParkingSession session = parkingSessionRepository
                .findById(request.sessionId())
                .orElseThrow();

        Tariff tariff = tariffRepository.findByZoneType(request.zoneType());
        DynamicPricingConfig dynamicConfig = dynamicPricingConfigRepository.getActiveConfig();
        DiscountInfo discountInfo = discountPolicyRepository.findDiscountForUser(session.getUserId());

        PenaltyHistory penaltyHistory = penaltyHistoryRepository.findById(session.getUserId());
        BigDecimal penaltiesTotal = penaltyHistory != null
                ? penaltyHistory.getTotalPenaltyAmount()
                : BigDecimal.ZERO;

        // Subscription plan – we only use it to derive parameters, not to recalc the bill.
        SubscriptionPlan plan = subscriptionPlanRepository.getPlanForUser(session.getUserId());

        int effectiveMaxDurationHours = request.maxDurationHours();
        if (plan != null && plan.maxDailyHours > 0 && effectiveMaxDurationHours <= 0) {
            effectiveMaxDurationHours = (int) Math.round(plan.maxDailyHours);
        }

        // Avoid passing null into the service – use a safe default (even if currently ignored)
        BigDecimal maxPriceCap = Settings.MAX_PRICE_CAPACITY;

        BigDecimal taxRate = Settings.TAX_RATIO;

        BillingResult result = billingService.calculateBill(
                session.getStartTime(),
                request.exitTime(),
                request.zoneType(),
                request.dayType(),
                request.timeOfDayBand(),
                request.occupancyRatio(),
                tariff,
                dynamicConfig,
                discountInfo,
                penaltiesTotal,
                effectiveMaxDurationHours,
                maxPriceCap,
                taxRate
        );

        BillingRecord record = new BillingRecord(
                request.sessionId(),
                session.getUserId(),
                request.zoneType(),
                session.getStartTime(),
                request.exitTime(),
                result
        );
        billingRecordRepository.save(record);

        session.markPaid();

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
