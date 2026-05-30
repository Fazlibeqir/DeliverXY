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
import org.springframework.lang.NonNull;

import java.util.List;

public interface DeliveryService {
    Page<DeliveryResponseDTO> getAllDeliveries(@NonNull Pageable pageable);

    DeliveryResponseDTO getDeliveryById(@NonNull Long id);

    List<DeliveryResponseDTO> getByStatus(String status);

    List<DeliveryResponseDTO> getByClient(Long clientId);

    List<DeliveryResponseDTO> getByAgent(Long agentId);

    List<DeliveryResponseDTO> findNearby(Double lat, Double lng, Double radiusKm);

    DeliveryResponseDTO create(@NonNull DeliveryDTO dto, @NonNull AppUser client);

    DeliveryResponseDTO update(@NonNull Long id, @NonNull DeliveryDTO dto);

    DeliveryResponseDTO assign(@NonNull Long id, @NonNull AppUser agent);

    DeliveryResponseDTO updateStatus(@NonNull Long id, @NonNull String status);

    void delete(@NonNull Long id);

    long countAll();

    long countByStatus(DeliveryStatus deliveryStatus);

    FareResponseDTO estimateFare(@Valid @NonNull FareEstimateDTO request, @NonNull AppUser user);

    DeliveryResponseDTO getActiveDelivery(@NonNull Long agentId);
}
