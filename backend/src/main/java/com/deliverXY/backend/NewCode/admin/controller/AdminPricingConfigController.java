package com.deliverXY.backend.NewCode.admin.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.deliveries.domain.PricingConfig;
import com.deliverXY.backend.NewCode.deliveries.dto.PricingConfigDTO;
import com.deliverXY.backend.NewCode.deliveries.service.PricingConfigService;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/pricing-config")
@RequiredArgsConstructor
public class AdminPricingConfigController {

    private final PricingConfigService pricingConfigService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<PricingConfigDTO>> list() {
        List<PricingConfigDTO> list = pricingConfigService.findAll().stream()
                .map(AdminPricingConfigController::toDTO)
                .collect(Collectors.toList());
        return ApiResponse.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PricingConfigDTO> getById(@PathVariable Long id) {
        PricingConfig config = pricingConfigService.findById(id)
                .orElseThrow(() -> new NotFoundException("Pricing config not found: " + id));
        return ApiResponse.ok(toDTO(config));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PricingConfigDTO> update(@PathVariable Long id, @RequestBody PricingConfigDTO dto) {
        PricingConfig config = pricingConfigService.findById(id)
                .orElseThrow(() -> new NotFoundException("Pricing config not found: " + id));
        applyDTO(config, dto);
        config = pricingConfigService.save(config);
        return ApiResponse.ok(toDTO(config));
    }

    private static PricingConfigDTO toDTO(PricingConfig c) {
        return PricingConfigDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .city(c.getCity())
                .currency(c.getCurrency())
                .baseFare(c.getBaseFare())
                .perKmRate(c.getPerKmRate())
                .perMinuteRate(c.getPerMinuteRate())
                .minimumFare(c.getMinimumFare())
                .surgeMultiplier(c.getSurgeMultiplier())
                .cityCenterMultiplier(c.getCityCenterMultiplier())
                .airportSurcharge(c.getAirportSurcharge())
                .nightMultiplier(c.getNightMultiplier())
                .weekendMultiplier(c.getWeekendMultiplier())
                .peakHourMultiplier(c.getPeakHourMultiplier())
                .isActive(c.getIsActive())
                .platformCommissionPercent(c.getPlatformCommissionPercent())
                .description(c.getDescription())
                .build();
    }

    private static void applyDTO(PricingConfig config, PricingConfigDTO dto) {
        if (dto.getName() != null) config.setName(dto.getName());
        if (dto.getCity() != null) config.setCity(dto.getCity());
        if (dto.getCurrency() != null) config.setCurrency(dto.getCurrency());
        if (dto.getBaseFare() != null) config.setBaseFare(dto.getBaseFare());
        if (dto.getPerKmRate() != null) config.setPerKmRate(dto.getPerKmRate());
        if (dto.getPerMinuteRate() != null) config.setPerMinuteRate(dto.getPerMinuteRate());
        if (dto.getMinimumFare() != null) config.setMinimumFare(dto.getMinimumFare());
        if (dto.getSurgeMultiplier() != null) config.setSurgeMultiplier(dto.getSurgeMultiplier());
        if (dto.getCityCenterMultiplier() != null) config.setCityCenterMultiplier(dto.getCityCenterMultiplier());
        if (dto.getAirportSurcharge() != null) config.setAirportSurcharge(dto.getAirportSurcharge());
        if (dto.getNightMultiplier() != null) config.setNightMultiplier(dto.getNightMultiplier());
        if (dto.getWeekendMultiplier() != null) config.setWeekendMultiplier(dto.getWeekendMultiplier());
        if (dto.getPeakHourMultiplier() != null) config.setPeakHourMultiplier(dto.getPeakHourMultiplier());
        if (dto.getIsActive() != null) config.setIsActive(dto.getIsActive());
        if (dto.getPlatformCommissionPercent() != null) config.setPlatformCommissionPercent(dto.getPlatformCommissionPercent());
        if (dto.getDescription() != null) config.setDescription(dto.getDescription());
    }
}
