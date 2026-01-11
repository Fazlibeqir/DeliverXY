package com.deliverXY.backend.NewCode.deliveries.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.deliveries.domain.DeliveryTracking;
import com.deliverXY.backend.NewCode.deliveries.service.DeliveryTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class DeliveryTrackingController {

    private final DeliveryTrackingService service;

    @PostMapping("/{deliveryId}/update")
    public ApiResponse<DeliveryTracking> update(
            @PathVariable Long deliveryId,
            @RequestBody Map<String, Double> body
    ) {
        return ApiResponse.ok(
                service.updateLocation(
                        deliveryId,
                        body.get("lat"),
                        body.get("lon")
                )
        );
    }

    @GetMapping("/{deliveryId}")
    public ApiResponse<DeliveryTracking> get(@PathVariable Long deliveryId) {
        return ApiResponse.ok(service.getTracking(deliveryId));
    }
}

