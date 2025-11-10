package com.deliverXY.backend.repositories;

import com.deliverXY.backend.models.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    Optional<PromoCode> findByCodeIgnoreCase(String code);

    @Query("SELECT p FROM PromoCode p WHERE p.isActive = true " +
            "AND (p.startDate IS NULL OR p.startDate <= :now) " +
            "AND (p.endDate IS NULL OR p.endDate >= :now) " +
            "AND (p.usageLimit IS NULL OR p.currentUsage < p.usageLimit)")
    List<PromoCode> findAllActivePromoCodes(@Param("now") LocalDateTime now);

    boolean existsByCodeIgnoreCase(String code);
}