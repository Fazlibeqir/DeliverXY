package com.deliverXY.backend.NewCode.deliveries.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.common.util.RequestPayloadValidator;
import com.deliverXY.backend.NewCode.deliveries.domain.DeliveryTracking;
import com.deliverXY.backend.NewCode.deliveries.dto.TrackingLocationUpdateDTO;
import com.deliverXY.backend.NewCode.deliveries.service.DeliveryTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DeliveryTrackingController {

    private final DeliveryTrackingService service;

    @PostMapping("/{deliveryId}/update")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<DeliveryTracking> update(
            @PathVariable @NonNull Long deliveryId,
            @Valid @RequestBody TrackingLocationUpdateDTO body
    ) {
        TrackingLocationUpdateDTO payload = RequestPayloadValidator.requireBody(body);
        return ApiResponse.ok(
                service.updateLocation(
                        deliveryId,
                        payload.getLat(),
                        payload.getLon()
                )
        );
    }
}
