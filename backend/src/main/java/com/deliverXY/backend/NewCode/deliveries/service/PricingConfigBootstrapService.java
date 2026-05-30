package com.deliverXY.backend.NewCode.deliveries.service;

import com.deliverXY.backend.NewCode.deliveries.domain.PricingConfig;
import com.deliverXY.backend.NewCode.deliveries.repository.PricingConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingConfigBootstrapService {

    private final PricingConfigRepository repo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public @NonNull PricingConfig createDefaultForCity(String city) {
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

        PricingConfig saved = Objects.requireNonNull(repo.save(config));
        log.info("Created default pricing config for city: {} with ID: {}", city, saved.getId());
        return saved;
    }
}
