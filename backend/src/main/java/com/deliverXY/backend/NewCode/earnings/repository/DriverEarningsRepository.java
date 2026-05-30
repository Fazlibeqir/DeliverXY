package com.deliverXY.backend.NewCode.earnings.repository;

import com.deliverXY.backend.NewCode.earnings.domain.DriverEarnings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface DriverEarningsRepository extends JpaRepository<DriverEarnings, Long> {

    List<DriverEarnings> findAllByCreatedAtBetween(LocalDateTime lastWeek, LocalDateTime now);

    Page<DriverEarnings> findByAgentId(Long driverId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.driverEarnings), 0) FROM DriverEarnings e")
    BigDecimal sumDriverEarnings();

    @Query("SELECT COALESCE(SUM(e.tip), 0) FROM DriverEarnings e")
    BigDecimal sumTips();

    @Query("SELECT COUNT(e) FROM DriverEarnings e")
    long countAllEarnings();

    @Query(value = """
            SELECT COALESCE(SUM(de.driver_earnings * 0.25), 0)
            FROM driver_earnings de
            WHERE NOT EXISTS (SELECT 1 FROM payments p WHERE p.delivery_id = de.delivery_id)
              AND COALESCE(de.driver_earnings, 0) > 0
            """, nativeQuery = true)
    BigDecimal sumFallbackPlatformRevenue();

    @Query("""
            SELECT COALESCE(SUM(e.driverEarnings), 0), COALESCE(SUM(e.tip), 0), COUNT(e)
            FROM DriverEarnings e
            WHERE e.agentId = :agentId AND e.createdAt BETWEEN :start AND :end
            """)
    Object[] sumByAgentAndPeriod(
            @Param("agentId") Long agentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            SELECT COALESCE(SUM(d.distanceKm), 0)
            FROM DriverEarnings e JOIN e.delivery d
            WHERE e.agentId = :agentId AND e.createdAt BETWEEN :start AND :end
            """)
    Double sumDistanceByAgentAndPeriod(
            @Param("agentId") Long agentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}