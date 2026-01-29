package com.deliverXY.backend.NewCode.admin.service;

import com.deliverXY.backend.NewCode.admin.dto.AdminDashboardDTO;
import com.deliverXY.backend.NewCode.admin.dto.AdminUserDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryResponseDTO;
import com.deliverXY.backend.NewCode.drivers.domain.DriverLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface AdminService {

    AdminDashboardDTO getDashboardStats();

    Page<AdminUserDTO> getAllUsers(Pageable pageable);

    void blockUser(Long id);
    void unblockUser(Long id);

    Page<DeliveryResponseDTO> getAllDeliveries(Pageable pageable);

    void assignDelivery(Long deliveryId, Long agentId);

    List<DriverLocation> getAllDriverLocations();

    void refundDelivery(Long deliveryId, BigDecimal amount, String reason);

    void cancelDelivery(Long deliveryId, String reason);
}
