package com.deliverXY.backend.repositories;

import com.deliverXY.backend.models.DeliveryStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryStatsRepository extends JpaRepository<DeliveryStats, Long> {

    Optional<DeliveryStats> findByStatDate(LocalDate date);

    List<DeliveryStats> findByStatDateBetweenOrderByStatDateDesc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(ds.totalRevenue) FROM DeliveryStats ds WHERE ds.statDate BETWEEN :startDate AND :endDate")
    Double getTotalRevenueBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(ds.completedDeliveries) FROM DeliveryStats ds WHERE ds.statDate BETWEEN :startDate AND :endDate")
    Integer getTotalDeliveriesBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}