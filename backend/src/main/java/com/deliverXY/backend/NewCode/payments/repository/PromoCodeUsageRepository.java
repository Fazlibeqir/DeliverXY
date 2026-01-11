package com.deliverXY.backend.NewCode.payments.repository;

import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.payments.domain.PromoCode;
import com.deliverXY.backend.NewCode.payments.domain.PromoCodeUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoCodeUsageRepository extends JpaRepository<PromoCodeUsage, Long> {
    
    Long countByPromoCodeAndUser(PromoCode promoCode, AppUser user);
    
    List<PromoCodeUsage> findByUserOrderByUsedAtDesc(AppUser user);
}
