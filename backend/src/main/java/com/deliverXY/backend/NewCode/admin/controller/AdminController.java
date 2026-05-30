package com.deliverXY.backend.NewCode.admin.controller;

import com.deliverXY.backend.NewCode.admin.dto.AdminDashboardDTO;
import com.deliverXY.backend.NewCode.admin.dto.AssignDeliveryDTO;
import com.deliverXY.backend.NewCode.admin.dto.RejectKycRequest;
import jakarta.validation.Valid;
import com.deliverXY.backend.NewCode.admin.service.AdminEarningsService;
import com.deliverXY.backend.NewCode.admin.service.AdminKycPresentationService;
import com.deliverXY.backend.NewCode.admin.service.AdminService;
import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.common.util.RequestPayloadValidator;
import com.deliverXY.backend.NewCode.kyc.service.AppUserKYCService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminEarningsService adminEarningsService;
    private final AppUserKYCService kycService;
    private final AdminKycPresentationService adminKycPresentationService;

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
            @PathVariable @NonNull Long deliveryId,
            @Valid @RequestBody @NonNull AssignDeliveryDTO request) {
        AssignDeliveryDTO body = RequestPayloadValidator.requireBody(request);
        adminService.assignDelivery(Objects.requireNonNull(deliveryId, "deliveryId"), Objects.requireNonNull(body.getAgentId(), "agentId"));
        return ApiResponse.ok("Delivery assigned");
    }
    @GetMapping("/earnings")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> getEarnings() {
        return ApiResponse.ok(adminEarningsService.getEarnings());
    }

    @GetMapping("/kyc/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> getKYC(@PathVariable @NonNull Long userId) {
        return ApiResponse.ok(adminKycPresentationService.getKycInfo(Objects.requireNonNull(userId, "userId")));
    }

    @PostMapping("/kyc/{userId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> approveKYC(
            @PathVariable @NonNull Long userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        String reviewer = principal != null && principal.getUser() != null 
            ? principal.getUser().getEmail() + " (" + principal.getUser().getUsername() + ")"
            : "ADMIN";
        return ApiResponse.ok(kycService.approveKYC(Objects.requireNonNull(userId, "userId"), reviewer));
    }

    @PostMapping("/kyc/{userId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> rejectKYC(
            @PathVariable @NonNull Long userId,
            @Valid @RequestBody @NonNull RejectKycRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        String reviewer = principal != null && principal.getUser() != null 
            ? principal.getUser().getEmail() + " (" + principal.getUser().getUsername() + ")"
            : "ADMIN";
        RejectKycRequest body = RequestPayloadValidator.requireBody(request);
        return ApiResponse.ok(kycService.rejectKYC(
                Objects.requireNonNull(userId, "userId"),
                RequestPayloadValidator.requireText(body.getReason(), "reason"),
                reviewer
        ));
    }

    @GetMapping("/drivers/locations")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> getAllDriverLocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ApiResponse.ok(adminService.getAllDriverLocations(PageRequest.of(page, size)));
    }
    //TODO : FUTURE POST /api/admin/deliveries/{id}/refund
    //TODO : FUTURE POST /api/admin/deliveries/{id}/cancel
    //TODO: FUTURE search/filter users
    //TODO: FUTURE search/filter deliveries
}
