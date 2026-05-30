package com.deliverXY.backend.NewCode.deliveries.service.impl;

import com.deliverXY.backend.NewCode.deliveries.domain.PricingConfig;
import com.deliverXY.backend.NewCode.deliveries.repository.PricingConfigRepository;
import com.deliverXY.backend.NewCode.deliveries.service.PricingConfigBootstrapService;
import com.deliverXY.backend.NewCode.deliveries.service.PricingConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingConfigServiceImpl implements PricingConfigService {

    private final PricingConfigRepository repo;
    private final PricingConfigBootstrapService pricingConfigBootstrapService;

    @Override
    @Transactional(readOnly = true)
    public Page<PricingConfig> findAll(@NonNull Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PricingConfig> findById(@NonNull Long id) {
        return repo.findById(id);
    }

    @Override
    @Transactional
    public @NonNull PricingConfig save(@NonNull PricingConfig config) {
        return Objects.requireNonNull(repo.save(config));
    }

    @Override
    @Transactional(readOnly = true)
    public @NonNull PricingConfig getActivePricing(String city) {
        PricingConfig existing = repo.findByCityAndIsActiveTrue(city);
        if (existing != null) {
            return existing;
        }

        log.warn("No active pricing config found for city: {}. Creating default configuration.", city);
        return pricingConfigBootstrapService.createDefaultForCity(city);
    }
}
