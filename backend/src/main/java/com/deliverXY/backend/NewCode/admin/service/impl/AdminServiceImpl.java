package com.deliverXY.backend.NewCode.admin.service.impl;

import com.deliverXY.backend.NewCode.admin.dto.AdminDashboardDTO;
import com.deliverXY.backend.NewCode.admin.dto.AdminUserDTO;
import com.deliverXY.backend.NewCode.admin.service.AdminService;
import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryResponseDTO;
import com.deliverXY.backend.NewCode.deliveries.service.DeliveryService;
import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.kyc.domain.AppUserKYC;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.kyc.service.AppUserKYCService;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import com.deliverXY.backend.NewCode.drivers.domain.DriverLocation;
import com.deliverXY.backend.NewCode.drivers.repository.DriverLocationRepository;
import com.deliverXY.backend.NewCode.common.enums.DeliveryStatus;
import com.deliverXY.backend.NewCode.common.enums.KYCStatus;
import com.deliverXY.backend.NewCode.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AppUserService userService;
    private final AppUserKYCService kycService;
    private final DeliveryService deliveryService;
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
                        Long userId = Objects.requireNonNull(e.getId(), "userId");
                        kyc = kycService.getKYC(userId);
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
    @Transactional(readOnly = true)
    public Page<DeliveryResponseDTO> getAllDeliveries(@NonNull Pageable pageable) {
        return deliveryService.getAllDeliveries(pageable);
    }

    @Override
    public void assignDelivery(@NonNull Long deliveryId, @NonNull Long agentId) {
        AppUser agent = Objects.requireNonNull(
                userService.findById(agentId)
                        .orElseThrow(() -> new NotFoundException("Agent not found"))
        );

        deliveryService.assign(deliveryId, agent);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DriverLocation> getAllDriverLocations(@NonNull Pageable pageable) {
        return driverLocationRepository.findAll(pageable);
    }
}
