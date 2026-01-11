package com.deliverXY.backend.NewCode.payments.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.payments.dto.PromoCodeDTO;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.payments.domain.PromoCode;
import com.deliverXY.backend.NewCode.payments.domain.PromoCodeUsage;
import com.deliverXY.backend.NewCode.payments.dto.ValidatePromoCodeRequest;
import com.deliverXY.backend.NewCode.payments.dto.ValidatePromoCodeResponse;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import com.deliverXY.backend.NewCode.payments.service.PromoCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/promo-codes")
@RequiredArgsConstructor
@Slf4j
public class PromoCodeController {

    private final PromoCodeService promoCodeService;
    private final AppUserService appUserService;

    /**
     * Validate promo code before applying
     */
    @PostMapping("/validate")
    public ApiResponse<ValidatePromoCodeResponse> validatePromoCode(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ValidatePromoCodeRequest request) {

        try {
            AppUser user = appUserService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            PromoCodeService.PromoCodeValidationResult result =
                    promoCodeService.validatePromoCode(
                            request.getPromoCode(),
                            user,
                            request.getOrderAmount()
                    );
            BigDecimal finalAmount = request.getOrderAmount().subtract(result.getDiscountAmount());
            ValidatePromoCodeResponse response = new ValidatePromoCodeResponse(
                    result.isValid(),
                    result.getMessage(),
                    result.getDiscountAmount(),
                    finalAmount,
                    result.isValid() ? request.getPromoCode() : null
            );

            return ApiResponse.ok(response);

        } catch (NoSuchElementException e) {
            return ApiResponse.error("User not authenticated or found.", 404, "USER_NOT_FOUND", "/api/promo-codes/validate");
        } catch (Exception e) {
            log.error("Error validating promo code: {}", e.getMessage());
            // Return a specific error response aligned with the ValidatePromoCodeResponse format
            ValidatePromoCodeResponse errorResponse = new ValidatePromoCodeResponse(
                    false,
                    e.getMessage(), // Return specific service error message
                    java.math.BigDecimal.ZERO,
                    request.getOrderAmount(),
                    request.getPromoCode()
            );
            return new ApiResponse<>(false, errorResponse, System.currentTimeMillis(), 400, "PROMO_VALIDATION_ERROR", "/api/promo-codes/validate", e.getMessage());
        }
    }

    /**
     * Get all active promo codes (public)
     */
    @GetMapping("/active")
    public ApiResponse<List<PromoCode>> getActivePromoCodes() {
        List<PromoCode> promoCodes = promoCodeService.getActivePromoCodes();
        return ApiResponse.ok(promoCodes);
    }

    /**
     * Get user's promo code usage history
     */
    @GetMapping("/my-usage")
    public ApiResponse<List<PromoCodeUsage>> getMyPromoCodeUsage(
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            AppUser user = appUserService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<PromoCodeUsage> usageHistory = promoCodeService.getUserPromoCodeUsage(user);
            return ApiResponse.ok(usageHistory);

        } catch (Exception e) {
            log.error("Error fetching promo code usage: {}", e.getMessage());
            return ApiResponse.error("Failed to retrieve usage history.", 400, "USAGE_FETCH_ERROR", "/api/promo-codes/my-usage"); // Use consistent wrapper error
        }
    }

    /**
     * Create promo code (Admin only)
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PromoCode> createPromoCode(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PromoCodeDTO promoCodeDTO) {

        try {
            PromoCode promoCode = new PromoCode();
            promoCode.setCode(promoCodeDTO.getCode().toUpperCase());
            promoCode.setDescription(promoCodeDTO.getDescription());
            promoCode.setDiscountType(promoCodeDTO.getDiscountType());
            promoCode.setDiscountValue(promoCodeDTO.getDiscountValue());
            promoCode.setMaxDiscountAmount(promoCodeDTO.getMaxDiscountAmount());
            promoCode.setMinOrderAmount(promoCodeDTO.getMinOrderAmount());
            promoCode.setUsageLimit(promoCodeDTO.getUsageLimit());
            promoCode.setUsagePerUser(promoCodeDTO.getUsagePerUser());
            promoCode.setStartDate(promoCodeDTO.getStartDate());
            promoCode.setEndDate(promoCodeDTO.getEndDate());
            promoCode.setIsActive(promoCodeDTO.getIsActive());
            promoCode.setIsFirstOrderOnly(promoCodeDTO.getIsFirstOrderOnly());
            promoCode.setApplicableForNewUsersOnly(promoCodeDTO.getApplicableForNewUsersOnly());

            PromoCode created = promoCodeService.createPromoCode(promoCode, userDetails.getUsername());
            return ApiResponse.ok(created);

        } catch (Exception e) {
            log.error("Error creating promo code: {}", e.getMessage());
            return ApiResponse.error(e.getMessage(), 400, "PROMO_CODE_CREATE_FAILED", "/api/promo-codes/create"); // Use consistent wrapper error
        }
    }

    /**
     * Get all promo codes (Admin only) - returns all codes including inactive ones
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<PromoCode>> getAllPromoCodes() {
        List<PromoCode> promoCodes = promoCodeService.getAllPromoCodes();
        return ApiResponse.ok(promoCodes);
    }

    /**
     * Deactivate promo code (Admin only)
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deactivatePromoCode(@PathVariable Long id) {
        try {
            promoCodeService.deactivatePromoCode(id);
            return ApiResponse.ok("Promo code deactivated successfully");
        } catch (Exception e) {
            log.error("Error deactivating promo code: {}", e.getMessage());
            return ApiResponse.error(e.getMessage(), 400, "PROMO_DEACTIVATE_FAILED", "/api/promo-codes/{id}/deactivate"); // Use consistent wrapper error
        }
    }
}