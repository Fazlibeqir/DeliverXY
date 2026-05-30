package com.deliverXY.backend.NewCode.deliveries.service;

import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.deliveries.domain.DeliveryHistory;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryHistoryRepository;
import com.deliverXY.backend.NewCode.earnings.domain.DriverEarnings;
import com.deliverXY.backend.NewCode.earnings.repository.DriverEarningsRepository;
import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.repository.PaymentRepository;
import com.deliverXY.backend.NewCode.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class DeliverySettlementService {

    private static final String DEFAULT_CITY = "Skopje";

    private final PaymentRepository paymentRepo;
    private final WalletService walletService;
    private final DriverEarningsRepository earningsRepo;
    private final DeliveryHistoryRepository historyRepo;
    private final PricingConfigService pricingConfigService;

    @Transactional
    public void settleDeliveryEarnings(Delivery d) {
        Payment payment = paymentRepo.findByDeliveryId(d.getId())
                .orElseThrow(() -> new BadRequestException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BadRequestException("Payment not completed");
        }

        if (Boolean.TRUE.equals(payment.getEscrowReleased())) {
            return;
        }

        BigDecimal total = payment.getAmount();

        var pricingConfig = pricingConfigService.getActivePricing(DEFAULT_CITY);
        double platformPct = pricingConfig.getPlatformCommissionPercent() != null
                ? pricingConfig.getPlatformCommissionPercent() / 100.0
                : 0.20;
        double driverPct = 1.0 - platformPct;
        BigDecimal driverCut = total
                .multiply(BigDecimal.valueOf(driverPct))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal platformCut = total.subtract(driverCut);

        walletService.deposit(
                d.getAgent().getId(),
                driverCut,
                "DELIVERY_EARNINGS_" + d.getTrackingCode()
        );

        DriverEarnings earnings = new DriverEarnings();
        earnings.setDelivery(d);
        earnings.setAgentId(d.getAgent().getId());
        earnings.setDriverEarnings(driverCut);
        earnings.setTip(BigDecimal.ZERO);
        earningsRepo.save(earnings);

        payment.setEscrowReleased(true);
        payment.setDriverAmount(driverCut);
        payment.setPlatformFee(platformCut);
        paymentRepo.save(payment);

        DeliveryHistory h = new DeliveryHistory();
        h.setDelivery(d);
        h.setStatus(d.getStatus());
        h.setChangedBy("SYSTEM");
        h.setNote("Delivery completed. Driver earned " + driverCut);
        historyRepo.save(h);
    }
}
