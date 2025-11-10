package com.deliverXY.backend.repositories;

import com.deliverXY.backend.enums.PayoutStatus;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.Payout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {

    List<Payout> findByDriverOrderByCreatedAtDesc(AppUser driver);

    Page<Payout> findByDriverOrderByCreatedAtDesc(AppUser driver, Pageable pageable);

    List<Payout> findByStatus(PayoutStatus status);

    List<Payout> findByDriverAndPeriodStartBetween(AppUser driver, LocalDate start, LocalDate end);
}