package com.deliverXY.backend.NewCode.admin.service.impl;

import com.deliverXY.backend.NewCode.admin.dto.AdminDashboardDTO;
import com.deliverXY.backend.NewCode.admin.dto.AdminUserDTO;
import com.deliverXY.backend.NewCode.admin.service.AdminService;
import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryResponseDTO;
import com.deliverXY.backend.NewCode.deliveries.service.DeliveryService;
import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.kyc.service.AppUserKYCService;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import com.deliverXY.backend.NewCode.common.enums.DeliveryStatus;
import com.deliverXY.backend.NewCode.common.enums.KYCStatus;
import com.deliverXY.backend.NewCode.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AppUserService userService;
    private final AppUserKYCService kycService;
    private final DeliveryService deliveryService;

    @Override
    public AdminDashboardDTO getDashboardStats() {

        var users = userService.findAll();
        var deliveries = deliveryService.getAllDeliveries();

        long totalUsers = users.size();
        long totalClients = users.stream()
                .filter(u -> u.getRole() == UserRole.CLIENT)
                .count();
        long totalAgents = users.stream()
                .filter(u -> u.getRole() == UserRole.AGENT)
                .count();

        long pendingKYC = kycService.countByStatus(KYCStatus.PENDING);

        long totalDeliveries = deliveries.size();
        long pendingDeliveries = deliveries.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.REQUESTED)
                .count();
        long activeDeliveries = deliveries.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.IN_TRANSIT)
                .count();
        long completedDeliveries = deliveries.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.DELIVERED)
                .count();

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
    public List<AdminUserDTO> getAllUsers() {
        return userService.findAll()
                .stream()
                .map(e-> new AdminUserDTO(e))
                .toList();
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
    public List<DeliveryResponseDTO> getAllDeliveries() {
        return deliveryService.getAllDeliveries();
    }

    @Override
    public void assignDelivery(Long deliveryId, Long agentId) {
        AppUser agent = userService.findById(agentId)
                .orElseThrow(() -> new NotFoundException("Agent not found"));

        deliveryService.assign(deliveryId, agent);
    }
}
