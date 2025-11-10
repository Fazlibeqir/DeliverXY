
package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.enums.UserRole;
import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.DeliveryStats;
import com.deliverXY.backend.repositories.DeliveryRepository;
import com.deliverXY.backend.repositories.DeliveryStatsRepository;
import com.deliverXY.backend.repositories.AppUserRepository;
import com.deliverXY.backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryStatsRepository deliveryStatsRepository;
    private final AppUserRepository appUserRepository;

    /**
     * Get user trip history with pagination
     */
    public Page<Delivery> getUserTripHistory(Long userId, Pageable pageable) {
        return deliveryRepository.findByClientIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Get driver delivery history with pagination
     */
    public Page<Delivery> getDriverDeliveryHistory(Long driverId, Pageable pageable) {
        return deliveryRepository.findByAgentIdOrderByCreatedAtDesc(driverId, pageable);
    }

    /**
     * Get user statistics
     */
    public UserStatistics getUserStatistics(Long userId) {
        List<Delivery> deliveries = deliveryRepository.findByClientId(userId);

        long totalTrips = deliveries.size();
        long completedTrips = deliveries.stream()
                .filter(Delivery::isDelivered)
                .count();
        long cancelledTrips = deliveries.stream()
                .filter(Delivery::isCancelled)
                .count();

        double totalSpent = deliveries.stream()
                .filter(Delivery::isDelivered)
                .mapToDouble(d -> d.getFinalPrice() != null ? d.getFinalPrice() : 0.0)
                .sum();

        double totalDistance = deliveries.stream()
                .filter(Delivery::isDelivered)
                .mapToDouble(d -> d.getDistance() != null ? d.getDistance() : 0.0)
                .sum();

        // Find favorite driver (most deliveries with)
        Map<Long, Long> driverCounts = deliveries.stream()
                .filter(d -> d.getDriver() != null)
                .collect(Collectors.groupingBy(d -> d.getDriver().getId(), Collectors.counting()));

        Long favoriteDriverId = driverCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        return new UserStatistics(
                totalTrips,
                completedTrips,
                cancelledTrips,
                totalSpent,
                totalDistance,
                favoriteDriverId
        );
    }

    /**
     * Get driver statistics
     */
    public DriverStatistics getDriverStatistics(Long driverId, LocalDate startDate, LocalDate endDate) {
        List<Delivery> deliveries = deliveryRepository.findByAgentIdAndCreatedAtBetween(
                driverId,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );

        long totalDeliveries = deliveries.size();
        long completedDeliveries = deliveries.stream()
                .filter(Delivery::isDelivered)
                .count();

        double totalEarnings = deliveries.stream()
                .filter(Delivery::isDelivered)
                .mapToDouble(d -> d.getDriverEarnings() != null ? d.getDriverEarnings() : 0.0)
                .sum();

        double totalDistance = deliveries.stream()
                .filter(Delivery::isDelivered)
                .mapToDouble(d -> d.getDistance() != null ? d.getDistance() : 0.0)
                .sum();

        // Calculate average rating
        Double averageRating = deliveries.stream()
                .filter(d -> d.getDriverRating() != null)
                .mapToDouble(Delivery::getDriverRating)
                .average()
                .orElse(0.0);

        // Calculate acceptance rate
        double acceptanceRate = totalDeliveries > 0 ?
                (completedDeliveries * 100.0 / totalDeliveries) : 0.0;

        return new DriverStatistics(
                totalDeliveries,
                completedDeliveries,
                totalEarnings,
                totalDistance,
                averageRating,
                acceptanceRate
        );
    }

    /**
     * Get platform-wide statistics (Admin)
     */
    public PlatformStatistics getPlatformStatistics(LocalDate startDate, LocalDate endDate) {
        List<DeliveryStats> stats = deliveryStatsRepository
                .findByStatDateBetweenOrderByStatDateDesc(startDate, endDate);

        int totalDeliveries = stats.stream()
                .mapToInt(DeliveryStats::getTotalDeliveries)
                .sum();

        int completedDeliveries = stats.stream()
                .mapToInt(DeliveryStats::getCompletedDeliveries)
                .sum();

        double totalRevenue = stats.stream()
                .mapToDouble(DeliveryStats::getTotalRevenue)
                .sum();

        double totalDistance = stats.stream()
                .mapToDouble(DeliveryStats::getTotalDistanceKm)
                .sum();

        // Get active users count
        long totalUsers = appUserRepository.count();
        long activeDrivers = appUserRepository.countByRoleAndIsActiveTrue(UserRole.AGENT);
        long activeCustomers = appUserRepository.countByRoleAndIsActiveTrue(UserRole.CLIENT);

        return new PlatformStatistics(
                totalDeliveries,
                completedDeliveries,
                totalRevenue,
                totalDistance,
                totalUsers,
                activeDrivers,
                activeCustomers
        );
    }

    /**
     * Generate daily statistics (Should be run as scheduled task)
     */
    @Transactional
    public void generateDailyStats(LocalDate date) {
        log.info("Generating daily stats for {}", date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<Delivery> deliveries = deliveryRepository
                .findByCreatedAtBetween(startOfDay, endOfDay);

        int totalDeliveries = deliveries.size();
        int completedDeliveries = (int) deliveries.stream()
                .filter(Delivery::isDelivered)
                .count();
        int cancelledDeliveries = (int) deliveries.stream()
                .filter(Delivery::isCancelled)
                .count();

        double totalRevenue = deliveries.stream()
                .filter(Delivery::isDelivered)
                .mapToDouble(d -> d.getFinalPrice() != null ? d.getFinalPrice() : 0.0)
                .sum();

        double totalDistance = deliveries.stream()
                .filter(Delivery::isDelivered)
                .mapToDouble(d -> d.getDistance() != null ? d.getDistance() : 0.0)
                .sum();

        DeliveryStats stats = deliveryStatsRepository.findByStatDate(date)
                .orElse(new DeliveryStats());

        stats.setStatDate(date);
        stats.setTotalDeliveries(totalDeliveries);
        stats.setCompletedDeliveries(completedDeliveries);
        stats.setCancelledDeliveries(cancelledDeliveries);
        stats.setTotalRevenue(totalRevenue);
        stats.setTotalDistanceKm(totalDistance);

        deliveryStatsRepository.save(stats);

        log.info("Daily stats generated: {} deliveries, {} MKD revenue", totalDeliveries, totalRevenue);
    }

    // Inner classes for statistics
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class UserStatistics {
        private Long totalTrips;
        private Long completedTrips;
        private Long cancelledTrips;
        private Double totalSpent;
        private Double totalDistanceKm;
        private Long favoriteDriverId;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class DriverStatistics {
        private Long totalDeliveries;
        private Long completedDeliveries;
        private Double totalEarnings;
        private Double totalDistanceKm;
        private Double averageRating;
        private Double acceptanceRate;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class PlatformStatistics {
        private Integer totalDeliveries;
        private Integer completedDeliveries;
        private Double totalRevenue;
        private Double totalDistanceKm;
        private Long totalUsers;
        private Long activeDrivers;
        private Long activeCustomers;
    }
}