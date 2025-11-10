package com.deliverXY.backend.service;

import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.dto.DeliveryDTO;
import com.deliverXY.backend.models.AppUser;

import java.util.List;

public interface DeliveryService {
    Delivery findById(Long id);
    List<Delivery> findAll();
    List<Delivery> findByStatus(String status);
    List<Delivery> findByClient(AppUser client);
    List<Delivery> findByAgent(AppUser agent);
    List<Delivery> findNearbyDeliveries(Double latitude, Double longitude, Double radius);
    Delivery create(DeliveryDTO deliveryDTO, AppUser client);
    Delivery update(Long id, DeliveryDTO deliveryDTO);
    Delivery assignToAgent(Long deliveryId, AppUser agent);
    Delivery updateStatus(Long deliveryId, String status);
    Delivery updateLocation(Long deliveryId, Double latitude, Double longitude);
    void deleteById(Long id);
    boolean canAgentAcceptDelivery(Long deliveryId, AppUser agent);
}
