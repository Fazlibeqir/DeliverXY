package com.deliverXY.backend.NewCode.deliveries.repository;

import com.deliverXY.backend.NewCode.deliveries.domain.DeliveryStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DeliveryStatsRepository extends JpaRepository<DeliveryStats, Long> {

    Optional<DeliveryStats> findByStatDate(LocalDate date);

    Page<DeliveryStats> findByStatDateBetweenOrderByStatDateDesc(
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable);
}
