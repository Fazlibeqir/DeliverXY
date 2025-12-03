
package com.deliverXY.backend.NewCode.payments.service;

import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.common.enums.DiscountType;
import com.deliverXY.backend.NewCode.payments.domain.PromoCode;
import com.deliverXY.backend.NewCode.payments.domain.PromoCodeUsage;
import com.deliverXY.backend.NewCode.payments.repository.PromoCodeRepository;
import com.deliverXY.backend.NewCode.payments.repository.PromoCodeUsageRepository;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private static final int SCALE = 2; // Currency scale
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public BigDecimal applyPromoCode(BigDecimal totalFare, String code, AppUser user) {
        // Validate the code using the new BigDecimal validation method
        PromoCodeValidationResult result = validatePromoCode(code, user, totalFare);

        if (!result.isValid()) {
            // If the code is invalid, we don't throw an error during fare ESTIMATION,
            // we simply return the original amount.
            log.warn("Promo code '{}' invalid for user {}: {}", code, user.getId(), result.getMessage());
            return totalFare.setScale(SCALE, ROUNDING_MODE);
        }

        // Apply discount: total - discount
        BigDecimal discountedTotal = totalFare.subtract(result.getDiscountAmount());

        return discountedTotal.setScale(SCALE, ROUNDING_MODE).max(BigDecimal.ZERO);
    }
    /**
     * Validate and apply promo code
     */
    public PromoCodeValidationResult validatePromoCode(String code, AppUser user, BigDecimal orderAmount) {
        log.info("Validating promo code '{}' for user {} with order amount {}", code, user.getId(), orderAmount);

        Optional<PromoCode> promoCodeOpt = promoCodeRepository.findByCodeIgnoreCase(code);

        if (promoCodeOpt.isEmpty()) {
            return new PromoCodeValidationResult(false, "Promo code not found", BigDecimal.ZERO, null);
        }

        PromoCode promoCode = promoCodeOpt.get();

        // Check if promo code is valid (Assuming isValid checks dates/activity)
        if (!promoCode.isValid()) {
            return new PromoCodeValidationResult(false, "Promo code is expired or inactive", BigDecimal.ZERO, null);
        }

        // Check minimum order amount
        if (promoCode.getMinOrderAmount() != null &&
                orderAmount.compareTo(BigDecimal.valueOf(promoCode.getMinOrderAmount())) < 0) {
            return new PromoCodeValidationResult(
                    false,
                    String.format("Minimum order amount is %.2f %s", promoCode.getMinOrderAmount(), promoCode.getCurrency()), // Assuming currency is available
                    BigDecimal.ZERO,
                    null
            );
        }

        // Check if user is eligible (new user only promos) - First delivery check logic consolidated below
        long userDeliveryCount = deliveryRepository.countByClientId(user.getId());

        if (promoCode.getApplicableForNewUsersOnly() && userDeliveryCount > 0) {
            return new PromoCodeValidationResult(false, "This promo code is only for new users", BigDecimal.ZERO, null);
        }

        // Check if it's first order only (this is redundant if 'ApplicableForNewUsersOnly' exists, but we keep it for now)
        if (promoCode.getIsFirstOrderOnly() && userDeliveryCount > 0) {
            return new PromoCodeValidationResult(false, "This promo code is only valid for first order", BigDecimal.ZERO, null);
        }

        // Check usage per user limit
        long userUsageCount = promoCodeUsageRepository.countByPromoCodeAndUser(promoCode, user);
        if (promoCode.getUsagePerUser() != null && userUsageCount >= promoCode.getUsagePerUser()) {
            return new PromoCodeValidationResult(false, "You have already used this promo code the maximum number of times", BigDecimal.ZERO, null);
        }

        // Calculate discount (now returns BigDecimal)
        BigDecimal discount = calculateDiscount(promoCode, orderAmount);

        log.info("Promo code '{}' validated successfully. Discount: {}", code, discount);

        return new PromoCodeValidationResult(true, "Promo code applied successfully", discount, promoCode);
    }

    /**
     * Calculate discount amount based on promo code type
     */
    private BigDecimal calculateDiscount(PromoCode promoCode, BigDecimal orderAmount) {
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal discountValue = BigDecimal.valueOf(promoCode.getDiscountValue());

        if (promoCode.getDiscountType() == DiscountType.PERCENTAGE) {
            // Calculate: orderAmount * (discountValue / 100)
            BigDecimal percentage = discountValue.divide(BigDecimal.valueOf(100), SCALE, ROUNDING_MODE);
            discount = orderAmount.multiply(percentage).setScale(SCALE, ROUNDING_MODE);

            // Apply max discount cap if specified
            if (promoCode.getMaxDiscountAmount() != null) {
                BigDecimal maxDiscount = BigDecimal.valueOf(promoCode.getMaxDiscountAmount());
                discount = discount.min(maxDiscount);
            }
        } else if (promoCode.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discount = discountValue.setScale(SCALE, ROUNDING_MODE);
        }

        // Ensure discount doesn't exceed order amount
        discount = discount.min(orderAmount).max(BigDecimal.ZERO);

        return discount;
    }

    /**
     * Record promo code usage
     */
    @Transactional
    public void recordPromoCodeUsage(PromoCode promoCode, AppUser user, Delivery delivery,
                                     BigDecimal originalAmount, BigDecimal discountAmount) {

        // Input validation to avoid data loss
        if (originalAmount == null || discountAmount == null) {
            throw new BadRequestException("Cannot record promo usage with null amounts.");
        }

        PromoCodeUsage usage = new PromoCodeUsage();
        usage.setPromoCode(promoCode);
        usage.setUser(user);
        usage.setDelivery(delivery);

        // Store BigDecimal values
        usage.setOriginalAmount(originalAmount.setScale(SCALE, ROUNDING_MODE).doubleValue());
        usage.setDiscountAmount(discountAmount.setScale(SCALE, ROUNDING_MODE).doubleValue());

        BigDecimal finalAmount = originalAmount.subtract(discountAmount).max(BigDecimal.ZERO);
        usage.setFinalAmount(finalAmount.doubleValue());

        promoCodeUsageRepository.save(usage);

        // Increment usage count
        promoCode.setCurrentUsage(promoCode.getCurrentUsage() + 1);
        promoCodeRepository.save(promoCode);

        log.info("Recorded promo code usage for code '{}' by user {}. Discount: {}", promoCode.getCode(), user.getId(), discountAmount);
    }

    /**
     * Create new promo code (Admin only)
     */
    @Transactional
    public PromoCode createPromoCode(PromoCode promoCode, String adminEmail) {
        if (promoCodeRepository.existsByCodeIgnoreCase(promoCode.getCode())) {
            throw new BadRequestException("Promo code already exists");
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
        private BigDecimal discountAmount;
        private PromoCode promoCode;
    }
}