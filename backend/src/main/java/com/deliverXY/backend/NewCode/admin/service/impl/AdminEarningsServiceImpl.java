package com.deliverXY.backend.NewCode.admin.service.impl;

import com.deliverXY.backend.NewCode.admin.dto.AdminEarningsDTO;
import com.deliverXY.backend.NewCode.admin.service.AdminEarningsService;
import com.deliverXY.backend.NewCode.earnings.repository.DriverEarningsRepository;
import com.deliverXY.backend.NewCode.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEarningsServiceImpl implements AdminEarningsService {

    private final DriverEarningsRepository earningsRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public AdminEarningsDTO getEarnings() {
        var list = earningsRepository.findAll();
        
        log.info("Calculating earnings for {} driver earnings records", list.size());

        BigDecimal driverTotal = list.stream()
                .map(e->safe(e.getDriverEarnings()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tipTotal = list.stream()
                .map(e->safe(e.getTip()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate platform revenue from Payment entity or DriverEarnings
        BigDecimal platformRevenue = BigDecimal.ZERO;
        
        for (var e : list) {
            Long deliveryId = e.getDeliveryId();
            if (deliveryId == null) {
                log.warn("DriverEarnings has null deliveryId: {}", e);
                continue;
            }

            BigDecimal driverEarn = safe(e.getDriverEarnings());
            BigDecimal calculatedRevenue = BigDecimal.ZERO;
            
            // Get Payment entity for this delivery
            var paymentOpt = paymentRepository.findByDeliveryId(deliveryId);
            
            if (paymentOpt.isPresent()) {
                var payment = paymentOpt.get();
                
                // Use platformFee if set and > 0
                BigDecimal platformFee = safe(payment.getPlatformFee());
                if (platformFee.compareTo(BigDecimal.ZERO) > 0) {
                    log.info("Using platformFee from Payment for delivery {}: {}", deliveryId, platformFee);
                    calculatedRevenue = platformFee;
                } else {
                    // Calculate from payment amount if available
                    BigDecimal totalAmount = safe(payment.getAmount());
                    if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal driverAmount = safe(payment.getDriverAmount());
                        BigDecimal paymentTip = safe(payment.getTip());
                        // If driverAmount is set, use it; otherwise use driverEarnings
                        BigDecimal actualDriverAmount = driverAmount.compareTo(BigDecimal.ZERO) > 0 
                            ? driverAmount 
                            : driverEarn;
                        BigDecimal calculatedFee = totalAmount.subtract(actualDriverAmount).subtract(paymentTip);
                        if (calculatedFee.compareTo(BigDecimal.ZERO) > 0) {
                            log.info("Calculated platformFee from Payment for delivery {}: {} (total: {}, driver: {}, tip: {})", 
                                deliveryId, calculatedFee, totalAmount, actualDriverAmount, paymentTip);
                            calculatedRevenue = calculatedFee;
                        } else {
                            log.warn("Calculated fee is <= 0 for delivery {}: total={}, driver={}, tip={}, fee={}", 
                                deliveryId, totalAmount, actualDriverAmount, paymentTip, calculatedFee);
                        }
                    } else {
                        log.warn("Payment amount is 0 or null for delivery {}", deliveryId);
                    }
                }
            } else {
                log.warn("No Payment found for delivery {}", deliveryId);
            }
            
            // Fallback: Calculate from driver earnings (20% platform fee = driver gets 80%)
            // If driver earned X (80%), then platform earned X * 0.25 (which is 20% of total)
            // Formula: platform = driverEarnings * 0.25 (since driver = 80% of total, platform = 20% = driver * 0.25)
            if (calculatedRevenue.compareTo(BigDecimal.ZERO) == 0 && driverEarn.compareTo(BigDecimal.ZERO) > 0) {
                calculatedRevenue = driverEarn.multiply(new BigDecimal("0.25")).setScale(2, java.math.RoundingMode.HALF_UP);
                log.info("Using fallback calculation for delivery {}: platformFee = {} (driver earned: {})", deliveryId, calculatedRevenue, driverEarn);
            }
            
            if (calculatedRevenue.compareTo(BigDecimal.ZERO) == 0) {
                log.warn("No platform revenue calculated for delivery {}: driverEarn={}", deliveryId, driverEarn);
            }
            
            platformRevenue = platformRevenue.add(calculatedRevenue);
        }

        log.info("Final earnings calculation - Platform Revenue: {}, Driver Earnings: {}, Tips: {}, Total Deliveries: {}", 
            platformRevenue, driverTotal, tipTotal, list.size());

        AdminEarningsDTO dto = new AdminEarningsDTO();
        dto.setTotalDriverEarnings(driverTotal.add(tipTotal));
        dto.setTotalPlatformRevenue(platformRevenue);
        dto.setTotalDelivered((long)list.size());
        return dto;
    }
    
    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
