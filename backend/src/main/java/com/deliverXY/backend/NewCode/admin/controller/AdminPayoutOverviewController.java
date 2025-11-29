package com.deliverXY.backend.NewCode.admin.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.earnings.repository.DriverPayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/payouts")
@RequiredArgsConstructor
public class AdminPayoutOverviewController {

    private final DriverPayoutRepository payoutRepo;

    @GetMapping
    public ApiResponse<?> listAllPayouts() {
        return ApiResponse.ok(
                payoutRepo.findAll()
        );
    }
}
