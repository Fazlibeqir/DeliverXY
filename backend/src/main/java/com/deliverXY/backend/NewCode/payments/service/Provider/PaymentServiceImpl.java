package com.deliverXY.backend.NewCode.payments.service.Provider;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import com.deliverXY.backend.NewCode.deliveries.dto.FareResponseDTO;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryRepository;
import com.deliverXY.backend.NewCode.deliveries.service.DeliveryService;
import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.dto.PaymentResultDTO;
import com.deliverXY.backend.NewCode.payments.repository.PaymentRepository;
import com.deliverXY.backend.NewCode.payments.service.PaymentGatewayProvider;
import com.deliverXY.backend.NewCode.payments.service.PaymentService;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
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
    private final DeliveryService deliveryService;
    private final AppUserService userService;

    public PaymentServiceImpl(List<PaymentGatewayProvider> providerList,
                              PaymentRepository paymentRepo,
                              DeliveryRepository deliveryRepo,
                              DeliveryService deliveryService,
                              AppUserService userService) {
        this.paymentRepo = paymentRepo;
        this.deliveryRepo = deliveryRepo;
        this.deliveryService = deliveryService;
        this.userService = userService;
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
    public PaymentResultDTO initializePayment(Long deliveryId, PaymentProvider provider, Long userId)
            throws StripeException {

        var delivery = deliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found"));

        FareResponseDTO fare = deliveryService.getFareForDelivery(deliveryId);
        BigDecimal finalAmount = BigDecimal.valueOf(fare.getTotalFare());

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
                .build();
        paymentRepo.save(payment);

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
    public PaymentResultDTO payWithWallet(Long deliveryId, Long userId) {
        // FIX: Delegation should go through the generic map, assuming WalletPaymentProvider exists.
        // 1. Fetch user (required for wallet access)
       userService.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // 2. Delegate to the main initializePayment, forcing the provider to WALLET
        try {
            return initializePayment(deliveryId, PaymentProvider.WALLET, userId);
        } catch (StripeException e) {
            // Wallet payment shouldn't throw StripeException, but we catch it for compatibility
            throw new BadRequestException("Wallet payment initialization failed: " + e.getMessage());
        }
    }

    @Override
    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepo.findByPayerId(userId);
    }

    @Override
    public List<Payment> getPaymentsByDelivery(Long deliveryId) {
        return paymentRepo.findByDeliveryId(deliveryId);
    }
}
