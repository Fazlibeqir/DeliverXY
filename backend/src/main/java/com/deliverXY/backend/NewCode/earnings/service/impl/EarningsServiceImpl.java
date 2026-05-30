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
import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class EarningsServiceImpl implements EarningsService {

    private final DriverEarningsRepository earningsRepo;
    private final DriverPayoutRepository payoutRepo;
    private final WalletService walletService;

    @Override
    @Transactional(readOnly = true)
    public EarningsSummaryDTO getDriverSummary(Long driverId, LocalDate start, LocalDate end) {
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(23, 59, 59);

        Object[] sums = earningsRepo.sumByAgentAndPeriod(driverId, startDt, endDt);
        BigDecimal totalEarned = sums[0] != null ? (BigDecimal) sums[0] : BigDecimal.ZERO;
        BigDecimal totalTips = sums[1] != null ? (BigDecimal) sums[1] : BigDecimal.ZERO;
        long count = sums[2] != null ? (Long) sums[2] : 0L;

        Double totalDistance = earningsRepo.sumDistanceByAgentAndPeriod(driverId, startDt, endDt);

        return new EarningsSummaryDTO(
                totalEarned,
                totalTips,
                count,
                totalDistance != null ? totalDistance : 0.0
        );
    }

    @Override
    @Transactional(readOnly = true)
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
        LocalDateTime startDt = request.getPeriodStart().atStartOfDay();
        LocalDateTime endDt = request.getPeriodEnd().atTime(23, 59, 59);

        Object[] sums = earningsRepo.sumByAgentAndPeriod(driverId, startDt, endDt);
        BigDecimal total = sums[0] != null ? (BigDecimal) sums[0] : BigDecimal.ZERO;

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("No earnings found for the selected period");
        }

        DriverPayout payout = Objects.requireNonNull(DriverPayout.builder()
                .driverId(driverId)
                .amountPaid(total)
                .periodStart(startDt)
                .periodEnd(endDt)
                .status(PayoutStatus.PENDING)
                .build());

        payoutRepo.save(payout);

        return new DriverPayoutDTO(
                payout.getId(),
                payout.getAmountPaid(),
                payout.getPeriodStart().toString(),
                payout.getPeriodEnd().toString(),
                payout.getPaidAt() != null ? payout.getPaidAt().toString() : null,
                payout.getStatus().name()
        );
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public Page<DriverPayoutDTO> getPendingPayouts(Pageable pageable) {
        return payoutRepo.findByStatus(PayoutStatus.PENDING, pageable)
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
    @Transactional
    public void processPayout(@NonNull Long payoutId, String transactionRef, String processedBy) {
        var payout = payoutRepo.findById(payoutId)
                .orElseThrow(() -> new NotFoundException("Payout not found with ID: " + payoutId));

        if (payout.getStatus() != PayoutStatus.PENDING) {
            log.warn("Attempt to process non-pending payout: {} (status: {})", payoutId, payout.getStatus());
            throw new BadRequestException("Payout is not in PENDING status");
        }

        walletService.deposit(
                payout.getDriverId(),
                payout.getAmountPaid(),
                "Manual payout processing. Ref: " + transactionRef
        );

        payout.setPaidAt(LocalDateTime.now());
        payout.setTransactionRef(transactionRef);
        payout.setProcessedBy(processedBy);
        payout.setStatus(PayoutStatus.PAID);
        payoutRepo.save(payout);
        log.info("Processed payout {} for driver {}", payoutId, payout.getDriverId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DriverPayout> findAllPayouts(@NonNull Pageable pageable) {
        return payoutRepo.findAll(pageable);
    }
}
