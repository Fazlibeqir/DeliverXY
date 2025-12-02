package com.deliverXY.backend.NewCode.deliveries.service.impl;

import com.deliverXY.backend.NewCode.common.enums.PaymentMethod;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import com.deliverXY.backend.NewCode.deliveries.domain.*;
import com.deliverXY.backend.NewCode.deliveries.dto.*;
import com.deliverXY.backend.NewCode.deliveries.repository.*;
import com.deliverXY.backend.NewCode.deliveries.mapper.DeliveryMapper;
import com.deliverXY.backend.NewCode.deliveries.service.DeliveryService;
import com.deliverXY.backend.NewCode.deliveries.validator.DeliveryValidator;
import com.deliverXY.backend.NewCode.earnings.domain.DriverEarnings;
import com.deliverXY.backend.NewCode.earnings.repository.DriverEarningsRepository;
import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.common.enums.DeliveryStatus;
import com.deliverXY.backend.NewCode.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepo;
    private final DeliveryHistoryRepository historyRepo;
    private final DeliveryTrackingRepository trackingRepo;
    private final WalletService walletService;
    private final DeliveryInsuranceRepository insuranceRepo;
    private final DeliveryPaymentRepository paymentRepo;
    private final DeliveryRatingRepository ratingRepo;
    private final DriverEarningsRepository earningsRepo;
    private final LocationService locationService;

    private final DeliveryMapper mapper;
    private final DeliveryValidator validator;


    private final PricingService pricingService;
    //HELPERS
    private Delivery load(Long id) {
        return deliveryRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Delivery not found: " + id ));
    }
    private DeliveryResponseDTO respond(Delivery d){
        return mapper.toResponse(d);
    }
    private void logHistory(Delivery d, String note, String by){
        DeliveryHistory h = new DeliveryHistory();
        h.setDelivery(d);
        h.setStatus(d.getStatus());
        h.setChangedBy(by);
        h.setNote(note);
        historyRepo.save(h);
    }
    //GETTERS
    @Override
    public List<DeliveryResponseDTO> getAllDeliveries() {
        return deliveryRepo.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public DeliveryResponseDTO getDeliveryById(Long id) {
        return respond(load(id));
    }

    @Override
    public List<DeliveryResponseDTO> getByStatus(String status) {
        DeliveryStatus st = DeliveryStatus.valueOf(status.toUpperCase());
        return deliveryRepo.findByStatus(st).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<DeliveryResponseDTO> getByClient(Long clientId) {
        return deliveryRepo.findByClientId(clientId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<DeliveryResponseDTO> getByAgent(Long agentId) {
        return deliveryRepo.findByAgentId(agentId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<DeliveryResponseDTO> findNearby(Double lat, Double lng, Double radiusKm) {
        return deliveryRepo.findNearbyDeliveries(lat, lng, radiusKm)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public DeliveryResponseDTO create(DeliveryDTO dto, AppUser client) {
        validator.validateCreate(dto);

        Delivery d = new Delivery();
        d.setClient(client);
        mapper.updateEntityFromDTO(d, dto);

        if (d.getPickupLatitude() != null && d.getPickupLongitude() != null &&
                d.getDropoffLatitude() != null && d.getDropoffLongitude() != null) {

            double distance = locationService.distanceKm(
                    d.getPickupLatitude(),
                    d.getPickupLongitude(),
                    d.getDropoffLatitude(),
                    d.getDropoffLongitude()
            );

            d.setDistanceKm(distance);
        } else {
            d.setDistanceKm(0.0);
        }



        d.setStatus(DeliveryStatus.REQUESTED);
        d.setTrackingCode("TRK-"+System.currentTimeMillis());

        deliveryRepo.save(d);

        logHistory(d, "Delivery created", client.getUsername());

        return respond(d);
    }

    @Override
    @Transactional
    public DeliveryResponseDTO update(Long id, DeliveryDTO dto) {
        Delivery d = load(id);

        mapper.updateEntityFromDTO(d, dto);
        deliveryRepo.save(d);

        logHistory(d, "Delivery updated", "SYSTEM");
        return respond(d);
    }

    @Override
    @Transactional
    public DeliveryResponseDTO assign(Long id, AppUser agent) {
        Delivery d = load(id);

        if (d.getStatus() != DeliveryStatus.REQUESTED)
            throw new BadRequestException("Delivery is not open for assignment");

        if (!agent.getIsActive())
            throw new BadRequestException("Agent is not active");

        if (!agent.getIsVerified()){
            throw new BadRequestException("Agent is not verified");
        }
        if (d.getAgent() != null){
            throw new BadRequestException("Delivery is already assigned");
        }

        d.setAgent(agent);
        d.setStatus(DeliveryStatus.ASSIGNED);
        d.setAssignedAt(LocalDateTime.now());

        deliveryRepo.save(d);

        logHistory(d, "Assigned to agent " + agent.getUsername(), agent.getUsername());

        return respond(d);
    }

    @Override
    @Transactional
    public DeliveryResponseDTO updateStatus(Long id, String status) {
        Delivery d = load(id);
        DeliveryStatus newStatus = DeliveryStatus.valueOf(status.toUpperCase());

        switch (newStatus) {
            case PICKED_UP -> d.setActualPickupTime(LocalDateTime.now());
            case DELIVERED -> {
                d.setActualDeliveryTime(LocalDateTime.now());
                processDeliveryPayment(d);
            }

            case CANCELLED -> d.setCancelledAt(LocalDateTime.now());
        }
        d.setStatus(newStatus);
        deliveryRepo.save(d);

        logHistory(d, "Status changed to " + status, "SYSTEM");

        return respond(d);
    }

    private void processDeliveryPayment(Delivery d) {
        BigDecimal totalPrice = BigDecimal.valueOf(pricingService.getFareBreakdown(
                d.getPickupLatitude(),
                d.getPickupLongitude(),
                d.getDropoffLatitude(),
                d.getDropoffLongitude()
        ).getTotalFare());

       try{
            walletService.withdraw(
                   d.getClient().getId(),
                   totalPrice,
                   "Delivery payment for " + d.getTrackingCode()
           );
       }catch (Exception e){
               throw new BadRequestException("Insufficient funds");
       }
        DeliveryPayment payment = paymentRepo.findByDeliveryId(d.getId())
                .orElse(new DeliveryPayment());

        payment.setDelivery(d);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setFinalAmount(totalPrice);
        payment.setPaymentMethod(PaymentMethod.WALLET);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepo.save(payment);

        BigDecimal driverCut = totalPrice.multiply(new BigDecimal("0.80"));
        BigDecimal platformCut = totalPrice.subtract(driverCut);

        DriverEarnings earnings = new DriverEarnings();
        earnings.setDelivery(d);
        earnings.setAgentId(d.getAgent().getId());
        earnings.setDriverEarnings(driverCut);
        earnings.setTip(BigDecimal.ZERO);

        earningsRepo.save(earnings);


        logHistory(d, "Payment processed. Driver earned " + driverCut + ", platform kept " + platformCut, "SYSTEM");
    }

    @Override
    @Transactional
    public DeliveryResponseDTO updateLocation(Long id, Double lat, Double lng) {
        Delivery delivery = load(id);

        DeliveryTracking tracking = trackingRepo.findByDeliveryId((id))
                .orElse(new DeliveryTracking());

        tracking.setDeliveryId(delivery.getId());
        tracking.setCurrentLatitude(lat);
        tracking.setCurrentLongitude(lng);
        tracking.setLastLocationUpdate(LocalDateTime.now());

        trackingRepo.save(tracking);
        return respond(delivery);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Delivery delivery = load(id);
        deliveryRepo.delete(delivery);
        historyRepo.deleteAll(historyRepo.findByDelivery_IdOrderByChangedAtAsc(id));
    }
    //FARE ESTIMATION
    @Override
    public FareResponseDTO estimateFare(FareEstimateDTO dto, AppUser user) {
        FareBreakdown breakdown = pricingService.getFareBreakdown(
                dto.getPickupLatitude(),
                dto.getPickupLongitude(),
                dto.getDropoffLatitude(),
                dto.getDropoffLongitude()
        );

        double total = breakdown.getTotalFare();
        double discount = 0;
        String promoApplied = null;

        if (hasPromo(dto)) {
            double before = total;
            total = pricingService.applyPromoCode(total, dto.getPromoCode(), user);
            discount = before - total;
            promoApplied = discount > 0 ? dto.getPromoCode() : null;
        }
        return new FareResponseDTO(
                total,
                breakdown.getCurrency(),
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
                surgeReason(breakdown.getSurgeMultiplier()),
                discount,
                promoApplied
        );
    }
    private boolean hasPromo(FareEstimateDTO dto) {
        return dto.getPromoCode() != null && !dto.getPromoCode().isBlank();
    }
    private String surgeReason(double surgeMultiplier) {
        if (surgeMultiplier >= 1.3) return "Peak Hour";
        if (surgeMultiplier >= 1.25) return "Night Time";
        if (surgeMultiplier >= 1.15) return "Weekend";
        return "Normal";
    }
}
