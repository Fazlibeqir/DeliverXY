package com.deliverXY.backend.NewCode.earnings.repository;

import com.deliverXY.backend.NewCode.common.enums.PayoutStatus;
import com.deliverXY.backend.NewCode.earnings.domain.DriverPayout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverPayoutRepository extends JpaRepository<DriverPayout, Long> {

    List<DriverPayout> findByDriverIdOrderByPaidAtDesc(Long driverId);

    Page<DriverPayout> findByDriverId(Long driverId, Pageable pageable);

    Page<DriverPayout> findByStatus(PayoutStatus status,Pageable pageable);
}
