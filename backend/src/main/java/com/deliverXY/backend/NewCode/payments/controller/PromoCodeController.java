package com.deliverXY.backend.NewCode.payments.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.payments.domain.PromoCode;
import com.deliverXY.backend.NewCode.payments.domain.PromoCodeUsage;
import com.deliverXY.backend.NewCode.payments.dto.PromoCodeDTO;
import com.deliverXY.backend.NewCode.payments.dto.ValidatePromoCodeRequest;
import com.deliverXY.backend.NewCode.payments.dto.ValidatePromoCodeResponse;
import com.deliverXY.backend.NewCode.payments.service.PromoCodeApplicationService;
import com.deliverXY.backend.NewCode.payments.service.PromoCodeService;
import com.deliverXY.backend.NewCode.payments.service.PromoCodeUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promo-codes")
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeService promoCodeService;
    private final PromoCodeApplicationService promoCodeApplicationService;
    private final PromoCodeUserService promoCodeUserService;

    @PostMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ValidatePromoCodeResponse> validatePromoCode(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ValidatePromoCodeRequest request) {
        return promoCodeApplicationService.validatePromoCodeForUser(userDetails.getUsername(), request);
    }

    @GetMapping("/active")
    public ApiResponse<List<PromoCode>> getActivePromoCodes() {
        return ApiResponse.ok(promoCodeService.getActivePromoCodes());
    }

    @GetMapping("/my-usage")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<PromoCodeUsage>> getMyPromoCodeUsage(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(promoCodeService.getUserPromoCodeUsage(
                promoCodeUserService.requireByEmail(userDetails.getUsername())
        ));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PromoCode> createPromoCode(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PromoCodeDTO promoCodeDTO) {
        return promoCodeApplicationService.createPromoCodeForAdmin(userDetails.getUsername(), promoCodeDTO);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<PromoCode>> getAllPromoCodes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(promoCodeService.getAllPromoCodes(PageRequest.of(page, size)));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deactivatePromoCode(@PathVariable Long id) {
        return promoCodeApplicationService.deactivatePromoCode(id);
    }
}
