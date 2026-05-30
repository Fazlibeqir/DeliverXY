package com.deliverXY.backend.NewCode.admin.controller;

import com.deliverXY.backend.NewCode.admin.dto.ProcessPayoutRequest;
import com.deliverXY.backend.NewCode.admin.service.AdminPayoutOverviewService;
import jakarta.validation.Valid;
import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.earnings.service.EarningsService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/payouts")
@RequiredArgsConstructor
public class AdminPayoutOverviewController {

    private final EarningsService earningsService;
    private final AdminPayoutOverviewService payoutOverviewService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> listAllPayouts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(payoutOverviewService.listAllPayouts(PageRequest.of(page, size)));
    }

    @PostMapping("/{payoutId}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> processPayout(
            @PathVariable @NonNull Long payoutId,
            @Valid @RequestBody ProcessPayoutRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        String transactionRef = request.getTransactionRef();
        if (transactionRef == null || transactionRef.trim().isEmpty()) {
            transactionRef = "ADMIN-" + payoutId + "-" + System.currentTimeMillis();
        }

        String processedBy = principal.getUser().getEmail() + " (" + principal.getUser().getUsername() + ")";

        earningsService.processPayout(payoutId, transactionRef, processedBy);

        return ApiResponse.ok("Payout processed successfully");
    }
}
