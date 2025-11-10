package com.deliverXY.backend.controllers;

import com.deliverXY.backend.enums.PaymentMethod;
import com.deliverXY.backend.exception.ResourceNotFoundException;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.dto.DeliveryDTO;
import com.deliverXY.backend.models.dto.DeliveryResponseDTO;
import com.deliverXY.backend.models.dto.FareEstimateDTO;
import com.deliverXY.backend.models.dto.FareResponseDTO;
import com.deliverXY.backend.service.DeliveryService;
import com.deliverXY.backend.service.AppUserService;
import com.deliverXY.backend.service.impl.PricingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = "*")
@Slf4j
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final AppUserService appUserService;
    private final PricingService pricingService;

    public DeliveryController(DeliveryService deliveryService, AppUserService appUserService, PricingService pricingService) {
        this.deliveryService = deliveryService;
        this.appUserService = appUserService;
        this.pricingService = pricingService;
    }

    @GetMapping
    public ResponseEntity<List<DeliveryResponseDTO>> getAll() {
        List<Delivery> deliveries = deliveryService.findAll();
        List<DeliveryResponseDTO> deliveryDTOs = deliveries.stream()
            .map(DeliveryMapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(deliveryDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponseDTO> getById(@PathVariable Long id) {
        Delivery delivery = deliveryService.findById(id);
        if (delivery == null) {
            throw new ResourceNotFoundException("Delivery", "id", id);
        }
        return ResponseEntity.ok(DeliveryMapper.toDTO(delivery));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeliveryResponseDTO>> getByStatus(@PathVariable String status) {
        List<Delivery> deliveries = deliveryService.findByStatus(status);
        List<DeliveryResponseDTO> dtos = deliveries.stream()
                .map(DeliveryMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<DeliveryResponseDTO>> getByClient(@PathVariable Long clientId) {
        AppUser client = findUserById(clientId);
        List<Delivery> deliveries = deliveryService.findByClient(client);
        List<DeliveryResponseDTO> dtos = deliveries.stream()
                .map(DeliveryMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<DeliveryResponseDTO>> getByAgent(@PathVariable Long agentId) {
        AppUser agent = findUserById(agentId);
        List<Delivery> deliveries = deliveryService.findByAgent(agent);
        List<DeliveryResponseDTO> dtos = deliveries.stream()
                .map(DeliveryMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<DeliveryResponseDTO>> getNearbyDeliveries(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radius) {
        List<Delivery> deliveries = deliveryService.findNearbyDeliveries(latitude, longitude, radius);
        List<DeliveryResponseDTO> dtos = deliveries.stream()
                .map(DeliveryMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<DeliveryResponseDTO> create(@Valid @RequestBody DeliveryDTO deliveryDTO,
                                         @RequestParam Long clientId) {
        AppUser client = findUserById(clientId);
        Delivery delivery = deliveryService.create(deliveryDTO, client);

        log.info("Created delivery {} for client {}", delivery.getId(), clientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(DeliveryMapper.toDTO(delivery));
    }

    @PutMapping("/{id}") 
    public ResponseEntity<DeliveryResponseDTO> update(@PathVariable Long id, @Valid @RequestBody DeliveryDTO deliveryDTO) {
        Delivery delivery = deliveryService.update(id, deliveryDTO);
        log.info("Updated delivery {}", id);
        return ResponseEntity.ok(DeliveryMapper.toDTO(delivery));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<DeliveryResponseDTO> assignToAgent(@PathVariable Long id, @RequestParam Long agentId) {
        AppUser agent = findUserById(agentId);
        Delivery delivery = deliveryService.assignToAgent(id, agent);

        log.info("Assigned delivery {} to agent {}", id, agentId);
        return ResponseEntity.ok(DeliveryMapper.toDTO(delivery));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<DeliveryResponseDTO> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Delivery delivery = deliveryService.updateStatus(id, status);
        log.info("Updated delivery {} status to {}", id, status);
        return ResponseEntity.ok(DeliveryMapper.toDTO(delivery));
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<DeliveryResponseDTO> updateLocation(@PathVariable Long id,
                                                 @RequestParam Double latitude,
                                                 @RequestParam Double longitude) {
        Delivery delivery = deliveryService.updateLocation(id, latitude, longitude);
        log.debug("Updated delivery {} location", id);
        return ResponseEntity.ok(DeliveryMapper.toDTO(delivery));
    }

    @DeleteMapping("/{id}") 
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deliveryService.deleteById(id);
        log.info("Deleted delivery {}", id);
        return ResponseEntity.noContent().build();
    }

   private static class DeliveryMapper {
        static DeliveryResponseDTO toDTO(Delivery delivery) {
            DeliveryResponseDTO dto = new DeliveryResponseDTO();

            // Basic fields
            dto.setId(delivery.getId());
            dto.setTitle(delivery.getTitle());
            dto.setDescription(delivery.getDescription());
            dto.setPackageType(delivery.getPackageType());
            dto.setPackageWeight(delivery.getPackageWeight());
            dto.setPackageDimensions(delivery.getPackageDimensions());
            dto.setIsFragile(delivery.getIsFragile());
            dto.setIsUrgent(delivery.getIsUrgent());

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

            // Users
            if (delivery.getClient() != null) {
                dto.setClientId(delivery.getClient().getId());
                dto.setClientUsername(delivery.getClient().getUsername());
                dto.setClientEmail(delivery.getClient().getEmail());
            }
            if (delivery.getAgent() != null) {
                dto.setAgentId(delivery.getAgent().getId());
                dto.setAgentUsername(delivery.getAgent().getUsername());
                dto.setAgentEmail(delivery.getAgent().getEmail());
            }

            // Payment
            dto.setBasePrice(delivery.getBasePrice());
            dto.setUrgentFee(delivery.getUrgentFee());
            dto.setFragileFee(delivery.getFragileFee());
            dto.setTotalPrice(delivery.getTotalPrice());
            dto.setTipAmount(delivery.getTipAmount());
            dto.setFinalAmount(delivery.getFinalAmount());
            dto.setPaymentStatus(delivery.getPaymentStatus());
            dto.setPaymentMethod(PaymentMethod.valueOf(delivery.getPaymentMethod()));
            dto.setPaidAt(delivery.getPaidAt());

            // Tracking
            dto.setEstimatedDistance(delivery.getEstimatedDistance());
            dto.setEstimatedDuration(delivery.getEstimatedDuration());
            dto.setCurrentLatitude(delivery.getCurrentLatitude());
            dto.setCurrentLongitude(delivery.getCurrentLongitude());
            dto.setLastLocationUpdate(delivery.getLastLocationUpdate());
            dto.setTrackingCode(delivery.getTrackingCode());

            // Insurance
            dto.setIsInsured(delivery.getIsInsured());
            dto.setInsuranceAmount(delivery.getInsuranceAmount());
            dto.setInsurancePremium(delivery.getInsurancePremium());

            // Ratings
            dto.setClientRating(delivery.getClientRating());
            dto.setClientReview(delivery.getClientReview());
            dto.setAgentRating(delivery.getAgentRating());
            dto.setAgentReview(delivery.getAgentReview());
            dto.setReviewedByClient(delivery.getReviewedByClient());
            dto.setReviewedByAgent(delivery.getReviewedByAgent());

            // Metadata
            dto.setCreatedAt(delivery.getCreatedAt());
            dto.setUpdatedAt(delivery.getUpdatedAt());
            dto.setCancelledAt(delivery.getCancelledAt());
            dto.setCancellationReason(delivery.getCancellationReason());
            dto.setCancelledBy(delivery.getCancelledBy());

            return dto;
        }
   }
    // Add this method to your existing DeliveryController

    @PostMapping("/estimate-fare")
    public ResponseEntity<FareResponseDTO> estimateFare(@Valid @RequestBody FareEstimateDTO request,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = appUserService.findByEmail(userDetails.getUsername()).orElseThrow(
                ()-> new ResourceNotFoundException("User","email", userDetails.getUsername()));
            PricingService.FareBreakdown breakdown = pricingService.getFareBreakdown(
                    request.getPickupLatitude(),
                    request.getPickupLongitude(),
                    request.getDropoffLatitude(),
                    request.getDropoffLongitude()
            );

            double totalFare = breakdown.getTotalFare();
            double discount = 0.0;
            String promoApplied = null;
            // Apply promo code if provided
            if (request.getPromoCode() != null && !request.getPromoCode().isEmpty()) {
                double originalFare = totalFare;
                totalFare = pricingService.applyPromoCode(totalFare, request.getPromoCode(),user);
                discount = originalFare - totalFare;
                if (discount > 0) {
                    promoApplied = request.getPromoCode();
                    log.info("User {} applied promo code '{}': -{} MKD",
                            user.getId(), request.getPromoCode(), discount);
                }else{
                    log.warn("Promo code '{}' was invalid or not applicable for user {}",
                            request.getPromoCode(), user.getId());
                }

            }

            FareResponseDTO response = buildFareResponse(breakdown, totalFare, discount, promoApplied);

            return ResponseEntity.ok(response);


    }
    /**
     * Helper: Build fare response
     */
    private FareResponseDTO buildFareResponse(PricingService.FareBreakdown breakdown,
                                              double totalFare, double discount, String promoApplied) {
        String surgeReason = determineSurgeReason(breakdown.getSurgeMultiplier());

        return new FareResponseDTO(
                totalFare,
                breakdown.getCurrency(),
                pricingService.convertToEUR(totalFare),
                breakdown.getDistanceKm(),
                breakdown.getEstimatedMinutes(),
                breakdown.getBaseFare(),
                breakdown.getDistanceFare(),
                breakdown.getTimeFare(),
                breakdown.getSurgeMultiplier(),
                breakdown.getCityCenterCharge() > 0,
                breakdown.getCityCenterCharge(),
                breakdown.getAirportCharge() > 0,
                breakdown.getAirportCharge(),
                surgeReason,
                discount,
                promoApplied
        );
    }
    private String determineSurgeReason(double multiplier) {
        if (multiplier >= 1.3) return "Peak Hour";
        if (multiplier >= 1.25) return "Night Time";
        if (multiplier >= 1.15) return "Weekend";
        return "Normal";
    }
    private AppUser findUserById(Long userId) {

        return appUserService.findById(userId).orElseThrow();
    }
}