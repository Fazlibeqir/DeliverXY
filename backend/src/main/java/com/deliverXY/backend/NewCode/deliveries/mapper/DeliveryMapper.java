package com.deliverXY.backend.NewCode.deliveries.mapper;

import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class DeliveryMapper {

    /*
     ───────────────────────────────────────────────────────────────
     • Convert Delivery entity → DeliveryResponseDTO
     ───────────────────────────────────────────────────────────────
     */
    public DeliveryResponseDTO toResponse(Delivery delivery) {

        if (delivery == null) return null;

        DeliveryResponseDTO dto = new DeliveryResponseDTO();

        // Basic
        dto.setId(delivery.getId());
        dto.setTitle(delivery.getTitle());
        dto.setDescription(delivery.getDescription());
        dto.setPackageType(delivery.getPackageType());
        dto.setPackageWeight(delivery.getPackageWeight());
        dto.setPackageDimensions(delivery.getPackageDimensions());

        // Pickup
        dto.setPickupAddress(delivery.getPickupAddress());
        dto.setPickupLatitude(delivery.getPickupLatitude());
        dto.setPickupLongitude(delivery.getPickupLongitude());
        dto.setPickupContactName(delivery.getPickupContactName());
        dto.setPickupContactPhone(delivery.getPickupContactPhone());
        dto.setPickupInstructions(delivery.getPickupInstructions());

        // Dropoff
        dto.setDropoffAddress(delivery.getDropoffAddress());
        dto.setDropoffLatitude(delivery.getDropoffLatitude());
        dto.setDropoffLongitude(delivery.getDropoffLongitude());
        dto.setDropoffContactName(delivery.getDropoffContactName());
        dto.setDropoffContactPhone(delivery.getDropoffContactPhone());
        dto.setDropoffInstructions(delivery.getDropoffInstructions());

        // Timing
        dto.setRequestedPickupTime(delivery.getRequestedPickupTime());
        dto.setRequestedDeliveryTime(delivery.getRequestedDeliveryTime());
        dto.setActualPickupTime(delivery.getActualPickupTime());
        dto.setActualDeliveryTime(delivery.getActualDeliveryTime());
        dto.setExpiresAt(delivery.getExpiresAt());

        // Status
        dto.setStatus(delivery.getStatus());
        dto.setAssignedAt(delivery.getAssignedAt());

        // Client
        if (delivery.getClient() != null) {
            AppUser c = delivery.getClient();
            dto.setClientId(c.getId());
            dto.setClientUsername(c.getUsername());
            dto.setClientEmail(c.getEmail());
        }

        // Agent
        if (delivery.getAgent() != null) {
            AppUser a = delivery.getAgent();
            dto.setAgentId(a.getId());
            dto.setAgentUsername(a.getUsername());
            dto.setAgentEmail(a.getEmail());
        }

        // Meta
        dto.setCreatedAt(delivery.getCreatedAt());
        dto.setUpdatedAt(delivery.getUpdatedAt());
        dto.setCancelledAt(delivery.getCancelledAt());
        dto.setCancellationReason(delivery.getCancellationReason());
        dto.setCancelledBy(delivery.getCancelledBy());

        return dto;
    }


    /*
     ───────────────────────────────────────────────────────────────
     • Update entity fields from DTO
     • Safely handles nulls AND allows clearing fields
     • Service will recalc fees after this
     ───────────────────────────────────────────────────────────────
     */
    public void updateEntityFromDTO(Delivery delivery, DeliveryDTO dto) {

        // Basic
        delivery.setTitle(dto.getTitle());
        delivery.setDescription(dto.getDescription());
        delivery.setPackageType(dto.getPackageType());
        delivery.setPackageWeight(dto.getPackageWeight());
        delivery.setPackageDimensions(dto.getPackageDimensions());

        // Pickup
        delivery.setPickupAddress(dto.getPickupAddress());
        delivery.setPickupLatitude(dto.getPickupLatitude());
        delivery.setPickupLongitude(dto.getPickupLongitude());
        delivery.setPickupContactName(dto.getPickupContactName());
        delivery.setPickupContactPhone(dto.getPickupContactPhone());
        delivery.setPickupInstructions(dto.getPickupInstructions());

        // Dropoff
        delivery.setDropoffAddress(dto.getDropoffAddress());
        delivery.setDropoffLatitude(dto.getDropoffLatitude());
        delivery.setDropoffLongitude(dto.getDropoffLongitude());
        delivery.setDropoffContactName(dto.getDropoffContactName());
        delivery.setDropoffContactPhone(dto.getDropoffContactPhone());
        delivery.setDropoffInstructions(dto.getDropoffInstructions());

        // Timing
        delivery.setRequestedPickupTime(dto.getRequestedPickupTime());
        delivery.setRequestedDeliveryTime(dto.getRequestedDeliveryTime());
    }
}
