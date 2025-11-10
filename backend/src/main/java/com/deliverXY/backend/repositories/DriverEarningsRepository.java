package com.deliverXY.backend.repositories;

import com.deliverXY.backend.enums.PayoutStatus;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.DriverEarnings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DriverEarningsRepository extends JpaRepository<DriverEarnings, Long> {

    List<DriverEarnings> findByDriverOrderByEarnedDateDesc(AppUser driver);

    Page<DriverEarnings> findByDriverOrderByEarnedDateDesc(AppUser driver, Pageable pageable);

    List<DriverEarnings> findByDriverAndEarnedDateBetweenOrderByEarnedDateDesc(
            AppUser driver, LocalDate startDate, LocalDate endDate);

    List<DriverEarnings> findByDriverAndPayoutStatusOrderByEarnedDateDesc(
            AppUser driver, PayoutStatus status);

    @Query("SELECT SUM(de.totalEarning) FROM DriverEarnings de " +
            "WHERE de.driver = :driver AND de.earnedDate BETWEEN :startDate AND :endDate")
    Double getTotalEarnings(@Param("driver") AppUser driver,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(de.totalEarning) FROM DriverEarnings de " +
            "WHERE de.driver = :driver AND de.payoutStatus = :status")
    Double getPendingEarnings(@Param("driver") AppUser driver,
                              @Param("status") PayoutStatus status);

    Long countByDriverAndEarnedDateBetween(AppUser driver, LocalDate startDate, LocalDate endDate);
}