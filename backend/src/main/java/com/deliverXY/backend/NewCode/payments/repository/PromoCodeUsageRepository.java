package com.deliverXY.backend.NewCode.payments.repository;

import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.payments.domain.PromoCode;
import com.deliverXY.backend.NewCode.payments.domain.PromoCodeUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromoCodeUsageRepository extends JpaRepository<PromoCodeUsage, Long> {

    Long countByPromoCodeAndUser(PromoCode promoCode, AppUser user);

    Page<PromoCodeUsage> findByUserOrderByUsedAtDesc(AppUser user, Pageable pageable);
}
