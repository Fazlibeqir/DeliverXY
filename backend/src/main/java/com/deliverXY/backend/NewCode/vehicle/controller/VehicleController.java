package com.deliverXY.backend.NewCode.vehicle.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.vehicle.dto.VehicleRequestDTO;
import com.deliverXY.backend.NewCode.vehicle.dto.VehicleResponseDTO;
import com.deliverXY.backend.NewCode.vehicle.service.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService service;

    @PostMapping
    public ApiResponse<VehicleResponseDTO> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody VehicleRequestDTO dto
    ) {
        return ApiResponse.ok(service.create(principal.getUser().getId(), dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<VehicleResponseDTO> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequestDTO dto
    ) {
        return ApiResponse.ok(service.update(principal.getUser().getId(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        service.delete(principal.getUser().getId(), id);
        return ApiResponse.ok("Vehicle deleted");
    }

    @GetMapping("/{id}")
    public ApiResponse<VehicleResponseDTO> find(@PathVariable Long id) {
        return ApiResponse.ok(service.getById(id));
    }

    @GetMapping("/my")
    public ApiResponse<?> myVehicles(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(service.listMyVehicles(principal.getUser().getId()));
    }
}
