
package com.deliverXY.backend.NewCode.payments.service;

import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.payments.domain.PromoCode;
import com.deliverXY.backend.NewCode.payments.domain.PromoCodeUsage;
import com.deliverXY.backend.NewCode.payments.repository.PromoCodeRepository;
import com.deliverXY.backend.NewCode.payments.repository.PromoCodeUsageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeUsageRepository promoCodeUsageRepository;
    private final PromoCodeValidationService promoCodeValidationService;

    private static final int SCALE = 2; // Currency scale
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public BigDecimal applyPromoCode(BigDecimal totalFare, String code, AppUser user) {
        // Validate the code using the new BigDecimal validation method
        PromoCodeValidationResult result = promoCodeValidationService.validatePromoCode(code, user, totalFare);

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
    @Transactional(readOnly = true)
    public PromoCodeValidationResult validatePromoCode(String code, AppUser user, BigDecimal orderAmount) {
        return promoCodeValidationService.validatePromoCode(code, user, orderAmount);
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
        usage.setOriginalAmount(originalAmount.setScale(SCALE, ROUNDING_MODE));
        usage.setDiscountAmount(discountAmount.setScale(SCALE, ROUNDING_MODE));

        BigDecimal finalAmount = originalAmount.subtract(discountAmount).max(BigDecimal.ZERO);
        usage.setFinalAmount(finalAmount);

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
    @Transactional(readOnly = true)
    public List<PromoCode> getActivePromoCodes() {
        Pageable pageable = PageRequest.of(0, 100);
        return promoCodeRepository.findAllActivePromoCodes(LocalDateTime.now(), pageable).getContent();
    }

    /**
     * Get all promo codes (for admin - includes inactive, expired, etc.)
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<PromoCode> getAllPromoCodes(org.springframework.data.domain.Pageable pageable) {
        return promoCodeRepository.findAll(Objects.requireNonNull(pageable, "pageable"));
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
    @Transactional(readOnly = true)
    public List<PromoCodeUsage> getUserPromoCodeUsage(AppUser user) {
        Pageable pageable = PageRequest.of(0, 50);
        return promoCodeUsageRepository.findByUserOrderByUsedAtDesc(Objects.requireNonNull(user, "user"), pageable).getContent();
    }

    /**
     * Deactivate promo code
     */
    @Transactional
    public void deactivatePromoCode(Long promoCodeId) {
        if (promoCodeId == null) {
            throw new BadRequestException("Promo code ID cannot be null");
        }
        Long id = Objects.requireNonNull(promoCodeId, "promoCodeId");
        promoCodeRepository.findById(id).ifPresent(promoCode -> {
            promoCode.setIsActive(false);
            promoCodeRepository.save(promoCode);
            log.info("Deactivated promo code '{}'", promoCode.getCode());
        });
    }

    /**
     * Result class for validation
     */
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class PromoCodeValidationResult {
        private final boolean valid;
        private final String message;
        private final BigDecimal discountAmount;
        private final PromoCode promoCode;
    }
}