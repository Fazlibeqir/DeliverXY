package com.deliverXY.backend.NewCode.admin.service;

import com.deliverXY.backend.NewCode.admin.dto.AdminDashboardDTO;
import com.deliverXY.backend.NewCode.admin.dto.AdminUserDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryResponseDTO;

import java.util.List;

public interface AdminService {

    AdminDashboardDTO getDashboardStats();

    List<AdminUserDTO> getAllUsers();

    void blockUser(Long id);
    void unblockUser(Long id);

    List<DeliveryResponseDTO> getAllDeliveries();

    void assignDelivery(Long deliveryId, Long agentId);
}
