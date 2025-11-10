package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.enums.DeliveryStatus;
import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.dto.DeliveryDTO;
import com.deliverXY.backend.repositories.DeliveryRepository;
import com.deliverXY.backend.service.DeliveryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;

    public DeliveryServiceImpl(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @Override
    public Delivery findById(Long id) {
        return deliveryRepository.findById(id).orElse(null);
    }

    @Override
    public List<Delivery> findAll() {
        return deliveryRepository.findAll();
    }

    @Override
    public List<Delivery> findByStatus(String status) {
        return deliveryRepository.findByStatus(DeliveryStatus.valueOf(status.toUpperCase()));
    }

    @Override
    public List<Delivery> findByClient(AppUser client) {
        return deliveryRepository.findByClient(client);
    }

    @Override
    public List<Delivery> findByAgent(AppUser agent) {
        return deliveryRepository.findByAgent(agent);
    }

    @Override
    public List<Delivery> findNearbyDeliveries(Double latitude, Double longitude, Double radius) {
        // This would typically use a spatial query or calculate distances
        // For now, return all pending deliveries
        return deliveryRepository.findByStatus(DeliveryStatus.REQUESTED);
    }

    @Override
    @Transactional
    public Delivery create(DeliveryDTO deliveryDTO, AppUser client) {
        Delivery delivery = new Delivery(
            deliveryDTO.getTitle(),
            deliveryDTO.getDescription(),
            deliveryDTO.getPickupAddress(),
            deliveryDTO.getDropoffAddress(),
            deliveryDTO.getBasePrice(),
            client
        );

        // Set additional fields
        if (deliveryDTO.getPackageType() != null) {
            delivery.setPackageType(deliveryDTO.getPackageType());
        }
        if (deliveryDTO.getPackageWeight() != null) {
            delivery.setPackageWeight(deliveryDTO.getPackageWeight());
        }
        if (deliveryDTO.getPackageDimensions() != null) {
            delivery.setPackageDimensions(deliveryDTO.getPackageDimensions());
        }
        if (deliveryDTO.getIsFragile() != null) {
            delivery.setIsFragile(deliveryDTO.getIsFragile());
        }
        if (deliveryDTO.getIsUrgent() != null) {
            delivery.setIsUrgent(deliveryDTO.getIsUrgent());
        }
        if (deliveryDTO.getPickupLatitude() != null) {
            delivery.setPickupLatitude(deliveryDTO.getPickupLatitude());
        }
        if (deliveryDTO.getPickupLongitude() != null) {
            delivery.setPickupLongitude(deliveryDTO.getPickupLongitude());
        }
        if (deliveryDTO.getPickupContactName() != null) {
            delivery.setPickupContactName(deliveryDTO.getPickupContactName());
        }
        if (deliveryDTO.getPickupContactPhone() != null) {
            delivery.setPickupContactPhone(deliveryDTO.getPickupContactPhone());
        }
        if (deliveryDTO.getPickupInstructions() != null) {
            delivery.setPickupInstructions(deliveryDTO.getPickupInstructions());
        }
        if (deliveryDTO.getDropoffLatitude() != null) {
            delivery.setDropoffLatitude(deliveryDTO.getDropoffLatitude());
        }
        if (deliveryDTO.getDropoffLongitude() != null) {
            delivery.setDropoffLongitude(deliveryDTO.getDropoffLongitude());
        }
        if (deliveryDTO.getDropoffContactName() != null) {
            delivery.setDropoffContactName(deliveryDTO.getDropoffContactName());
        }
        if (deliveryDTO.getDropoffContactPhone() != null) {
            delivery.setDropoffContactPhone(deliveryDTO.getDropoffContactPhone());
        }
        if (deliveryDTO.getDropoffInstructions() != null) {
            delivery.setDropoffInstructions(deliveryDTO.getDropoffInstructions());
        }
        if (deliveryDTO.getRequestedPickupTime() != null) {
            delivery.setRequestedPickupTime(deliveryDTO.getRequestedPickupTime());
        }
        if (deliveryDTO.getRequestedDeliveryTime() != null) {
            delivery.setRequestedDeliveryTime(deliveryDTO.getRequestedDeliveryTime());
        }
        if (deliveryDTO.getIsInsured() != null && deliveryDTO.getIsInsured()) {
            delivery.setIsInsured(true);
            if (deliveryDTO.getInsuranceAmount() != null) {
                delivery.setInsuranceAmount(deliveryDTO.getInsuranceAmount());
            }
        }

        // Calculate fees and total price
        calculateFees(delivery);

        return deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public Delivery update(Long id, DeliveryDTO deliveryDTO) {
        Delivery delivery = findById(id);
        if (delivery == null) {
            throw new RuntimeException("Delivery not found");
        }

        // Update fields if provided
        if (deliveryDTO.getTitle() != null) {
            delivery.setTitle(deliveryDTO.getTitle());
        }
        if (deliveryDTO.getDescription() != null) {
            delivery.setDescription(deliveryDTO.getDescription());
        }
        if (deliveryDTO.getPackageType() != null) {
            delivery.setPackageType(deliveryDTO.getPackageType());
        }
        if (deliveryDTO.getPackageWeight() != null) {
            delivery.setPackageWeight(deliveryDTO.getPackageWeight());
        }
        if (deliveryDTO.getPackageDimensions() != null) {
            delivery.setPackageDimensions(deliveryDTO.getPackageDimensions());
        }
        if (deliveryDTO.getIsFragile() != null) {
            delivery.setIsFragile(deliveryDTO.getIsFragile());
        }
        if (deliveryDTO.getIsUrgent() != null) {
            delivery.setIsUrgent(deliveryDTO.getIsUrgent());
        }

        // Recalculate fees
        calculateFees(delivery);

        return deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public Delivery assignToAgent(Long deliveryId, AppUser agent) {
        Delivery delivery = findById(deliveryId);
        if (delivery == null) {
            throw new RuntimeException("Delivery not found");
        }

        if (!canAgentAcceptDelivery(deliveryId, agent)) {
            throw new RuntimeException("Agent cannot accept this delivery");
        }

        delivery.setAgent(agent);
        delivery.setStatus(DeliveryStatus.DRIVER_ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());

        return deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public Delivery updateStatus(Long deliveryId, String status) {
        Delivery delivery = findById(deliveryId);
        if (delivery == null) {
            throw new RuntimeException("Delivery not found");
        }

        DeliveryStatus newStatus = DeliveryStatus.valueOf(status.toUpperCase());
        delivery.setStatus(newStatus);

        // Update timestamps based on status
        switch (newStatus) {
            case PICKED_UP:
                delivery.setActualPickupTime(LocalDateTime.now());
                break;
            case DELIVERED:
                delivery.setActualDeliveryTime(LocalDateTime.now());
                break;
            case CANCELLED:
                delivery.setCancelledAt(LocalDateTime.now());
                break;
        }

        return deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public Delivery updateLocation(Long deliveryId, Double latitude, Double longitude) {
        Delivery delivery = findById(deliveryId);
        if (delivery == null) {
            throw new RuntimeException("Delivery not found");
        }

        delivery.setCurrentLatitude(latitude);
        delivery.setCurrentLongitude(longitude);
        delivery.setLastLocationUpdate(LocalDateTime.now());

        return deliveryRepository.save(delivery);
    }

    @Override
    public void deleteById(Long id) {
        deliveryRepository.deleteById(id);
    }

    @Override
    public boolean canAgentAcceptDelivery(Long deliveryId, AppUser agent) {
        Delivery delivery = findById(deliveryId);
        if (delivery == null) {
            return false;
        }

        // Check if delivery is available
        if (delivery.getStatus() != DeliveryStatus.REQUESTED) {
            return false;
        }

        // Check if agent is verified
        if (!agent.getIsVerified()) {
            return false;
        }

        // Check if agent is available
        if (!agent.getIsAvailable()) {
            return false;
        }

        // Check if agent has good rating (optional)
        return !(agent.getRating() < 3.0);
    }

    private void calculateFees(Delivery delivery) {
        BigDecimal totalPrice = delivery.getBasePrice();

        // Add urgent fee
        if (delivery.getIsUrgent()) {
            delivery.setUrgentFee(delivery.getBasePrice().multiply(new BigDecimal("0.2"))); // 20% urgent fee
            totalPrice = totalPrice.add(delivery.getUrgentFee());
        }

        // Add fragile fee
        if (delivery.getIsFragile()) {
            delivery.setFragileFee(delivery.getBasePrice().multiply(new BigDecimal("0.15"))); // 15% fragile fee
            totalPrice = totalPrice.add(delivery.getFragileFee());
        }

        // Add insurance premium
        if (delivery.getIsInsured() && delivery.getInsuranceAmount() != null) {
            delivery.setInsurancePremium(delivery.getInsuranceAmount().multiply(new BigDecimal("0.05"))); // 5% insurance premium
            totalPrice = totalPrice.add(delivery.getInsurancePremium());
        }

        delivery.setTotalPrice(totalPrice);
        delivery.setFinalAmount(totalPrice.add(delivery.getTipAmount()));
    }
}
