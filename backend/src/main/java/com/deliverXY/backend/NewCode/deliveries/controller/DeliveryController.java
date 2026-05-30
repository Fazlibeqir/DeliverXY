package com.deliverXY.backend.NewCode.deliveries.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryResponseDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.FareEstimateDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.FareResponseDTO;
import com.deliverXY.backend.NewCode.deliveries.service.DeliveryService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DeliveryController {

    private final DeliveryService deliveryService;

    private static @NonNull AppUser requireUser(@NonNull UserPrincipal principal) {
        return Objects.requireNonNull(principal.getUser(), "Authenticated user is required");
    }

    private static @NonNull Long requireUserId(@NonNull UserPrincipal principal) {
        return Objects.requireNonNull(requireUser(principal).getId(), "User id is required");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<DeliveryResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(deliveryService.getAllDeliveries(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DeliveryResponseDTO> getById(@PathVariable @NonNull Long id) {
        return ApiResponse.ok(deliveryService.getDeliveryById(id));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<DeliveryResponseDTO>> getByStatus(@PathVariable @NonNull String status) {
        return ApiResponse.ok(deliveryService.getByStatus(status));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<DeliveryResponseDTO>> getByClient(@PathVariable @NonNull Long clientId) {
        // NOTE: Admin/internal endpoint. Normal users should use /me/deliveries.
        return ApiResponse.ok(deliveryService.getByClient(clientId));
    }
    @GetMapping("/mine")
    public ApiResponse<List<DeliveryResponseDTO>> myDeliveries(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(deliveryService.getByClient(principal.getUser().getId()));
    }
    @GetMapping("/assigned")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<List<DeliveryResponseDTO>> assignedToMe(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(deliveryService.getByAgent(principal.getUser().getId()));
    }
    @GetMapping("/active")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<DeliveryResponseDTO> activeDelivery(
            @AuthenticationPrincipal @NonNull UserPrincipal principal
    ) {
        return ApiResponse.ok(deliveryService.getActiveDelivery(requireUserId(principal)));
    }




    @GetMapping("/agent/{agentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<DeliveryResponseDTO>> getByAgent(@PathVariable @NonNull Long agentId) {
        // NOTE: Admin/internal endpoint. Normal agents should use /me/deliveries.
        return ApiResponse.ok(deliveryService.getByAgent(agentId));
    }

    @GetMapping("/nearby")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<List<DeliveryResponseDTO>> findNearby(
            @RequestParam @NonNull Double latitude,
            @RequestParam @NonNull Double longitude,
            @RequestParam(defaultValue = "5.0") Double radius
    ) {
        return ApiResponse.ok(deliveryService.findNearby(latitude, longitude, radius));
    }

    @PostMapping
    // We use @ResponseStatus(HttpStatus.CREATED) instead of ResponseEntity to signal 201
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DeliveryResponseDTO> create(
            @Valid @RequestBody @NonNull DeliveryDTO deliveryDTO,
            @AuthenticationPrincipal @NonNull UserPrincipal principal
    ) {
        return ApiResponse.ok(deliveryService.create(deliveryDTO, requireUser(principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DeliveryResponseDTO> update(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody @NonNull DeliveryDTO deliveryDTO
    ) {
        return ApiResponse.ok(deliveryService.update(id, deliveryDTO));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<DeliveryResponseDTO> assignDelivery(
            @PathVariable @NonNull Long id,
            @AuthenticationPrincipal @NonNull UserPrincipal agentPrincipal
    ) {
        return ApiResponse.ok(deliveryService.assign(id, requireUser(agentPrincipal)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ApiResponse<DeliveryResponseDTO> updateStatus(
            @PathVariable @NonNull Long id,
            @RequestParam @NonNull String status
    ) {
        return ApiResponse.ok(deliveryService.updateStatus(id, status));
    }

    // REMOVED: updateLocation endpoint (see removal section below)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteDelivery(@PathVariable @NonNull Long id) {
        deliveryService.delete(id);
        // Return ApiResponse.ok(null) or a specific empty response for 204
        return ApiResponse.ok(null);
    }

    // --- FARE ESTIMATE ---
    @PostMapping("/estimate-fare")
    public ApiResponse<FareResponseDTO> estimateFare(
            @Valid @RequestBody @NonNull FareEstimateDTO request,
            @AuthenticationPrincipal @NonNull UserPrincipal principal
    ) {
        return ApiResponse.ok(deliveryService.estimateFare(request, requireUser(principal)));
    }
}