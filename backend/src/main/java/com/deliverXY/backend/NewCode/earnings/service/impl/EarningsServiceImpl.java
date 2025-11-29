package com.deliverXY.backend.NewCode.earnings.service.impl;

import com.deliverXY.backend.NewCode.earnings.domain.DriverPayout;
import com.deliverXY.backend.NewCode.earnings.dto.DriverEarningsDTO;
import com.deliverXY.backend.NewCode.earnings.dto.DriverPayoutDTO;
import com.deliverXY.backend.NewCode.earnings.dto.EarningsSummaryDTO;
import com.deliverXY.backend.NewCode.earnings.dto.PayoutRequestDTO;
import com.deliverXY.backend.NewCode.earnings.repository.DriverEarningsRepository;
import com.deliverXY.backend.NewCode.earnings.repository.DriverPayoutRepository;
import com.deliverXY.backend.NewCode.earnings.service.EarningsService;
import com.deliverXY.backend.NewCode.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EarningsServiceImpl implements EarningsService {

    private final DriverEarningsRepository earningsRepo;
    private final DriverPayoutRepository payoutRepo;
    private final WalletService walletService;

    @Override
    public EarningsSummaryDTO getDriverSummary(Long driverId, LocalDate start, LocalDate end) {
        var earnings = earningsRepo.findAllByCreatedAtBetween(
                        start.atStartOfDay(),
                        end.atTime(23, 59, 59)
                ).stream()
                .filter(e -> e.getAgentId().equals(driverId))
                .toList();

        double totalEarned = earnings.stream()
                .mapToDouble(e -> e.getDriverEarnings().doubleValue())
                .sum();

        double totalTips = earnings.stream()
                .mapToDouble(e -> e.getTip().doubleValue())
                .sum();

        double totalDistance = earnings.stream()
                .mapToDouble(e -> e.getDelivery().getDistanceKm())
                .sum();

        return new EarningsSummaryDTO(
                totalEarned,
                totalTips,
                earnings.size(),
                totalDistance
        );
    }

    @Override
    public Page<DriverEarningsDTO> getDriverEarnings(Long driverId, Pageable pageable) {
        return earningsRepo.findByAgentId(driverId, pageable)
                .map(e -> new DriverEarningsDTO(
                        e.getDeliveryId(),
                        e.getDriverEarnings().doubleValue(),
                        e.getTip().doubleValue(),
                        e.getCreatedAt().toString()
                ));
    }

    @Override
    public DriverPayoutDTO requestManualPayout(Long driverId, PayoutRequestDTO request) {
        var earnings = earningsRepo.findAllByCreatedAtBetween(
                        request.getPeriodStart().atStartOfDay(),
                        request.getPeriodEnd().atTime(23, 59, 59)
                ).stream()
                .filter(e -> e.getAgentId().equals(driverId))
                .toList();

        double total = earnings.stream()
                .mapToDouble(e -> e.getDriverEarnings().doubleValue())
                .sum();

        walletService.deposit(driverId, BigDecimal.valueOf(total), "Manual payout");

        var payout = DriverPayout.builder()
                .driverId(driverId)
                .amountPaid(BigDecimal.valueOf(total))
                .periodStart(request.getPeriodStart().atStartOfDay())
                .periodEnd(request.getPeriodEnd().atTime(23, 59, 59))
                .build();

        payoutRepo.save(payout);

        return new DriverPayoutDTO(
                payout.getId(),
                payout.getAmountPaid().doubleValue(),
                payout.getPeriodStart().toString(),
                payout.getPeriodEnd().toString(),
                payout.getPaidAt().toString(),
                "PAID"
        );
    }

    @Override
    public Page<DriverPayoutDTO> getPayoutHistory(Long driverId, Pageable pageable) {
        return payoutRepo.findByDriverId(driverId, pageable)
                .map(p -> new DriverPayoutDTO(
                        p.getId(),
                        p.getAmountPaid().doubleValue(),
                        p.getPeriodStart().toString(),
                        p.getPeriodEnd().toString(),
                        p.getPaidAt().toString(),
                        "PAID"
                ));
    }

    @Override
    public Page<DriverPayoutDTO> getPendingPayouts(Pageable pageable) {
        return payoutRepo.findPending(pageable)
                .map(p -> new DriverPayoutDTO(
                        p.getId(),
                        p.getAmountPaid().doubleValue(),
                        p.getPeriodStart().toString(),
                        p.getPeriodEnd().toString(),
                        p.getPaidAt().toString(),
                        "PENDING"
                ));
    }

    @Override
    public void processPayout(Long payoutId, String transactionRef, String processedBy) {
        var payout = payoutRepo.findById(payoutId)
                .orElseThrow(() -> new RuntimeException("Payout not found"));

        payout.setPaidAt(LocalDateTime.now());
        payoutRepo.save(payout);
    }
}