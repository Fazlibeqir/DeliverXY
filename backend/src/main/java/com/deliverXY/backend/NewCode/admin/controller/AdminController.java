package com.deliverXY.backend.NewCode.admin.controller;

import com.deliverXY.backend.NewCode.admin.dto.AdminDashboardDTO;
import com.deliverXY.backend.NewCode.admin.dto.AssignDeliveryDTO;
import com.deliverXY.backend.NewCode.admin.service.AdminEarningsService;
import com.deliverXY.backend.NewCode.admin.service.AdminService;
import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.kyc.service.AppUserKYCService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminEarningsService adminEarningsService;
    private final AppUserKYCService kycService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminDashboardDTO> getDashboard() {
        return ApiResponse.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> getUsers( @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(adminService.getAllUsers(PageRequest.of(page, size)));
    }





    @PostMapping("/users/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> blockUser(@PathVariable Long id) {
        adminService.blockUser(id);
        return ApiResponse.ok("User blocked");
    }

    @PostMapping("/users/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> unblockUser(@PathVariable Long id) {
        adminService.unblockUser(id);
        return ApiResponse.ok("User unblocked");
    }

    @GetMapping("/deliveries")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> getDeliveries(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(adminService.getAllDeliveries(PageRequest.of(page, size)));
    }

    @PostMapping("/deliveries/{deliveryId}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> assignDelivery(
            @PathVariable Long deliveryId,
            @RequestBody AssignDeliveryDTO request) {

        adminService.assignDelivery(deliveryId, request.getAgentId());
        return ApiResponse.ok("Delivery assigned");
    }
    @GetMapping("/earnings")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> getEarnings() {
        return ApiResponse.ok(adminEarningsService.getEarnings());
    }

    @PostMapping("/kyc/{userId}/approve")
    public ApiResponse<?> approveKYC(@PathVariable Long userId) {
        return ApiResponse.ok(kycService.approveKYC(userId, "ADMIN"));
    }

    @PostMapping("/kyc/{userId}/reject")
    public ApiResponse<?> rejectKYC(
            @PathVariable Long userId,
            @RequestBody String reason
    ) {
        return ApiResponse.ok(kycService.rejectKYC(userId, reason));
    }
    //TODO : FUTURE POST /api/admin/deliveries/{id}/refund
    //TODO : FUTURE POST /api/admin/deliveries/{id}/cancel
    //TODO: FUTURE search/filter users
    //TODO: FUTURE search/filter deliveries
}
