
package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.enums.DiscountType;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.PromoCode;
import com.deliverXY.backend.models.PromoCodeUsage;
import com.deliverXY.backend.repositories.PromoCodeRepository;
import com.deliverXY.backend.repositories.PromoCodeUsageRepository;
import com.deliverXY.backend.repositories.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeUsageRepository promoCodeUsageRepository;
    private final DeliveryRepository deliveryRepository;

    /**
     * Validate and apply promo code
     */
    public PromoCodeValidationResult validatePromoCode(String code, AppUser user, double orderAmount) {
        log.info("Validating promo code '{}' for user {} with order amount {} MKD", code, user.getId(), orderAmount);

        Optional<PromoCode> promoCodeOpt = promoCodeRepository.findByCodeIgnoreCase(code);

        if (promoCodeOpt.isEmpty()) {
            return new PromoCodeValidationResult(false, "Promo code not found", 0.0, null);
        }

        PromoCode promoCode = promoCodeOpt.get();

        // Check if promo code is valid
        if (!promoCode.isValid()) {
            return new PromoCodeValidationResult(false, "Promo code is expired or inactive", 0.0, null);
        }

        // Check minimum order amount
        if (promoCode.getMinOrderAmount() != null && orderAmount < promoCode.getMinOrderAmount()) {
            return new PromoCodeValidationResult(
                    false,
                    String.format("Minimum order amount is %.2f MKD", promoCode.getMinOrderAmount()),
                    0.0,
                    null
            );
        }

        // Check if user is eligible (new user only promos)
        if (promoCode.getApplicableForNewUsersOnly()) {
            long userDeliveryCount = deliveryRepository.countByClientId(user.getId());
            if (userDeliveryCount > 0) {
                return new PromoCodeValidationResult(false, "This promo code is only for new users", 0.0, null);
            }
        }

        // Check if it's first order only
        if (promoCode.getIsFirstOrderOnly()) {
            long userDeliveryCount = deliveryRepository.countByClientId(user.getId());
            if (userDeliveryCount > 0) {
                return new PromoCodeValidationResult(false, "This promo code is only valid for first order", 0.0, null);
            }
        }

        // Check usage per user limit
        long userUsageCount = promoCodeUsageRepository.countByPromoCodeAndUser(promoCode, user);
        if (promoCode.getUsagePerUser() != null && userUsageCount >= promoCode.getUsagePerUser()) {
            return new PromoCodeValidationResult(false, "You have already used this promo code", 0.0, null);
        }

        // Calculate discount
        double discount = calculateDiscount(promoCode, orderAmount);

        log.info("Promo code '{}' validated successfully. Discount: {} MKD", code, discount);

        return new PromoCodeValidationResult(true, "Promo code applied successfully", discount, promoCode);
    }

    /**
     * Calculate discount amount based on promo code type
     */
    private double calculateDiscount(PromoCode promoCode, double orderAmount) {
        double discount = 0.0;

        if (promoCode.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = orderAmount * (promoCode.getDiscountValue() / 100.0);

            // Apply max discount cap if specified
            if (promoCode.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, promoCode.getMaxDiscountAmount());
            }
        } else if (promoCode.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discount = promoCode.getDiscountValue();
        }

        // Ensure discount doesn't exceed order amount
        discount = Math.min(discount, orderAmount);

        return Math.round(discount * 100.0) / 100.0;
    }

    /**
     * Record promo code usage
     */
    @Transactional
    public void recordPromoCodeUsage(PromoCode promoCode, AppUser user, Delivery delivery,
                                     double originalAmount, double discountAmount) {
        PromoCodeUsage usage = new PromoCodeUsage();
        usage.setPromoCode(promoCode);
        usage.setUser(user);
        usage.setDelivery(delivery);
        usage.setOriginalAmount(originalAmount);
        usage.setDiscountAmount(discountAmount);
        usage.setFinalAmount(originalAmount - discountAmount);

        promoCodeUsageRepository.save(usage);

        // Increment usage count
        promoCode.setCurrentUsage(promoCode.getCurrentUsage() + 1);
        promoCodeRepository.save(promoCode);

        log.info("Recorded promo code usage for code '{}' by user {}", promoCode.getCode(), user.getId());
    }

    /**
     * Create new promo code (Admin only)
     */
    @Transactional
    public PromoCode createPromoCode(PromoCode promoCode, String adminEmail) {
        if (promoCodeRepository.existsByCodeIgnoreCase(promoCode.getCode())) {
            throw new RuntimeException("Promo code already exists");
        }

        promoCode.setCreatedBy(adminEmail);
        promoCode.setCurrentUsage(0);

        PromoCode saved = promoCodeRepository.save(promoCode);
        log.info("Created promo code '{}' by admin {}", saved.getCode(), adminEmail);

        return saved;
    }

    /**
     * Get all active promo codes
     */
    public List<PromoCode> getActivePromoCodes() {
        return promoCodeRepository.findAllActivePromoCodes(LocalDateTime.now());
    }

    /**
     * Get promo code by code
     */
    public Optional<PromoCode> getPromoCodeByCode(String code) {
        return promoCodeRepository.findByCodeIgnoreCase(code);
    }

    /**
     * Get user's promo code usage history
     */
    public List<PromoCodeUsage> getUserPromoCodeUsage(AppUser user) {
        return promoCodeUsageRepository.findByUserOrderByUsedAtDesc(user);
    }

    /**
     * Deactivate promo code
     */
    @Transactional
    public void deactivatePromoCode(Long promoCodeId) {
        promoCodeRepository.findById(promoCodeId).ifPresent(promoCode -> {
            promoCode.setIsActive(false);
            promoCodeRepository.save(promoCode);
            log.info("Deactivated promo code '{}'", promoCode.getCode());
        });
    }

    /**
     * Result class for validation
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class PromoCodeValidationResult {
        private boolean valid;
        private String message;
        private double discountAmount;
        private PromoCode promoCode;
    }
}