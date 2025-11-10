package com.deliverXY.backend.repositories;

import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.PromoCode;
import com.deliverXY.backend.models.PromoCodeUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoCodeUsageRepository extends JpaRepository<PromoCodeUsage, Long> {
    
    Long countByPromoCodeAndUser(PromoCode promoCode, AppUser user);
    
    List<PromoCodeUsage> findByUserOrderByUsedAtDesc(AppUser user);
    
    List<PromoCodeUsage> findByPromoCodeOrderByUsedAtDesc(PromoCode promoCode);
}
