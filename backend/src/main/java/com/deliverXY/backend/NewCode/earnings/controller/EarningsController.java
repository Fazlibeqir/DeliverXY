package com.deliverXY.backend.NewCode.earnings.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.earnings.dto.DriverEarningsDTO;
import com.deliverXY.backend.NewCode.earnings.dto.DriverPayoutDTO;
import com.deliverXY.backend.NewCode.earnings.dto.EarningsSummaryDTO;
import com.deliverXY.backend.NewCode.earnings.dto.PayoutRequestDTO;
import com.deliverXY.backend.NewCode.earnings.service.EarningsService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/earnings")
@RequiredArgsConstructor
public class EarningsController {

    private final EarningsService earningsService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<EarningsSummaryDTO> summary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {

        if (start == null) start = LocalDate.now().minusMonths(1);
        if (end == null) end = LocalDate.now();

        var stats = earningsService.getDriverSummary(principal.getUser().getId(), start, end);
        return ApiResponse.ok(stats);
    }

    @GetMapping
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<Page<DriverEarningsDTO>> earnings(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.ok(
                earningsService.getDriverEarnings(principal.getUser().getId(), PageRequest.of(page, size))
        );
    }

    @PostMapping("/request-payout")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<DriverPayoutDTO> requestPayout(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody PayoutRequestDTO request) {

        var payout = earningsService.requestManualPayout(principal.getUser().getId(), request);
        return ApiResponse.ok(payout);
    }

    @GetMapping("/payouts")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<Page<DriverPayoutDTO>> history(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.ok(
                earningsService.getPayoutHistory(principal.getUser().getId(), PageRequest.of(page, size))
        );
    }
}