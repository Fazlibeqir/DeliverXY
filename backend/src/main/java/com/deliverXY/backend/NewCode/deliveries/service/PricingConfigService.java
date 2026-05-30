package com.deliverXY.backend.NewCode.deliveries.service;

import com.deliverXY.backend.NewCode.deliveries.domain.PricingConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface PricingConfigService {
    @NonNull PricingConfig getActivePricing(String city);

    Page<PricingConfig> findAll(@NonNull Pageable pageable);

    Optional<PricingConfig> findById(@NonNull Long id);

    @NonNull PricingConfig save(@NonNull PricingConfig config);
}
