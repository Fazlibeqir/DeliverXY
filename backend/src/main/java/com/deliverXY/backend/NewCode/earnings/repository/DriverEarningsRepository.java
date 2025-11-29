package com.deliverXY.backend.NewCode.earnings.repository;

import com.deliverXY.backend.NewCode.earnings.domain.DriverEarnings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface DriverEarningsRepository extends JpaRepository<DriverEarnings, Long> {

    List<DriverEarnings> findAllByCreatedAtBetween(LocalDateTime lastWeek, LocalDateTime now);

    Page<DriverEarnings> findByAgentId(Long driverId, Pageable pageable);
}