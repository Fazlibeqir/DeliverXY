package com.deliverXY.backend.NewCode.admin.controller;

import com.deliverXY.backend.NewCode.admin.dto.AdminDashboardDTO;
import com.deliverXY.backend.NewCode.admin.dto.AssignDeliveryDTO;
import com.deliverXY.backend.NewCode.admin.service.AdminService;
import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminDashboardDTO> getDashboard() {
        return ApiResponse.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> getUsers() {
        return ApiResponse.ok(adminService.getAllUsers());
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
    public ApiResponse<?> getDeliveries() {
        return ApiResponse.ok(adminService.getAllDeliveries());
    }

    @PostMapping("/deliveries/{deliveryId}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> assignDelivery(
            @PathVariable Long deliveryId,
            @RequestBody AssignDeliveryDTO request) {

        adminService.assignDelivery(deliveryId, request.getAgentId());
        return ApiResponse.ok("Delivery assigned");
    }
}
