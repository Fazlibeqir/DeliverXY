package com.deliverXY.backend.NewCode.deliveries.service;

import com.deliverXY.backend.NewCode.deliveries.domain.PricingConfig;

import java.util.List;
import java.util.Optional;

public interface PricingConfigService {
    PricingConfig getActivePricing(String city);

    List<PricingConfig> findAll();

    Optional<PricingConfig> findById(Long id);

    PricingConfig save(PricingConfig config);
}
