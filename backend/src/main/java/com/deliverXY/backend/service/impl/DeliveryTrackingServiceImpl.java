package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.service.DeliveryTrackingService;
import com.deliverXY.backend.service.DeliveryService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DeliveryTrackingServiceImpl implements DeliveryTrackingService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final DeliveryService deliveryService;
    private final Map<Long, Boolean> activeTracking = new HashMap<>();
    
    public DeliveryTrackingServiceImpl(SimpMessagingTemplate messagingTemplate, 
                                     DeliveryService deliveryService) {
        this.messagingTemplate = messagingTemplate;
        this.deliveryService = deliveryService;
    }
    
    @Override
    public void updateDeliveryLocation(Long deliveryId, Double latitude, Double longitude) {
        // Update delivery location in database
        deliveryService.updateLocation(deliveryId, latitude, longitude);
        
        // Notify client about location update
        Map<String, Object> locationUpdate = new HashMap<>();
        locationUpdate.put("deliveryId", deliveryId);
        locationUpdate.put("latitude", latitude);
        locationUpdate.put("longitude", longitude);
        locationUpdate.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/delivery/" + deliveryId + "/location", locationUpdate);
    }
    
    @Override
    public void notifyDeliveryStatusChange(Long deliveryId, String status) {
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("deliveryId", deliveryId);
        statusUpdate.put("status", status);
        statusUpdate.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/delivery/" + deliveryId + "/status", statusUpdate);
    }
    
    @Override
    public void notifyClientOfAgentLocation(Long deliveryId, Double latitude, Double longitude) {
        Map<String, Object> agentLocation = new HashMap<>();
        agentLocation.put("deliveryId", deliveryId);
        agentLocation.put("agentLatitude", latitude);
        agentLocation.put("agentLongitude", longitude);
        agentLocation.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/delivery/" + deliveryId + "/agent-location", agentLocation);
    }
    
    @Override
    public void notifyAgentOfNewDelivery(Delivery delivery) {
        Map<String, Object> newDelivery = new HashMap<>();
        newDelivery.put("deliveryId", delivery.getId());
        newDelivery.put("title", delivery.getTitle());
        newDelivery.put("pickupAddress", delivery.getPickupAddress());
        newDelivery.put("dropoffAddress", delivery.getDropoffAddress());
        newDelivery.put("basePrice", delivery.getBasePrice());
        newDelivery.put("timestamp", System.currentTimeMillis());
        
        // Notify all available agents in the area
        messagingTemplate.convertAndSend("/topic/agents/new-delivery", newDelivery);
    }
    
    @Override
    public void startTracking(Long deliveryId) {
        activeTracking.put(deliveryId, true);
        
        Map<String, Object> trackingStart = new HashMap<>();
        trackingStart.put("deliveryId", deliveryId);
        trackingStart.put("message", "Tracking started");
        trackingStart.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/delivery/" + deliveryId + "/tracking", trackingStart);
    }
    
    @Override
    public void stopTracking(Long deliveryId) {
        activeTracking.put(deliveryId, false);
        
        Map<String, Object> trackingStop = new HashMap<>();
        trackingStop.put("deliveryId", deliveryId);
        trackingStop.put("message", "Tracking stopped");
        trackingStop.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/delivery/" + deliveryId + "/tracking", trackingStop);
    }
} 