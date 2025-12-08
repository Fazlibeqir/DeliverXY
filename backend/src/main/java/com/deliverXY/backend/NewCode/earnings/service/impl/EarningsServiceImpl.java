package com.deliverXY.backend.NewCode.earnings.service.impl;

import com.deliverXY.backend.NewCode.common.enums.PayoutStatus;
import com.deliverXY.backend.NewCode.earnings.domain.DriverPayout;
import com.deliverXY.backend.NewCode.earnings.dto.DriverEarningsDTO;
import com.deliverXY.backend.NewCode.earnings.dto.DriverPayoutDTO;
import com.deliverXY.backend.NewCode.earnings.dto.EarningsSummaryDTO;
import com.deliverXY.backend.NewCode.earnings.dto.PayoutRequestDTO;
import com.deliverXY.backend.NewCode.earnings.repository.DriverEarningsRepository;
import com.deliverXY.backend.NewCode.earnings.repository.DriverPayoutRepository;
import com.deliverXY.backend.NewCode.earnings.service.EarningsService;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
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

        BigDecimal totalEarned = earnings.stream()
                .map(e -> e.getDriverEarnings())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTips = earnings.stream()
                .map(e -> e.getTip())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double totalDistance = earnings.stream()
                .mapToDouble(e -> e.getDelivery().getDistanceKm())
                .sum();

        return new EarningsSummaryDTO(
                totalEarned,
                totalTips,
                (long)earnings.size(),
                totalDistance
        );
    }

    @Override
    public Page<DriverEarningsDTO> getDriverEarnings(Long driverId, Pageable pageable) {
        return earningsRepo.findByAgentId(driverId, pageable)
                .map(e -> new DriverEarningsDTO(
                        e.getDeliveryId(),
                        e.getDriverEarnings(),
                        e.getTip(),
                        e.getCreatedAt().toString()
                ));
    }

    @Override
    @Transactional
    public DriverPayoutDTO requestManualPayout(Long driverId, PayoutRequestDTO request) {
        var earnings = earningsRepo.findAllByCreatedAtBetween(
                        request.getPeriodStart().atStartOfDay(),
                        request.getPeriodEnd().atTime(23, 59, 59)
                ).stream()
                .filter(e -> e.getAgentId().equals(driverId))
                .toList();

        BigDecimal total = earnings.stream()
                .map(e -> e.getDriverEarnings())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var payout = DriverPayout.builder()
                .driverId(driverId)
                .amountPaid(total)
                .periodStart(request.getPeriodStart().atStartOfDay())
                .periodEnd(request.getPeriodEnd().atTime(23, 59, 59))
                .status(PayoutStatus.PENDING)
                .build();

        payoutRepo.save(payout);

        return new DriverPayoutDTO(
                payout.getId(),
                payout.getAmountPaid(),
                payout.getPeriodStart().toString(),
                payout.getPeriodEnd().toString(),
                payout.getPaidAt() !=null ? payout.getPaidAt().toString() : null,
                payout.getStatus().name()
        );
    }

    @Override
    public Page<DriverPayoutDTO> getPayoutHistory(Long driverId, Pageable pageable) {
        return payoutRepo.findByDriverId(driverId, pageable)
                .map(p -> new DriverPayoutDTO(
                        p.getId(),
                        p.getAmountPaid(),
                        p.getPeriodStart().toString(),
                        p.getPeriodEnd().toString(),
                        p.getPaidAt() != null ? p.getPaidAt().toString() : null,
                        p.getStatus().name()
                ));
    }

    @Override
    public Page<DriverPayoutDTO> getPendingPayouts(Pageable pageable) {
        return payoutRepo.findByStatus(PayoutStatus.PENDING,pageable)
                .map(p -> new DriverPayoutDTO(
                        p.getId(),
                        p.getAmountPaid(),
                        p.getPeriodStart().toString(),
                        p.getPeriodEnd().toString(),
                        null,
                        p.getStatus().name()
                ));
    }

    @Override
    public void processPayout(Long payoutId, String transactionRef, String processedBy) {
        var payout = payoutRepo.findById(payoutId)
                .orElseThrow(() -> new NotFoundException("Payout not found with ID: " + payoutId));

        if (payout.getStatus() != PayoutStatus.PAID) {
            log.warn("Attempt to re-process paid payout: {}", payoutId);
            return;
        }
        walletService.deposit(payout.getDriverId(),
                payout.getAmountPaid(), "Manual payout processing. Ref: " + transactionRef);

        // 2. Update payout record
        payout.setPaidAt(LocalDateTime.now());
        payout.setTransactionRef(transactionRef);
        payout.setProcessedBy(processedBy);
        payout.setStatus(PayoutStatus.PAID);
        payoutRepo.save(payout);
        log.info("Processed payout {} for driver {}", payoutId, payout.getDriverId());
    }
}