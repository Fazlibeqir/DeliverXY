package com.deliverXY.backend.NewCode.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminDashboardDTO {
    private long totalUsers;
    private long totalClients;
    private long totalAgents;
    private long pendingKYC;

    private long totalDeliveries;
    private long pendingDeliveries;
    private long activeDeliveries;
    private long completedDeliveries;
}
