package com.deliverXY.backend.NewCode.deliveries.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryResponseDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.FareEstimateDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.FareResponseDTO;
import com.deliverXY.backend.NewCode.deliveries.service.DeliveryService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping
    public ApiResponse<Page<DeliveryResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(deliveryService.getAllDeliveries(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ApiResponse<DeliveryResponseDTO> getById(@PathVariable Long id) {
        return ApiResponse.ok(deliveryService.getDeliveryById(id));
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<DeliveryResponseDTO>> getByStatus(@PathVariable String status) {
        return ApiResponse.ok(deliveryService.getByStatus(status));
    }

    @GetMapping("/client/{clientId}")
    public ApiResponse<List<DeliveryResponseDTO>> getByClient(@PathVariable Long clientId) {
        // NOTE: Admin/internal endpoint. Normal users should use /me/deliveries.
        return ApiResponse.ok(deliveryService.getByClient(clientId));
    }

    @GetMapping("/agent/{agentId}")
    public ApiResponse<List<DeliveryResponseDTO>> getByAgent(@PathVariable Long agentId) {
        // NOTE: Admin/internal endpoint. Normal agents should use /me/deliveries.
        return ApiResponse.ok(deliveryService.getByAgent(agentId));
    }

    @GetMapping("/nearby")
    public ApiResponse<List<DeliveryResponseDTO>> findNearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radius
    ) {
        return ApiResponse.ok(deliveryService.findNearby(latitude, longitude, radius));
    }

    @PostMapping
    // We use @ResponseStatus(HttpStatus.CREATED) instead of ResponseEntity to signal 201
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DeliveryResponseDTO> create(
            @Valid @RequestBody DeliveryDTO deliveryDTO,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // We use ApiResponse.ok(data) which returns status 200,
        // but @ResponseStatus(CREATED) overrides the HTTP status to 201.
        return ApiResponse.ok(deliveryService.create(deliveryDTO, principal.getUser()));
    }

    @PutMapping("/{id}")
    public ApiResponse<DeliveryResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryDTO deliveryDTO
    ) {
        return ApiResponse.ok(deliveryService.update(id, deliveryDTO));
    }

    @PostMapping("/{id}/assign")
    public ApiResponse<DeliveryResponseDTO> assignDelivery(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal agentPrincipal
    ) {
        return ApiResponse.ok(deliveryService.assign(id, agentPrincipal.getUser()));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<DeliveryResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ApiResponse.ok(deliveryService.updateStatus(id, status));
    }

    // REMOVED: updateLocation endpoint (see removal section below)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Signals 204 No Content
    public ApiResponse<Void> deleteDelivery(@PathVariable Long id) {
        deliveryService.delete(id);
        // Return ApiResponse.ok(null) or a specific empty response for 204
        return ApiResponse.ok(null);
    }

    // --- FARE ESTIMATE ---
    @PostMapping("/estimate-fare")
    public ApiResponse<FareResponseDTO> estimateFare(
            @Valid @RequestBody FareEstimateDTO request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(deliveryService.estimateFare(request, principal.getUser()));
    }
}