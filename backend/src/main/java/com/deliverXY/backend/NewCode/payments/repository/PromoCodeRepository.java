package com.deliverXY.backend.NewCode.payments.repository;

import com.deliverXY.backend.NewCode.payments.domain.PromoCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    Optional<PromoCode> findByCodeIgnoreCase(String code);

    @Query("SELECT p FROM PromoCode p WHERE p.isActive = true " +
            "AND (p.startDate IS NULL OR p.startDate <= :now) " +
            "AND (p.endDate IS NULL OR p.endDate >= :now) " +
            "AND (p.usageLimit IS NULL OR p.currentUsage < p.usageLimit)")
    Page<PromoCode> findAllActivePromoCodes(@Param("now") LocalDateTime now, Pageable pageable);

    boolean existsByCodeIgnoreCase(String code);
}
