package com.deliverXY.backend.NewCode.deliveries.service;


import com.deliverXY.backend.NewCode.deliveries.domain.DeliveryTracking;

public interface DeliveryTrackingService {
    DeliveryTracking updateLocation(Long deliveryId, Double lat, Double lon);
    DeliveryTracking getTracking(Long deliveryId);
} 