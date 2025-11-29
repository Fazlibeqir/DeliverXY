package com.deliverXY.backend.NewCode.deliveries.service.impl;

import com.deliverXY.backend.NewCode.deliveries.domain.PricingConfig;
import com.deliverXY.backend.NewCode.deliveries.repository.PricingConfigRepository;
import com.deliverXY.backend.NewCode.deliveries.service.PricingConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PricingConfigServiceImpl implements PricingConfigService {

    private final PricingConfigRepository repo;

    @Override
    public PricingConfig getActivePricing(String city) {
        PricingConfig config = repo.findByCityAndIsActiveTrue(city);
        if (config == null) throw new RuntimeException("No active pricing config for: " + city);
        return config;
    }
}
