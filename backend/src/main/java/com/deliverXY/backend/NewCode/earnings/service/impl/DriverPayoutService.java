package com.deliverXY.backend.NewCode.earnings.service.impl;

import com.deliverXY.backend.NewCode.earnings.domain.DriverEarnings;
import com.deliverXY.backend.NewCode.earnings.domain.DriverPayout;
import com.deliverXY.backend.NewCode.earnings.repository.DriverPayoutRepository;
import com.deliverXY.backend.NewCode.earnings.repository.DriverEarningsRepository;
import com.deliverXY.backend.NewCode.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DriverPayoutService {
    private final DriverPayoutRepository driverPayoutRepository;
    private final DriverEarningsRepository earningsRepository;
    private final WalletService walletService;

    @Scheduled( cron = "0 0 3 * * MON")
    public void generateWeeklyPayout(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastWeek = now.minusDays(7);

        var toPay = earningsRepository.findAllByCreatedAtBetween(lastWeek,now);

        Map<Long, BigDecimal> grouped = new HashMap<>();

        for(DriverEarnings e : toPay){
            grouped.merge(e.getAgentId(), e.getDriverEarnings(), BigDecimal::add);
        }

        for (var entry : grouped.entrySet()){
            Long agentId = entry.getKey();
            BigDecimal amount = entry.getValue();

            walletService.deposit(agentId, amount, "Weekly payout");

            driverPayoutRepository.save(DriverPayout.builder()
                    .driverId(agentId)
                    .amountPaid(amount)
                    .periodStart(lastWeek)
                    .periodEnd(now)
                    .build()
            );
        }
    }
}
