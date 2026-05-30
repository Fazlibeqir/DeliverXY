package com.deliverXY.backend.NewCode.earnings.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.earnings.service.EarningsService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver/payouts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('AGENT')")
public class DriverPayoutController {

    private final EarningsService earningsService;

    @GetMapping
    public ApiResponse<?> getMyPayouts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(
                earningsService.getPayoutHistory(
                        principal.getUser().getId(),
                        PageRequest.of(page, size)
                )
        );
    }
}
