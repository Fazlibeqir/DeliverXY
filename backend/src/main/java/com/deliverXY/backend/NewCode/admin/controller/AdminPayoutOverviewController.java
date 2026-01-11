package com.deliverXY.backend.NewCode.admin.controller;

import com.deliverXY.backend.NewCode.admin.dto.AdminPayoutDTO;
import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.earnings.domain.DriverPayout;
import com.deliverXY.backend.NewCode.earnings.repository.DriverPayoutRepository;
import com.deliverXY.backend.NewCode.earnings.service.EarningsService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/payouts")
@RequiredArgsConstructor
public class AdminPayoutOverviewController {

    private final DriverPayoutRepository payoutRepo;
    private final AppUserService userService;
    private final EarningsService earningsService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> listAllPayouts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<DriverPayout> payouts = payoutRepo.findAll(PageRequest.of(page, size));
        
        // Fetch all unique driver IDs
        Map<Long, AppUser> driverMap = new HashMap<>();
        payouts.getContent().forEach(p -> {
            if (p.getDriverId() != null && !driverMap.containsKey(p.getDriverId())) {
                userService.findById(p.getDriverId()).ifPresent(user -> driverMap.put(p.getDriverId(), user));
            }
        });
        
        // Convert to DTOs
        Page<AdminPayoutDTO> dtoPage = payouts.map(p -> {
            AppUser driver = driverMap.get(p.getDriverId());
            String driverName = driver != null 
                ? (driver.getFirstName() != null ? driver.getFirstName() : "") + " " + 
                  (driver.getLastName() != null ? driver.getLastName() : "").trim()
                : "Unknown User";
            if (driverName.trim().isEmpty()) driverName = driver != null ? driver.getEmail() : "Unknown User";
            
            return new AdminPayoutDTO(
                p.getId(),
                p.getDriverId(),
                driverName,
                driver != null ? driver.getEmail() : null,
                p.getAmountPaid(),
                p.getPeriodStart(),
                p.getPeriodEnd(),
                p.getStatus() != null ? p.getStatus().name() : "PENDING",
                p.getPaidAt(),
                p.getTransactionRef(),
                p.getProcessedBy()
            );
        });
        
        return ApiResponse.ok(dtoPage);
    }

    @PostMapping("/{payoutId}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> processPayout(
            @PathVariable Long payoutId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        String transactionRef = request.get("transactionRef");
        if (transactionRef == null || transactionRef.trim().isEmpty()) {
            transactionRef = "ADMIN-" + payoutId + "-" + System.currentTimeMillis();
        }
        
        String processedBy = principal.getUser().getEmail() + " (" + principal.getUser().getUsername() + ")";
        
        earningsService.processPayout(payoutId, transactionRef, processedBy);
        
        return ApiResponse.ok("Payout processed successfully");
    }
}
