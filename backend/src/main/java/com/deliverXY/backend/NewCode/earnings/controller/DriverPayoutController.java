package com.deliverXY.backend.NewCode.earnings.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.earnings.repository.DriverPayoutRepository;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver/payouts")
@RequiredArgsConstructor
public class DriverPayoutController {

    private final DriverPayoutRepository payoutRepo;

    @GetMapping
    public ApiResponse<?> getMyPayouts(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(
                payoutRepo.findByDriverIdOrderByPaidAtDesc(principal.getUser().getId())
        );
    }
}
