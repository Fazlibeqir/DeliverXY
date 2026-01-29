package com.deliverXY.backend.NewCode.deliveries.service.impl;

import com.deliverXY.backend.NewCode.deliveries.domain.PricingConfig;
import com.deliverXY.backend.NewCode.deliveries.repository.PricingConfigRepository;
import com.deliverXY.backend.NewCode.deliveries.service.PricingConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingConfigServiceImpl implements PricingConfigService {

    private final PricingConfigRepository repo;

    @Override
    public List<PricingConfig> findAll() {
        return repo.findAll();
    }

    @Override
    public Optional<PricingConfig> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    @Transactional
    public PricingConfig save(PricingConfig config) {
        return repo.save(config);
    }

    @Override
    @Transactional
    public PricingConfig getActivePricing(String city) {
        PricingConfig config = repo.findByCityAndIsActiveTrue(city);
        
        if (config == null) {
            log.warn("No active pricing config found for city: {}. Creating default configuration.", city);
            PricingConfig defaultConfig = createDefaultPricingConfig(city);
            config = repo.save(defaultConfig);
            log.info("Created default pricing config for city: {} with ID: {}", city, config.getId());
        }
        
        return config;
    }

    /**
     * Creates a default pricing configuration for a city
     */
    private PricingConfig createDefaultPricingConfig(String city) {
        PricingConfig config = new PricingConfig();
        config.setName("Standard " + city);
        config.setCity(city);
        config.setCurrency("MKD");
        config.setBaseFare(50.0);
        config.setPerKmRate(30.0);
        config.setPerMinuteRate(2.0);
        config.setMinimumFare(80.0);
        config.setSurgeMultiplier(1.0);
        config.setCityCenterMultiplier(1.1);
        config.setAirportSurcharge(100.0);
        config.setNightMultiplier(1.25);
        config.setWeekendMultiplier(1.15);
        config.setPeakHourMultiplier(1.3);
        config.setIsActive(true);
        config.setPlatformCommissionPercent(20.0);
        config.setDescription("Default pricing configuration for " + city);
        return config;
    }
}
