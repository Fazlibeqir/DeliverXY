package com.deliverXY.backend.service;

import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.DeliveryStats;
import com.deliverXY.backend.service.impl.AnalyticsServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface AnalyticsService {
    Page<Delivery> getUserTripHistory(Long userId, Pageable pageable);

    /**
     * Get driver delivery history with pagination
     */
    Page<Delivery> getDriverDeliveryHistory(Long driverId, Pageable pageable);

    /**
     * Get user statistics
     */
    AnalyticsServiceImpl.UserStatistics getUserStatistics(Long userId);

    /**
     * Get driver statistics
     */
    AnalyticsServiceImpl.DriverStatistics getDriverStatistics(Long driverId, LocalDate startDate, LocalDate endDate);

    /**
     * Get platform-wide statistics (Admin)
     */
    AnalyticsServiceImpl.PlatformStatistics getPlatformStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * Generate daily statistics (Should be run as scheduled task)
     */
    void generateDailyStats(LocalDate date);


}
