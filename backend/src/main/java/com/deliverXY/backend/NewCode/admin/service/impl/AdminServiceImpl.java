package com.deliverXY.backend.NewCode.admin.service.impl;

import com.deliverXY.backend.NewCode.admin.dto.AdminDashboardDTO;
import com.deliverXY.backend.NewCode.admin.dto.AdminUserDTO;
import com.deliverXY.backend.NewCode.admin.service.AdminService;
import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryResponseDTO;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryRepository;
import com.deliverXY.backend.NewCode.deliveries.service.DeliveryService;
import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.kyc.domain.AppUserKYC;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.repository.PaymentRepository;
import com.deliverXY.backend.NewCode.payments.service.PaymentService;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.kyc.service.AppUserKYCService;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import com.deliverXY.backend.NewCode.drivers.domain.DriverLocation;
import com.deliverXY.backend.NewCode.drivers.repository.DriverLocationRepository;
import com.deliverXY.backend.NewCode.common.enums.DeliveryStatus;
import com.deliverXY.backend.NewCode.common.enums.KYCStatus;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import com.deliverXY.backend.NewCode.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final AppUserService userService;
    private final AppUserKYCService kycService;
    private final DeliveryService deliveryService;
    private final DeliveryRepository deliveryRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final DriverLocationRepository driverLocationRepository;

    @Override
    public AdminDashboardDTO getDashboardStats() {


        long totalUsers = userService.countALL();
        long totalClients = userService.countByRole(UserRole.CLIENT);
        long totalAgents = userService.countByRole(UserRole.AGENT);
        long pendingKYC = kycService.countByStatus(KYCStatus.PENDING);

        long totalDeliveries = deliveryService.countAll();
        long pendingDeliveries = deliveryService.countByStatus(DeliveryStatus.REQUESTED);
        long activeDeliveries = deliveryService.countByStatus(DeliveryStatus.IN_TRANSIT);
        long completedDeliveries = deliveryService.countByStatus(DeliveryStatus.DELIVERED);

        return new AdminDashboardDTO(
                totalUsers,
                totalClients,
                totalAgents,
                pendingKYC,
                totalDeliveries,
                pendingDeliveries,
                activeDeliveries,
                completedDeliveries
        );
    }

    @Override
    public Page<AdminUserDTO> getAllUsers(Pageable pageable) {
        return userService.findAll(pageable)
                .map(e-> {
                    AppUserKYC kyc = null;
                    try {
                        kyc = kycService.getKYC(e.getId());
                    } catch (Exception ignored){}
                    return new AdminUserDTO(e, kyc);
                });
    }

    @Override
    public void blockUser(Long id) {
        AppUser user = userService.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("Cannot block admin accounts.");
        }
        user.setIsActive(false);
        userService.save(user);
    }

    @Override
    public void unblockUser(Long id) {
        AppUser user = userService.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setIsActive(true);
        userService.save(user);
    }

    @Override
    public Page<DeliveryResponseDTO> getAllDeliveries(Pageable pageable) {
        return deliveryService.getAllDeliveries(pageable);
    }

    @Override
    public void assignDelivery(Long deliveryId, Long agentId) {
        AppUser agent = userService.findById(agentId)
                .orElseThrow(() -> new NotFoundException("Agent not found"));

        deliveryService.assign(deliveryId, agent);
    }

    @Override
    public List<DriverLocation> getAllDriverLocations() {
        return driverLocationRepository.findAll();
    }

    /**
     * Refund a delivery payment.
     * 
     * @param deliveryId The delivery ID to refund
     * @param amount The amount to refund (must be <= payment amount)
     * @param reason The reason for the refund
     */
    @Override
    @Transactional
    public void refundDelivery(Long deliveryId, BigDecimal amount, String reason) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found"));

        // Find the payment for this delivery
        Payment payment = paymentRepository.findByDeliveryId(deliveryId)
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("No completed payment found for this delivery"));

        // Validate refund amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Refund amount must be positive");
        }

        if (amount.compareTo(payment.getAmount()) > 0) {
            throw new BadRequestException("Refund amount cannot exceed payment amount");
        }

        // Perform the refund
        paymentService.refund(payment.getId(), amount, reason);

        log.info("Admin refunded delivery {} for amount {}: {}", deliveryId, amount, reason);
    }

    /**
     * Cancel a delivery.
     * 
     * @param deliveryId The delivery ID to cancel
     * @param reason The reason for cancellation
     */
    @Override
    @Transactional
    public void cancelDelivery(Long deliveryId, String reason) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found"));

        // Check if delivery can be cancelled
        if (delivery.getStatus().isTerminal()) {
            throw new BadRequestException("Cannot cancel a delivery that is already " + delivery.getStatus());
        }

        // Update delivery status to CANCELLED
        delivery.setStatus(DeliveryStatus.CANCELLED);
        deliveryRepository.save(delivery);

        log.info("Admin cancelled delivery {}: {}", deliveryId, reason);
    }
}
