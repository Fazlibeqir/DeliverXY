package com.deliverXY.backend.controllers;

import com.deliverXY.backend.models.Payment;
import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.service.PaymentService;
import com.deliverXY.backend.service.DeliveryService;
import com.deliverXY.backend.service.AppUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final DeliveryService deliveryService;
    private final AppUserService appUserService;
    
    public PaymentController(PaymentService paymentService, DeliveryService deliveryService, 
                           AppUserService appUserService) {
        this.paymentService = paymentService;
        this.deliveryService = deliveryService;
        this.appUserService = appUserService;
    }
    
    @PostMapping("/create-intent")
    public ResponseEntity<Payment> createPaymentIntent(@RequestParam Long deliveryId, 
                                                     @RequestParam Long payerId) {
        try {
            Delivery delivery = deliveryService.findById(deliveryId);
            if (delivery == null) {
                return ResponseEntity.notFound().build();
            }
            
            AppUser payer = appUserService.findById(payerId).orElseThrow();

            Payment payment = paymentService.createPaymentIntent(delivery, payer);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PostMapping("/confirm")
    public ResponseEntity<Payment> confirmPayment(@RequestParam String paymentIntentId) {
        try {
            Payment payment = paymentService.processPayment(paymentIntentId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PostMapping("/refund")
    public ResponseEntity<Payment> refundPayment(@RequestParam Long paymentId,
                                               @RequestParam BigDecimal amount,
                                               @RequestParam String reason) {
        try {
            Payment refundedPayment = paymentService.refundPayment(paymentId, amount, reason);
            return ResponseEntity.ok(refundedPayment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getUserPayments(@PathVariable Long userId) {
        try {
            List<Payment> payments = paymentService.getPaymentsByUser(userId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/delivery/{deliveryId}")
    public ResponseEntity<List<Payment>> getDeliveryPayments(@PathVariable Long deliveryId) {
        try {
            List<Payment> payments = paymentService.getPaymentsByDelivery(deliveryId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.getPaymentById(paymentId);
            return payment != null ? ResponseEntity.ok(payment) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PostMapping("/wallet/topup")
    public ResponseEntity<String> topUpWallet(@RequestParam Long userId, 
                                            @RequestParam BigDecimal amount) {
        try {
            AppUser user = appUserService.findById(userId).orElseThrow();

            paymentService.topUpWallet(user, amount);
            return ResponseEntity.ok("Wallet topped up successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to top up wallet: " + e.getMessage());
        }
    }
    
    @PostMapping("/wallet/withdraw")
    public ResponseEntity<String> withdrawFromWallet(@RequestParam Long userId, 
                                                   @RequestParam BigDecimal amount) {
        try {
            AppUser user = appUserService.findById(userId).orElseThrow();

            paymentService.withdrawFromWallet(user, amount);
            return ResponseEntity.ok("Withdrawal successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to withdraw: " + e.getMessage());
        }
    }
    
    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, 
                                                    @RequestHeader("Stripe-Signature") String signature) {
        try {
            // Handle Stripe webhook events
            // This would validate the signature and process events like payment success/failure
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook processing failed: " + e.getMessage());
        }
    }
} 