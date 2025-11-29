package com.deliverXY.backend.NewCode.admin.service.impl;

import com.deliverXY.backend.NewCode.admin.dto.AdminEarningsDTO;
import com.deliverXY.backend.NewCode.admin.service.AdminEarningsService;
import com.deliverXY.backend.NewCode.earnings.repository.DriverEarningsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminEarningsServiceImpl implements AdminEarningsService {

    private final DriverEarningsRepository earningsRepository;

    @Override
    public AdminEarningsDTO getEarnings() {
        var list = earningsRepository.findAll();

        BigDecimal driverTotal = list.stream()
                .map(e->e.getDriverEarnings())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tipTotal = list.stream()
                .map(e->e.getTip())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal platformRevenue = list.stream()
                .map(e -> {
                    var delivery = e.getDelivery();
                    if (delivery == null || delivery.getDeliveryPayment() == null) {
                        return BigDecimal.ZERO; // avoid NPE
                    }

                    BigDecimal finalAmount = safe(delivery.getDeliveryPayment().getFinalAmount());
                    BigDecimal driverEarn = safe(e.getDriverEarnings());
                    BigDecimal tip = safe(e.getTip());

                    return finalAmount.subtract(driverEarn).subtract(tip);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        AdminEarningsDTO dto = new AdminEarningsDTO();
        dto.setTotalDriverEarnings(driverTotal.add(tipTotal));
        dto.setTotalPlatformRevenue(platformRevenue);
        dto.setTotalDelivered(BigDecimal.valueOf(list.size()));
        return dto;
    }
    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
