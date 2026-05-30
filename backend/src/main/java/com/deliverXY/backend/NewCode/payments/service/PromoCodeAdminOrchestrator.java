package com.deliverXY.backend.NewCode.payments.service;

import com.deliverXY.backend.NewCode.payments.domain.PromoCode;
import com.deliverXY.backend.NewCode.payments.dto.PromoCodeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PromoCodeAdminOrchestrator {

    private final PromoCodeService promoCodeService;

    @Transactional
    public PromoCode createFromDto(PromoCodeDTO dto, String adminUsername) {
        PromoCode promoCode = new PromoCode();
        promoCode.setCode(dto.getCode().toUpperCase());
        promoCode.setDescription(dto.getDescription());
        promoCode.setDiscountType(dto.getDiscountType());
        promoCode.setDiscountValue(dto.getDiscountValue());
        promoCode.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        promoCode.setMinOrderAmount(dto.getMinOrderAmount());
        promoCode.setUsageLimit(dto.getUsageLimit());
        promoCode.setUsagePerUser(dto.getUsagePerUser());
        promoCode.setStartDate(dto.getStartDate());
        promoCode.setEndDate(dto.getEndDate());
        promoCode.setIsActive(dto.getIsActive());
        promoCode.setIsFirstOrderOnly(dto.getIsFirstOrderOnly());
        promoCode.setApplicableForNewUsersOnly(dto.getApplicableForNewUsersOnly());
        return promoCodeService.createPromoCode(promoCode, adminUsername);
    }
}
