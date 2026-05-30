package com.deliverXY.backend.NewCode.payments.service;

import com.deliverXY.backend.NewCode.payments.dto.ValidatePromoCodeRequest;
import com.deliverXY.backend.NewCode.payments.dto.ValidatePromoCodeResponse;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PromoCodeValidationOrchestrator {

    private final PromoCodeService promoCodeService;
    private final PromoCodeUserService promoCodeUserService;

    @Transactional(readOnly = true)
    public ValidatePromoCodeResponse validateForUser(String username, ValidatePromoCodeRequest request) {
        AppUser user = promoCodeUserService.requireByEmail(username);
        PromoCodeService.PromoCodeValidationResult result = promoCodeService.validatePromoCode(
                request.getPromoCode(),
                user,
                request.getOrderAmount()
        );
        BigDecimal finalAmount = request.getOrderAmount().subtract(result.getDiscountAmount());
        return new ValidatePromoCodeResponse(
                result.isValid(),
                result.getMessage(),
                result.getDiscountAmount(),
                finalAmount,
                result.isValid() ? request.getPromoCode() : null
        );
    }
}
