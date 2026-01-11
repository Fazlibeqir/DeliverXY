package com.deliverXY.backend.NewCode.deliveries.service;

import com.deliverXY.backend.NewCode.deliveries.domain.PricingConfig;

public interface PricingConfigService {
    PricingConfig getActivePricing(String city);
}
