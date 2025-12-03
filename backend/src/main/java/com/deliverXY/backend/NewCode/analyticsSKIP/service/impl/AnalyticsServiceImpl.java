package com.deliverXY.backend.NewCode.analyticsSKIP.service.impl;

import com.deliverXY.backend.NewCode.analyticsSKIP.dto.*;
import com.deliverXY.backend.NewCode.analyticsSKIP.service.AnalyticsService;
import com.deliverXY.backend.NewCode.common.enums.DeliveryStatus;
import com.deliverXY.backend.NewCode.common.enums.UserRole;
import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.deliveries.domain.DeliveryStats;
import com.deliverXY.backend.NewCode.earnings.domain.DriverEarnings;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryRepository;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryStatsRepository;
import com.deliverXY.backend.NewCode.earnings.repository.DriverEarningsRepository;
import com.deliverXY.backend.NewCode.user.repository.AppUserRepository;
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
    private final DriverEarningsRepository driverEarningsRepository;
    private final AppUserRepository appUserRepository;

    // USER TRIPS --------------------------------------------------------------

    @Override
    public Page<TripDTO> getUserTripHistory(Long userId, Pageable pageable) {
        return deliveryRepository
                .findByClientIdOrderByCreatedAtDesc(userId, pageable)
                .map(TripDTO::fromEntity);
    }

    @Override
    public Page<TripDTO> getDriverDeliveryHistory(Long driverId, Pageable pageable) {
        return deliveryRepository
                .findByAgentIdOrderByCreatedAtDesc(driverId, pageable)
                .map(TripDTO::fromEntity);
    }

    // USER STATS -------------------------------------------------------------

    @Override
    public UserStatsDTO getUserStatistics(Long userId) {
        List<Delivery> deliveries = deliveryRepository.findByClientId(userId);

        long total = deliveries.size();

        long completed = deliveries.stream()
                .filter(d->d.getStatus()== DeliveryStatus.DELIVERED)
                .count();
        long cancelled = deliveries.stream()
                .filter(d->d.getStatus()==DeliveryStatus.CANCELLED)
                .count();
        double spent = deliveries.stream()
                .filter(d->d.getStatus()==DeliveryStatus.DELIVERED)
                .mapToDouble(d->
                        d.getDeliveryPayment() != null && d.getDeliveryPayment().getFinalAmount() !=null
                                ? d.getDeliveryPayment().getFinalAmount().doubleValue() : 0.0)
                .sum();
        double distance = deliveries.stream()
                .filter(d-> d.getStatus() == DeliveryStatus.DELIVERED)
                .mapToDouble(d -> d.getDistanceKm() != null ? d.getDistanceKm() : 0.0)
                .sum();

        Long favoriteDriver = deliveries.stream()
                .filter(d -> d.getAgent() != null)
                .collect(Collectors.groupingBy(d -> d.getAgent().getId(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        return new UserStatsDTO(
                total,
                completed,
                cancelled,
                spent,
                distance,
                favoriteDriver
        );
    }

    // DRIVER STATS -----------------------------------------------------------
    //TODO: add driver rating and earnings repositories
    @Override
    public DriverStatsDTO getDriverStatistics(Long driverId, LocalDate start, LocalDate end) {

        List<Delivery> deliveries = deliveryRepository.findByAgentIdAndCreatedAtBetween(
                driverId,
                start.atStartOfDay(),
                end.atTime(23, 59, 59)
        );

        long total = deliveries.size();

        long completed = deliveries.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.DELIVERED)
                .count();

        List<DriverEarnings> driverEarningsList =
                driverEarningsRepository.findAllByCreatedAtBetween(
                        start.atStartOfDay(),
                        end.atTime(23, 59, 59)
                )
                        .stream()
                        .filter(e -> Objects.equals(e.getAgentId(),driverId))
                        .toList();

        double totalEarnings = driverEarningsList.stream()
                .mapToDouble(e->e.getDriverEarnings() !=null ?
                        e.getDriverEarnings().doubleValue() : 0.0)
                .sum();

        double totalTips = driverEarningsList.stream()
                .mapToDouble(e->e.getTip() !=null ?
                        e.getTip().doubleValue():0.0)
                .sum();


        double distance = deliveries.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.DELIVERED)
                .mapToDouble(d -> d.getDistanceKm() != null ? d.getDistanceKm() : 0.0)
                .sum();

        // TEMP: you do NOT have a driverRating in Delivery anymore
        double avgRating = 0.0;

        double acceptRate = total > 0 ? (completed * 100.0 / total) : 0.0;

        return new DriverStatsDTO(
                total,
                completed,
                totalEarnings+totalTips,
                distance,
                avgRating,
                acceptRate
        );
    }

    // PLATFORM STATS ---------------------------------------------------------

    @Override
    public PlatformStatsDTO getPlatformStatistics(LocalDate start, LocalDate end) {

        List<DeliveryStats> stats =
                deliveryStatsRepository.findByStatDateBetweenOrderByStatDateDesc(start, end);

        int totalDeliveries = stats.stream().mapToInt(DeliveryStats::getTotalDeliveries).sum();
        int completedDeliveries = stats.stream().mapToInt(DeliveryStats::getCompletedDeliveries).sum();
        double totalRevenue = stats.stream().mapToDouble(DeliveryStats::getTotalRevenue).sum();
        double totalDistance = stats.stream().mapToDouble(DeliveryStats::getTotalDistanceKm).sum();

        long totalUsers = appUserRepository.count();
        long activeDrivers = appUserRepository.countByRoleAndIsActiveTrue(UserRole.AGENT);
        long activeCustomers = appUserRepository.countByRoleAndIsActiveTrue(UserRole.CLIENT);

        return new PlatformStatsDTO(
                totalDeliveries,
                completedDeliveries,
                totalRevenue,
                totalDistance,
                totalUsers,
                activeDrivers,
                activeCustomers
        );
    }

    // DAILY STATS ------------------------------------------------------------

    @Override
    @Transactional
    public void generateDailyStats(LocalDate date) {

        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.atTime(23, 59, 59);

        List<Delivery> list = deliveryRepository.findByCreatedAtBetween(startTime, endTime);

        int total = list.size();
        int completed = (int) list.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.DELIVERED)
                .count();

        int cancelled = (int) list.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.CANCELLED)
                .count();

        double revenue = list.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.DELIVERED)
                .mapToDouble(d ->
                        d.getDeliveryPayment() != null && d.getDeliveryPayment().getFinalAmount() != null
                                ? d.getDeliveryPayment().getFinalAmount().doubleValue() : 0.0)
                .sum();

        double distance = list.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.DELIVERED)
                .mapToDouble(d -> d.getDistanceKm() != null ? d.getDistanceKm() : 0.0)
                .sum();

        DeliveryStats stats = deliveryStatsRepository.findByStatDate(date)
                .orElse(new DeliveryStats());

        stats.setStatDate(date);
        stats.setTotalDeliveries(total);
        stats.setCompletedDeliveries(completed);
        stats.setCancelledDeliveries(cancelled);
        stats.setTotalRevenue(revenue);
        stats.setTotalDistanceKm(distance);

        deliveryStatsRepository.save(stats);
    }
}
