package com.deliverXY.backend.service;

import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.AppUser;

public interface DeliveryTrackingService {
    void updateDeliveryLocation(Long deliveryId, Double latitude, Double longitude);
    void notifyDeliveryStatusChange(Long deliveryId, String status);
    void notifyClientOfAgentLocation(Long deliveryId, Double latitude, Double longitude);
    void notifyAgentOfNewDelivery(Delivery delivery);
    void startTracking(Long deliveryId);
    void stopTracking(Long deliveryId);
} 