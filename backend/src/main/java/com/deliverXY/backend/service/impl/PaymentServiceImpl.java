package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.models.Payment;
import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.Wallet;
import com.deliverXY.backend.repositories.PaymentRepository;
import com.deliverXY.backend.repositories.WalletRepository;
import com.deliverXY.backend.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    
    @Value("${stripe.secret-key}")
    private String stripeSecretKey;
    
    public PaymentServiceImpl(PaymentRepository paymentRepository, WalletRepository walletRepository) {
        this.paymentRepository = paymentRepository;
        this.walletRepository = walletRepository;
    }
    
    @Override
    @Transactional
    public Payment createPaymentIntent(Delivery delivery, AppUser payer) {
        try {
            Stripe.apiKey = stripeSecretKey;
            
            // Create Stripe PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(delivery.getFinalAmount().multiply(new BigDecimal("100")).longValue()) // Convert to cents
                .setCurrency("usd")
                .setDescription("Delivery: " + delivery.getTitle())
                .putMetadata("delivery_id", delivery.getId().toString())
                .putMetadata("payer_id", payer.getId().toString())
                .build();
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // Create payment record
            Payment payment = new Payment(
                UUID.randomUUID().toString(),
                payer,
                delivery.getFinalAmount(),
                Payment.PaymentType.DELIVERY_PAYMENT,
                "Delivery payment for: " + delivery.getTitle()
            );
            
            payment.setDelivery(delivery);
            payment.setStripePaymentIntentId(paymentIntent.getId());
            payment.setStatus(Payment.PaymentStatus.PENDING);
            
            return paymentRepository.save(payment);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create payment intent: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Payment processPayment(String paymentIntentId) {
        try {
            Stripe.apiKey = stripeSecretKey;
            
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            
            if ("succeeded".equals(paymentIntent.getStatus())) {
                Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
                
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setStripeChargeId(paymentIntent.getLatestCharge());
                payment.setCompletedAt(java.time.LocalDateTime.now());
                
                return paymentRepository.save(payment);
            } else {
                throw new RuntimeException("Payment not successful: " + paymentIntent.getStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process payment: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Payment refundPayment(Long paymentId, BigDecimal amount, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        try {
            Stripe.apiKey = stripeSecretKey;
            
            // Process refund through Stripe
            // Note: This is a simplified implementation
            // In production, you'd want to handle partial refunds properly
            
            payment.setRefundAmount(amount);
            payment.setRefundReason(reason);
            payment.setRefundedAt(java.time.LocalDateTime.now());
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            
            return paymentRepository.save(payment);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process refund: " + e.getMessage());
        }
    }
    
    @Override
    public List<Payment> getPaymentsByUser(Long userId) {
        // This would need a custom query method in the repository
        // For now, return empty list
        return List.of();
    }
    
    @Override
    public List<Payment> getPaymentsByDelivery(Long deliveryId) {
        // This would need a custom query method in the repository
        // For now, return empty list
        return List.of();
    }
    
    @Override
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId).orElse(null);
    }
    
    @Override
    @Transactional
    public void processAgentPayout(AppUser agent, BigDecimal amount) {
        // Create payout payment record
        Payment payout = new Payment(
            UUID.randomUUID().toString(),
            null, // Platform is the payer
            amount,
            Payment.PaymentType.AGENT_PAYOUT,
            "Agent payout for deliveries"
        );
        
        payout.setRecipient(agent);
        payout.setStatus(Payment.PaymentStatus.COMPLETED);
        payout.setCompletedAt(java.time.LocalDateTime.now());
        
        paymentRepository.save(payout);
        
        // Update agent's total earnings
        agent.setTotalEarnings(agent.getTotalEarnings() + amount.doubleValue());
        // Note: You'd need to save the updated agent
    }
    
    @Override
    @Transactional
    public void topUpWallet(AppUser user, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUser(user)
            .orElseGet(() -> {
                Wallet newWallet = new Wallet(user);
                return walletRepository.save(newWallet);
            });
        
        wallet.addFunds(amount);
        walletRepository.save(wallet);
        
        // Create payment record for wallet top-up
        Payment topUpPayment = new Payment(
            UUID.randomUUID().toString(),
            user,
            amount,
            Payment.PaymentType.WALLET_TOPUP,
            "Wallet top-up"
        );
        
        topUpPayment.setStatus(Payment.PaymentStatus.COMPLETED);
        topUpPayment.setCompletedAt(java.time.LocalDateTime.now());
        
        paymentRepository.save(topUpPayment);
    }
    
    @Override
    @Transactional
    public void withdrawFromWallet(AppUser user, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUser(user)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        if (wallet.deductFunds(amount)) {
            walletRepository.save(wallet);
            
            // Create payment record for withdrawal
            Payment withdrawalPayment = new Payment(
                UUID.randomUUID().toString(),
                user,
                amount,
                Payment.PaymentType.WALLET_WITHDRAWAL,
                "Wallet withdrawal"
            );
            
            withdrawalPayment.setStatus(Payment.PaymentStatus.COMPLETED);
            withdrawalPayment.setCompletedAt(java.time.LocalDateTime.now());
            
            paymentRepository.save(withdrawalPayment);
        } else {
            throw new RuntimeException("Insufficient funds or limit exceeded");
        }
    }
} 