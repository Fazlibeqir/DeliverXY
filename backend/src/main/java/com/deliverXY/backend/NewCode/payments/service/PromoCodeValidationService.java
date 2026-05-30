package com.deliverXY.backend.NewCode.payments.service;

import com.deliverXY.backend.NewCode.common.enums.DiscountType;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryRepository;
import com.deliverXY.backend.NewCode.payments.domain.PromoCode;
import com.deliverXY.backend.NewCode.payments.repository.PromoCodeRepository;
import com.deliverXY.backend.NewCode.payments.repository.PromoCodeUsageRepository;
import com.deliverXY.backend.NewCode.payments.service.PromoCodeService.PromoCodeValidationResult;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromoCodeValidationService {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeUsageRepository promoCodeUsageRepository;
    private final DeliveryRepository deliveryRepository;

    @Transactional(readOnly = true)
    public PromoCodeValidationResult validatePromoCode(String code, AppUser user, BigDecimal orderAmount) {
        log.info("Validating promo code '{}' for user {} with order amount {}", code, user.getId(), orderAmount);

        Optional<PromoCode> promoCodeOpt = promoCodeRepository.findByCodeIgnoreCase(code);
        if (promoCodeOpt.isEmpty()) {
            return new PromoCodeValidationResult(false, "Promo code not found", BigDecimal.ZERO, null);
        }

        PromoCode promoCode = promoCodeOpt.get();
        if (promoCode.getIsActive() == null || !promoCode.getIsActive()) {
            return new PromoCodeValidationResult(false, "Promo code is not active", BigDecimal.ZERO, null);
        }

        LocalDateTime now = LocalDateTime.now();
        if (promoCode.getStartDate() != null && now.isBefore(promoCode.getStartDate())) {
            return new PromoCodeValidationResult(false, "Promo code has not started yet", BigDecimal.ZERO, null);
        }
        if (promoCode.getEndDate() != null && now.isAfter(promoCode.getEndDate())) {
            return new PromoCodeValidationResult(false, "Promo code has expired", BigDecimal.ZERO, null);
        }
        if (promoCode.getUsageLimit() != null && promoCode.getCurrentUsage() >= promoCode.getUsageLimit()) {
            return new PromoCodeValidationResult(false, "Promo code usage limit has been reached", BigDecimal.ZERO, null);
        }
        if (promoCode.getMinOrderAmount() != null && orderAmount.compareTo(promoCode.getMinOrderAmount()) < 0) {
            return new PromoCodeValidationResult(
                    false,
                    String.format("Minimum order amount is %.2f %s", promoCode.getMinOrderAmount(), promoCode.getCurrency()),
                    BigDecimal.ZERO,
                    null
            );
        }

        long userDeliveryCount = deliveryRepository.countByClientId(user.getId());
        if (promoCode.getApplicableForNewUsersOnly() && userDeliveryCount > 0) {
            return new PromoCodeValidationResult(false, "This promo code is only for new users", BigDecimal.ZERO, null);
        }
        if (promoCode.getIsFirstOrderOnly() && userDeliveryCount > 0) {
            return new PromoCodeValidationResult(false, "This promo code is only valid for first order", BigDecimal.ZERO, null);
        }

        Long userUsageCount = promoCodeUsageRepository.countByPromoCodeAndUser(promoCode, user);
        if (promoCode.getUsagePerUser() != null && userUsageCount != null && userUsageCount >= promoCode.getUsagePerUser()) {
            return new PromoCodeValidationResult(false, "You have already used this promo code the maximum number of times", BigDecimal.ZERO, null);
        }

        BigDecimal discount = calculateDiscount(promoCode, orderAmount);
        log.info("Promo code '{}' validated successfully. Discount: {}", code, discount);
        return new PromoCodeValidationResult(true, "Promo code applied successfully", discount, promoCode);
    }

    private BigDecimal calculateDiscount(PromoCode promoCode, BigDecimal orderAmount) {
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal discountValue = promoCode.getDiscountValue();

        if (promoCode.getDiscountType() == DiscountType.PERCENTAGE) {
            BigDecimal percentage = discountValue.divide(BigDecimal.valueOf(100), SCALE, ROUNDING_MODE);
            discount = orderAmount.multiply(percentage).setScale(SCALE, ROUNDING_MODE);
            if (promoCode.getMaxDiscountAmount() != null) {
                discount = discount.min(promoCode.getMaxDiscountAmount());
            }
        } else if (promoCode.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discount = discountValue.setScale(SCALE, ROUNDING_MODE);
        }

        return discount.min(orderAmount).max(BigDecimal.ZERO);
    }
}
