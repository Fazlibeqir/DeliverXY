package com.deliverXY.backend.NewCode.deliveries.service;

import com.deliverXY.backend.NewCode.deliveries.domain.DeliveryTracking;
import org.springframework.lang.NonNull;

public interface DeliveryTrackingService {
    DeliveryTracking updateLocation(@NonNull Long deliveryId, Double lat, Double lon);

    DeliveryTracking getTracking(@NonNull Long deliveryId);
}
