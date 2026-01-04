package com.deliverXY.backend.NewCode.deliveries.service;

import com.deliverXY.backend.NewCode.common.enums.DeliveryStatus;
import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryResponseDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.FareEstimateDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.FareResponseDTO;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DeliveryService {
    Page<DeliveryResponseDTO> getAllDeliveries(Pageable pageable);
    DeliveryResponseDTO getDeliveryById(Long id);

    List<DeliveryResponseDTO> getByStatus(String status);
    List<DeliveryResponseDTO> getByClient(Long clientId);
    List<DeliveryResponseDTO> getByAgent(Long agentId);
    List<DeliveryResponseDTO> findNearby(Double lat, Double lng, Double radiusKm);

    DeliveryResponseDTO create(DeliveryDTO dto, AppUser client);
    DeliveryResponseDTO update(Long id, DeliveryDTO dto);

    DeliveryResponseDTO assign(Long id, AppUser agent);
    DeliveryResponseDTO updateStatus(Long id, String status);

    void delete(Long id);


    long countAll();

    long countByStatus(DeliveryStatus deliveryStatus);

    FareResponseDTO estimateFare(@Valid FareEstimateDTO request, AppUser user);

    FareResponseDTO getFareForDelivery(Long deliveryId);

    DeliveryResponseDTO getActiveDelivery(Long agentId);
}