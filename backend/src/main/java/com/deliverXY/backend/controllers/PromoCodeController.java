package com.deliverXY.backend.controllers;

import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.PromoCode;
import com.deliverXY.backend.models.PromoCodeUsage;
import com.deliverXY.backend.models.dto.PromoCodeDTO;
import com.deliverXY.backend.models.dto.ValidatePromoCodeRequest;
import com.deliverXY.backend.models.dto.ValidatePromoCodeResponse;
import com.deliverXY.backend.service.AppUserService;
import com.deliverXY.backend.service.impl.PromoCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ValidatePromoCodeResponse> validatePromoCode(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ValidatePromoCodeRequest request) {

        try {
            AppUser user = appUserService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            PromoCodeService.PromoCodeValidationResult result =
                    promoCodeService.validatePromoCode(
                            request.getPromoCode(),
                            user,
                            request.getOrderAmount()
                    );

            ValidatePromoCodeResponse response = new ValidatePromoCodeResponse(
                    result.isValid(),
                    result.getMessage(),
                    result.getDiscountAmount(),
                    request.getOrderAmount() - result.getDiscountAmount(),
                    result.isValid() ? request.getPromoCode() : null
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error validating promo code: {}", e.getMessage());
            return ResponseEntity.ok(new ValidatePromoCodeResponse(
                    false,
                    "Error validating promo code",
                    0.0,
                    request.getOrderAmount(),
                    null
            ));
        }
    }

    /**
     * Get all active promo codes (public)
     */
    @GetMapping("/active")
    public ResponseEntity<List<PromoCode>> getActivePromoCodes() {
        List<PromoCode> promoCodes = promoCodeService.getActivePromoCodes();
        return ResponseEntity.ok(promoCodes);
    }

    /**
     * Get user's promo code usage history
     */
    @GetMapping("/my-usage")
    public ResponseEntity<List<PromoCodeUsage>> getMyPromoCodeUsage(
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            AppUser user = appUserService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<PromoCodeUsage> usageHistory = promoCodeService.getUserPromoCodeUsage(user);
            return ResponseEntity.ok(usageHistory);

        } catch (Exception e) {
            log.error("Error fetching promo code usage: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create promo code (Admin only)
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPromoCode(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PromoCodeDTO promoCodeDTO) {

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
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating promo code: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get all promo codes (Admin only)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PromoCode>> getAllPromoCodes() {
        List<PromoCode> promoCodes = promoCodeService.getActivePromoCodes();
        return ResponseEntity.ok(promoCodes);
    }

    /**
     * Deactivate promo code (Admin only)
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivatePromoCode(@PathVariable Long id) {
        try {
            promoCodeService.deactivatePromoCode(id);
            return ResponseEntity.ok("Promo code deactivated successfully");
        } catch (Exception e) {
            log.error("Error deactivating promo code: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}