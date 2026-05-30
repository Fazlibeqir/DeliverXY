package com.deliverXY.backend.NewCode.payments.service;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.common.util.ExceptionMessageSanitizer;
import com.deliverXY.backend.NewCode.common.util.RequestPayloadValidator;
import com.deliverXY.backend.NewCode.payments.domain.PromoCode;
import com.deliverXY.backend.NewCode.payments.dto.PromoCodeDTO;
import com.deliverXY.backend.NewCode.payments.dto.ValidatePromoCodeRequest;
import com.deliverXY.backend.NewCode.payments.dto.ValidatePromoCodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromoCodeApplicationService {

    private final PromoCodeService promoCodeService;
    private final PromoCodeValidationOrchestrator validationOrchestrator;
    private final PromoCodeAdminOrchestrator adminOrchestrator;

    public ApiResponse<ValidatePromoCodeResponse> validatePromoCodeForUser(
            String username,
            ValidatePromoCodeRequest request
    ) {
        ValidatePromoCodeRequest body = RequestPayloadValidator.requireBody(request);
        try {
            return ApiResponse.ok(validationOrchestrator.validateForUser(username, body));
        } catch (NoSuchElementException e) {
            return ApiResponse.error("User not authenticated or found.", 404, "USER_NOT_FOUND", "/api/promo-codes/validate");
        } catch (Exception e) {
            log.error("Error validating promo code", e);
            ValidatePromoCodeResponse errorResponse = new ValidatePromoCodeResponse(
                    false,
                    ExceptionMessageSanitizer.validationFailureMessage(),
                    BigDecimal.ZERO,
                    body.getOrderAmount(),
                    body.getPromoCode()
            );
            return new ApiResponse<>(false, errorResponse, System.currentTimeMillis(), 400,
                    "PROMO_VALIDATION_ERROR", "/api/promo-codes/validate", null);
        }
    }

    public ApiResponse<PromoCode> createPromoCodeForAdmin(String adminUsername, PromoCodeDTO dto) {
        try {
            return ApiResponse.ok(adminOrchestrator.createFromDto(RequestPayloadValidator.requireBody(dto), adminUsername));
        } catch (Exception e) {
            log.error("Error creating promo code", e);
            return ApiResponse.error(ExceptionMessageSanitizer.operationFailureMessage(), 400, "PROMO_CODE_CREATE_FAILED", "/api/promo-codes/create");
        }
    }

    public ApiResponse<String> deactivatePromoCode(Long id) {
        try {
            promoCodeService.deactivatePromoCode(id);
            return ApiResponse.ok("Promo code deactivated successfully");
        } catch (Exception e) {
            log.error("Error deactivating promo code", e);
            return ApiResponse.error(ExceptionMessageSanitizer.operationFailureMessage(), 400, "PROMO_DEACTIVATE_FAILED", "/api/promo-codes/{id}/deactivate");
        }
    }
}
