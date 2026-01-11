package com.deliverXY.backend.NewCode.payments.service.Provider;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryRepository;
import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.dto.PaymentResultDTO;
import com.deliverXY.backend.NewCode.payments.repository.PaymentRepository;
import com.deliverXY.backend.NewCode.payments.service.PaymentGatewayProvider;
import com.deliverXY.backend.NewCode.payments.service.PaymentService;
import com.deliverXY.backend.NewCode.wallet.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final Map<PaymentProvider, PaymentGatewayProvider> providers;
    private final PaymentRepository paymentRepo;
    private final DeliveryRepository deliveryRepo;
    private final WalletService walletService;

    public PaymentServiceImpl(List<PaymentGatewayProvider> providerList,
                              PaymentRepository paymentRepo,
                              DeliveryRepository deliveryRepo,
                              WalletService walletService) {
        this.paymentRepo = paymentRepo;
        this.deliveryRepo = deliveryRepo;
        this.walletService=walletService;
        this.providers = providerList.stream()
                .collect(Collectors.toMap(PaymentGatewayProvider::getProviderType, Function.identity()));
    }

    private PaymentGatewayProvider getGatewayProvider(PaymentProvider provider) {
        PaymentGatewayProvider gateway = providers.get(provider);
        if (gateway == null) {
            throw new BadRequestException("Payment provider '" + provider + "' is not supported.");
        }
        return gateway;
    }

    @Override
    @Transactional
    public PaymentResultDTO initializePayment(Long deliveryId, BigDecimal amount, PaymentProvider provider, Long userId)
    {

        var delivery = deliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found"));

//        FareResponseDTO fare = deliveryService.getFareForDelivery(deliveryId);
        BigDecimal finalAmount = amount;

        if (delivery.getDeliveryPayment() != null
                && delivery.getDeliveryPayment().getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("A payment already exists for this delivery.");
        }
        Payment payment = Payment.builder()
                .delivery(delivery)
                .payerId(userId)
                .amount(finalAmount)
                .method(provider.getDefaultMethod())
                .provider(provider)
                .status(PaymentStatus.PENDING)
                .initiatedAt(LocalDateTime.now())
                .tip(BigDecimal.ZERO)
                .platformFee(BigDecimal.ZERO)
                .driverAmount(BigDecimal.ZERO)
                .refundedAmount(BigDecimal.ZERO)
                .escrowReleased(false)
                .build();

        paymentRepo.save(payment);
        if (provider == PaymentProvider.WALLET) {

            // 🔥 deduct funds immediately
            walletService.withdraw(
                    userId,
                    finalAmount,
                    "DELIVERY_PAYMENT_" + delivery.getTrackingCode()
            );

            // 🔥 mark payment completed
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());

            paymentRepo.save(payment);

            // 🔥 return immediately (NO gateway)
            return PaymentResultDTO.builder()
                    .paymentId(payment.getId())
                    .deliveryId(delivery.getId())
                    .provider(provider)
                    .amountPaid(payment.getAmount())
                    .status(PaymentStatus.COMPLETED)
                    .build();
        }

        PaymentGatewayProvider gateway = getGatewayProvider(provider);

        PaymentResultDTO result = gateway.initiateTransaction(payment);

        payment.setProviderReference(result.getProviderReference());
        payment.setProviderSessionId(result.getProviderSessionId());
        payment.setProviderChargeId(result.getProviderChargeId());

        PaymentStatus newStatus = result.getStatus() !=null ? result.getStatus() : PaymentStatus.PENDING;
        payment.setStatus(newStatus);

        // If the result status is COMPLETED (e.g., Mock or instant provider like WALLET)
        if (newStatus == PaymentStatus.COMPLETED) {
            payment.setCompletedAt(LocalDateTime.now());


        }

        paymentRepo.save(payment);

        result.setPaymentId(payment.getId());
        result.setDeliveryId(delivery.getId());
        result.setProvider(provider);
        result.setAmountPaid(payment.getAmount());

        return result;
    }

    @Override
    public PaymentResultDTO confirmPayment(String reference) {

        Payment payment = paymentRepo.findByProviderReference(reference)
                .orElseThrow(() -> new NotFoundException("Invalid payment reference"));

        PaymentGatewayProvider gateway = getGatewayProvider(payment.getProvider());
        PaymentResultDTO result = gateway.confirmTransaction(reference);

        PaymentStatus confirmedStatus = result.getStatus() != null ? result.getStatus() : payment.getStatus();


        if (payment.getStatus() != confirmedStatus) {
            payment.setStatus(confirmedStatus);
            if (confirmedStatus == PaymentStatus.COMPLETED) {
                payment.setCompletedAt(LocalDateTime.now());

            }
            paymentRepo.save(payment);
        }
        result.setPaymentId(payment.getId());
        result.setDeliveryId(payment.getDelivery().getId());
        result.setProvider(payment.getProvider());
        result.setAmountPaid(payment.getAmount());
        result.setProviderReference(reference);

        return result;
    }

    @Override
    public void refund(Long paymentId, BigDecimal amount, String reason) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));

        PaymentGatewayProvider gateway = getGatewayProvider(payment.getProvider());
        gateway.refundTransaction(payment, amount, reason);
    }


    @Override
    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepo.findByPayerId(userId);
    }

}
