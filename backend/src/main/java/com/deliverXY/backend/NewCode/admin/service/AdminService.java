package com.deliverXY.backend.NewCode.admin.service;

import com.deliverXY.backend.NewCode.admin.dto.AdminDashboardDTO;
import com.deliverXY.backend.NewCode.admin.dto.AdminUserDTO;
import com.deliverXY.backend.NewCode.deliveries.dto.DeliveryResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    AdminDashboardDTO getDashboardStats();

    Page<AdminUserDTO> getAllUsers(Pageable pageable);

    void blockUser(Long id);
    void unblockUser(Long id);

    Page<DeliveryResponseDTO> getAllDeliveries(Pageable pageable);

    void assignDelivery(Long deliveryId, Long agentId);
}
