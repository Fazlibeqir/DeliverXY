package com.deliverXY.backend.NewCode.deliveries.repository;

import com.deliverXY.backend.NewCode.deliveries.domain.PricingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PricingConfigRepository extends JpaRepository<PricingConfig, Long> {

    PricingConfig findByCityAndIsActiveTrue(String city);

}
