package com.deliverXY.backend.NewCode.admin.service.impl;

import com.deliverXY.backend.NewCode.admin.dto.AdminEarningsDTO;
import com.deliverXY.backend.NewCode.admin.service.AdminEarningsService;
import com.deliverXY.backend.NewCode.earnings.repository.DriverEarningsRepository;
import com.deliverXY.backend.NewCode.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEarningsServiceImpl implements AdminEarningsService {

    private final DriverEarningsRepository earningsRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminEarningsDTO getEarnings() {
        BigDecimal driverTotal = safe(earningsRepository.sumDriverEarnings());
        BigDecimal tipTotal = safe(earningsRepository.sumTips());
        long deliveryCount = earningsRepository.countAllEarnings();

        BigDecimal platformRevenue = safe(paymentRepository.sumPositivePlatformFees())
                .add(safe(paymentRepository.sumDerivedPlatformFees()))
                .add(safe(earningsRepository.sumFallbackPlatformRevenue()));

        log.info(
                "Admin earnings - platform: {}, driver: {}, tips: {}, deliveries: {}",
                platformRevenue, driverTotal, tipTotal, deliveryCount
        );

        AdminEarningsDTO dto = new AdminEarningsDTO();
        dto.setTotalDriverEarnings(driverTotal.add(tipTotal));
        dto.setTotalPlatformRevenue(platformRevenue);
        dto.setTotalDelivered(deliveryCount);
        return dto;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
