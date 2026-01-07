package Controller;

import Dto.Billing.BillingRequest;
import Dto.Billing.BillingResponse;
import Model.*;
import Repository.BillingRecordRepository;
import Repository.DiscountPolicyRepository;
import Repository.DynamicPricingConfigRepository;
import Repository.TariffRepository;
import Service.Billing.BillingService;

import java.math.BigDecimal;
import java.util.Objects;

public class BillingController {

    private final BillingService billingService;
    private final TariffRepository tariffRepository;
    private final DynamicPricingConfigRepository dynamicPricingConfigRepository;
    private final DiscountPolicyRepository discountPolicyRepository;
    private final BillingRecordRepository billingRecordRepository;

    public BillingController(BillingService billingService,
                             TariffRepository tariffRepository,
                             DynamicPricingConfigRepository dynamicPricingConfigRepository,
                             DiscountPolicyRepository discountPolicyRepository,
                             BillingRecordRepository billingRecordRepository) {
        this.billingService = Objects.requireNonNull(billingService);
        this.tariffRepository = Objects.requireNonNull(tariffRepository);
        this.dynamicPricingConfigRepository = Objects.requireNonNull(dynamicPricingConfigRepository);
        this.discountPolicyRepository = Objects.requireNonNull(discountPolicyRepository);
        this.billingRecordRepository = Objects.requireNonNull(billingRecordRepository);
    }

    public BillingResponse calculateBill(BillingRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        Tariff tariff = tariffRepository.findByZoneType(request.zoneType());
        DynamicPricingConfig dynamicConfig = dynamicPricingConfigRepository.getActiveConfig();
        DiscountInfo discountInfo = discountPolicyRepository.findDiscountForUser(request.userId());

        BigDecimal penalties = request.penalties() != null ? request.penalties() : BigDecimal.ZERO;
        BigDecimal maxPriceCap = request.maxPriceCap() != null ? request.maxPriceCap() : null;
        BigDecimal taxRate = request.taxRate() != null ? request.taxRate() : BigDecimal.ZERO;

        BillingResult result = billingService.calculateBill(
                request.entryTime(),
                request.exitTime(),
                request.zoneType(),
                request.dayType(),
                request.timeOfDayBand(),
                request.occupancyRatio(),
                tariff,
                dynamicConfig,
                discountInfo,
                penalties,
                request.maxDurationHours(),
                maxPriceCap,
                taxRate
        );

        BillingRecord record = new BillingRecord(
                request.sessionId(),
                request.userId(),
                request.zoneType(),
                request.entryTime(),
                request.exitTime(),
                result
        );
        billingRecordRepository.save(record);

        return new BillingResponse(
                request.sessionId(),
                request.userId(),
                result.getBasePrice(),
                result.getDiscountsTotal(),
                result.getPenaltiesTotal(),
                result.getNetPrice(),
                result.getTaxAmount(),
                result.getFinalPrice()
        );
    }
}
