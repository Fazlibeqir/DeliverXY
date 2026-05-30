package com.deliverXY.backend.NewCode.earnings.service.impl;

import com.deliverXY.backend.NewCode.earnings.domain.DriverEarnings;
import com.deliverXY.backend.NewCode.earnings.domain.DriverPayout;
import com.deliverXY.backend.NewCode.earnings.repository.DriverEarningsRepository;
import com.deliverXY.backend.NewCode.earnings.repository.DriverPayoutRepository;
import com.deliverXY.backend.NewCode.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DriverPayoutJobService {

    private final DriverPayoutRepository driverPayoutRepository;
    private final DriverEarningsRepository earningsRepository;
    private final WalletService walletService;

    @Transactional
    public void processWeeklyPayouts(LocalDateTime periodStart, LocalDateTime periodEnd) {
        List<DriverEarnings> toPay = earningsRepository.findAllByCreatedAtBetween(periodStart, periodEnd);

        Map<Long, BigDecimal> grouped = new HashMap<>();
        for (DriverEarnings earnings : toPay) {
            Long agentId = earnings.getAgentId();
            BigDecimal amount = earnings.getDriverEarnings();
            grouped.compute(agentId, (id, existing) ->
                    existing == null ? amount : existing.add(amount));
        }

        List<DriverPayout> payouts = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : grouped.entrySet()) {
            Long agentId = entry.getKey();
            BigDecimal amount = entry.getValue();

            walletService.deposit(agentId, amount, "Weekly payout");

            payouts.add(DriverPayout.builder()
                    .driverId(agentId)
                    .amountPaid(amount)
                    .periodStart(periodStart)
                    .periodEnd(periodEnd)
                    .build());
        }

        if (!payouts.isEmpty()) {
            driverPayoutRepository.saveAll(payouts);
        }
    }
}
