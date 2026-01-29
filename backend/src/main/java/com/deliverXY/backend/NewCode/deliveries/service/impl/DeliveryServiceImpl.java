package com.deliverXY.backend.NewCode.deliveries.service.impl;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import com.deliverXY.backend.NewCode.deliveries.domain.*;
import com.deliverXY.backend.NewCode.deliveries.dto.*;
import com.deliverXY.backend.NewCode.deliveries.repository.*;
import com.deliverXY.backend.NewCode.deliveries.mapper.DeliveryMapper;
import com.deliverXY.backend.NewCode.deliveries.service.DeliveryService;
import com.deliverXY.backend.NewCode.deliveries.service.PricingConfigService;
import com.deliverXY.backend.NewCode.deliveries.validator.DeliveryValidator;
import com.deliverXY.backend.NewCode.earnings.domain.DriverEarnings;
import com.deliverXY.backend.NewCode.earnings.repository.DriverEarningsRepository;
import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.repository.PaymentRepository;
import com.deliverXY.backend.NewCode.payments.service.PaymentService;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.common.enums.DeliveryStatus;
import com.deliverXY.backend.NewCode.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import static com.deliverXY.backend.NewCode.common.enums.DeliveryStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepo;
    private final DeliveryHistoryRepository historyRepo;
    private final WalletService walletService;
    private final DriverEarningsRepository earningsRepo;
    private final PaymentRepository paymentRepo;
    private final GeocodingService geocodingService;
    private final PaymentService paymentService;

    private final DeliveryMapper mapper;
    private final DeliveryValidator validator;


    private final PricingService pricingService;
    private final PricingConfigService pricingConfigService;

    private static final String DEFAULT_CITY = "Skopje";

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

    @Override
    public Page<DeliveryResponseDTO> getAllDeliveries(Pageable pageable) {
        return deliveryRepo.findAll(pageable).map(mapper::toResponse);
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
        return deliveryRepo.findByClientId(clientId).stream()
                .sorted((a, b) -> {
                    LocalDateTime dateA = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.MIN;
                    LocalDateTime dateB = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN;
                    return dateB.compareTo(dateA); // Descending order (newest first)
                })
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<DeliveryResponseDTO> getByAgent(Long agentId) {
        return deliveryRepo.findByAgentId(agentId).stream()
                .sorted((a, b) -> {
                    LocalDateTime dateA = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.MIN;
                    LocalDateTime dateB = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN;
                    return dateB.compareTo(dateA); // Descending order (newest first)
                })
                .map(mapper::toResponse)
                .toList();
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

        if (d.getDropoffLatitude() == null || d.getDropoffLongitude() == null) {

            if (dto.getDropoffLatitude() != null && dto.getDropoffLongitude() != null) {
                d.setDropoffLatitude(dto.getDropoffLatitude());
                d.setDropoffLongitude(dto.getDropoffLongitude());
            } else {
                if (d.getDropoffAddress() == null || d.getDropoffAddress().isBlank()) {
                    throw new BadRequestException("Dropoff address or coordinates are required");
                }

                GeocodingService.GeoPoint geo =
                        geocodingService.geocode(d.getDropoffAddress(), "Skopje");

                d.setDropoffLatitude(geo.lat());
                d.setDropoffLongitude(geo.lng());
            }
        }

        FareBreakdown breakdown = pricingService.getFareBreakdown(
                d.getPickupLatitude(),
                d.getPickupLongitude(),
                d.getDropoffLatitude(),
                d.getDropoffLongitude()
        );

        d.setDistanceKm(
                BigDecimal.valueOf(breakdown.getDistanceKm())
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue()
        );

        d.setStatus(DeliveryStatus.REQUESTED);
        d.setTrackingCode("TRK-" + System.currentTimeMillis());

        deliveryRepo.save(d);

        PaymentProvider provider =
                dto.getPaymentProvider() != null
                        ? dto.getPaymentProvider()
                        : PaymentProvider.MOCK;

        if (provider == PaymentProvider.WALLET) {
            walletService.ensureSufficientBalance(
                    client.getId(),
                    breakdown.getTotalFare()
            );
        }

        paymentService.initializePayment(
                d.getId(),
                breakdown.getTotalFare(),
                provider,
                client.getId()
        );

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
        if (deliveryRepo.findFirstByAgentIdAndStatusInOrderByAssignedAtDesc(
                agent.getId(),
                List.of(ASSIGNED, PICKED_UP, IN_TRANSIT)
        ).isPresent()) {
            throw new BadRequestException("Agent already has an active delivery");
        }


        d.setAgent(agent);
        d.setStatus(ASSIGNED);
        d.setAssignedAt(LocalDateTime.now());

        deliveryRepo.save(d);

        logHistory(d, "Assigned to agent " + agent.getUsername(), agent.getUsername());

        return respond(d);
    }
    @Transactional
    public void settleDeliveryEarnings(Delivery d) {

        Payment payment = paymentRepo.findByDeliveryId(d.getId())
                .orElseThrow(() -> new BadRequestException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BadRequestException("Payment not completed");
        }

        if (Boolean.TRUE.equals(payment.getEscrowReleased())) {
            return; // already settled (idempotent)
        }

        BigDecimal total = payment.getAmount();

        // Use platform commission % from active pricing config (driver gets remainder)
        var pricingConfig = pricingConfigService.getActivePricing(DEFAULT_CITY);
        double platformPct = pricingConfig.getPlatformCommissionPercent() != null
                ? pricingConfig.getPlatformCommissionPercent() / 100.0
                : 0.20;
        double driverPct = 1.0 - platformPct;
        BigDecimal driverCut = total
                .multiply(BigDecimal.valueOf(driverPct))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal platformCut = total.subtract(driverCut);

        walletService.deposit(
                d.getAgent().getId(),
                driverCut,
                "DELIVERY_EARNINGS_" + d.getTrackingCode()
        );

        DriverEarnings earnings = new DriverEarnings();
        earnings.setDelivery(d);
        earnings.setAgentId(d.getAgent().getId());
        earnings.setDriverEarnings(driverCut);
        earnings.setTip(BigDecimal.ZERO);
        earningsRepo.save(earnings);

        payment.setEscrowReleased(true);
        payment.setDriverAmount(driverCut);
        payment.setPlatformFee(platformCut);
        paymentRepo.save(payment);

        logHistory(
                d,
                "Delivery completed. Driver earned " + driverCut,
                "SYSTEM"
        );
    }


    @Override
    @Transactional
    public DeliveryResponseDTO updateStatus(Long id, String status) {
        Delivery d = load(id);
        DeliveryStatus newStatus = DeliveryStatus.fromString(status);

        switch (newStatus) {
            case PICKED_UP -> d.setActualPickupTime(LocalDateTime.now());
            case DELIVERED -> {
                d.setActualDeliveryTime(LocalDateTime.now());
                settleDeliveryEarnings(d);
            }

            case CANCELLED -> d.setCancelledAt(LocalDateTime.now());
            default -> {}
        }
        d.setStatus(newStatus);
        deliveryRepo.save(d);

        logHistory(d, "Status changed to " + status, "SYSTEM");

        return respond(d);
    }



    @Override
    @Transactional
    public void delete(Long id) {
        Delivery delivery = load(id);
        deliveryRepo.delete(delivery);
        historyRepo.deleteAll(historyRepo.findByDelivery_IdOrderByChangedAtAsc(id));
    }

    @Override
    public long countAll() {
        return deliveryRepo.count();
    }

    @Override
    public long countByStatus(DeliveryStatus deliveryStatus) {
        return deliveryRepo.countByStatus(deliveryStatus);
    }

    @Override
    public FareResponseDTO estimateFare(FareEstimateDTO dto, AppUser user) {
        FareBreakdown breakdown = pricingService.getFareBreakdown(
                dto.getPickupLatitude(),
                dto.getPickupLongitude(),
                dto.getDropoffLatitude(),
                dto.getDropoffLongitude()
        );

        BigDecimal total = breakdown.getTotalFare();
        BigDecimal discount = BigDecimal.ZERO;
        String promoApplied = null;

        if (hasPromo(dto)) {
            BigDecimal before = total;
            // Use the new BigDecimal-based promo application
            total = pricingService.applyPromoCode(total, dto.getPromoCode(), user);
            discount = before.subtract(total).max(BigDecimal.ZERO); // Ensure discount is not negative
            promoApplied = discount.compareTo(BigDecimal.ZERO) > 0 ? dto.getPromoCode() : null;
        }

        return new FareResponseDTO(
                total.doubleValue(), // Final total
                breakdown.getCurrency(),
                breakdown.getDistanceKm(),
                breakdown.getEstimatedMinutes(),
                breakdown.getBaseFare().doubleValue(),
                breakdown.getDistanceFare().doubleValue(),
                breakdown.getTimeFare().doubleValue(),
                breakdown.getSurgeMultiplier(),
                breakdown.getCityCenterCharge().doubleValue() > 0,
                breakdown.getCityCenterCharge().doubleValue(),
                breakdown.getAirportCharge().doubleValue() > 0,
                breakdown.getAirportCharge().doubleValue(),
                dto.getPromoCode(),
                surgeReason(breakdown.getSurgeMultiplier()),
                discount.doubleValue(), // Final discount
                promoApplied
        );
    }

    @Override
    public DeliveryResponseDTO getActiveDelivery(Long agentId) {
        return deliveryRepo
                .findFirstByAgentIdAndStatusInOrderByAssignedAtDesc(
                        agentId,
                        List.of(
                                ASSIGNED,
                                PICKED_UP,
                                IN_TRANSIT
                        )
                )
                .map(mapper::toResponse)
                .orElse(null);
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
