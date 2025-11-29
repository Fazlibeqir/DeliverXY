package com.deliverXY.backend.NewCode.deliveries.service;

import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryResponseDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.FareEstimateDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.FareResponseDTO;
import com.deliverXY.backend.NewCode.user.domain.AppUser;

import java.util.List;

public interface DeliveryService {
    List<DeliveryResponseDTO> getAllDeliveries();
    DeliveryResponseDTO getDeliveryById(Long id);

    List<DeliveryResponseDTO> getByStatus(String status);
    List<DeliveryResponseDTO> getByClient(Long clientId);
    List<DeliveryResponseDTO> getByAgent(Long agentId);
    List<DeliveryResponseDTO> findNearby(Double lat, Double lng, Double radiusKm);

    DeliveryResponseDTO create(DeliveryDTO dto, AppUser client);
    DeliveryResponseDTO update(Long id, DeliveryDTO dto);

    DeliveryResponseDTO assign(Long id, AppUser agent);
    DeliveryResponseDTO updateStatus(Long id, String status);
    DeliveryResponseDTO updateLocation(Long id, Double lat, Double lng);

    void delete(Long id);

    FareResponseDTO estimateFare(FareEstimateDTO dto, AppUser user);
}