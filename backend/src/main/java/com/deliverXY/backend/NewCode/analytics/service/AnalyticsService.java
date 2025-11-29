package com.deliverXY.backend.NewCode.analytics.service;

import com.deliverXY.backend.NewCode.analytics.dto.DriverStatsDTO;
import com.deliverXY.backend.NewCode.analytics.dto.PlatformStatsDTO;
import com.deliverXY.backend.NewCode.analytics.dto.TripDTO;
import com.deliverXY.backend.NewCode.analytics.dto.UserStatsDTO;
import com.deliverXY.backend.NewCode.analytics.service.impl.AnalyticsServiceImpl;
import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AnalyticsService {
    Page<TripDTO> getUserTripHistory(Long userId, Pageable pageable);

    /**
     * Get driver delivery history with pagination
     */
    Page<TripDTO> getDriverDeliveryHistory(Long driverId, Pageable pageable);

    /**
     * Get user statistics
     */
    UserStatsDTO getUserStatistics(Long userId);

    /**
     * Get driver statistics
     */
    DriverStatsDTO getDriverStatistics(Long driverId, LocalDate startDate, LocalDate endDate);

    /**
     * Get platform-wide statistics (Admin)
     */
    PlatformStatsDTO getPlatformStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * Generate daily statistics (Should be run as scheduled task)
     */
    void generateDailyStats(LocalDate date);


}
