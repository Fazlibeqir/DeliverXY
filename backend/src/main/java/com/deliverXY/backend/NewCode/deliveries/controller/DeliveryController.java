package com.deliverXY.backend.NewCode.deliveries.controller;

import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryResponseDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.FareEstimateDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.FareResponseDTO;
import com.deliverXY.backend.NewCode.deliveries.service.DeliveryService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping
    public ResponseEntity<List<DeliveryResponseDTO>> getAll() {
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getDeliveryById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeliveryResponseDTO>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(deliveryService.getByStatus(status));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<DeliveryResponseDTO>> getByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(deliveryService.getByClient(clientId));
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<DeliveryResponseDTO>> getByAgent(@PathVariable Long agentId) {
        return ResponseEntity.ok(deliveryService.getByAgent(agentId));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<DeliveryResponseDTO>> findNearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radius
    ) {
        return ResponseEntity.ok(deliveryService.findNearby(latitude, longitude, radius));
    }

    @PostMapping
    public ResponseEntity<DeliveryResponseDTO> create(
            @Valid @RequestBody DeliveryDTO deliveryDTO,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deliveryService.create(deliveryDTO, principal.getUser()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryDTO deliveryDTO
    ) {
        return ResponseEntity.ok(deliveryService.update(id, deliveryDTO));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<DeliveryResponseDTO> assignDelivery(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal agentPrincipal
    ) {
        return ResponseEntity.ok(deliveryService.assign(id, agentPrincipal.getUser()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<DeliveryResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(deliveryService.updateStatus(id, status));
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<DeliveryResponseDTO> updateLocation(
            @PathVariable Long id,
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        return ResponseEntity.ok(deliveryService.updateLocation(id, latitude, longitude));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDelivery(@PathVariable Long id) {
        deliveryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/estimate-fare")
    public ResponseEntity<FareResponseDTO> estimateFare(
            @Valid @RequestBody FareEstimateDTO request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(deliveryService.estimateFare(request, principal.getUser()));
    }
}