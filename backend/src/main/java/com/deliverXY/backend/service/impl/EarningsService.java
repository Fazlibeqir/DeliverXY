package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.constants.DeliveryConstants;
import com.deliverXY.backend.enums.PaymentMethod;
import com.deliverXY.backend.enums.PayoutStatus;
import com.deliverXY.backend.exception.DeliveryException;
import com.deliverXY.backend.exception.ResourceNotFoundException;
import com.deliverXY.backend.models.*;
import com.deliverXY.backend.repositories.DriverEarningsRepository;
import com.deliverXY.backend.repositories.PayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EarningsService {

    private final DriverEarningsRepository earningsRepository;
    private final PayoutRepository payoutRepository;

    /**
     * Record earnings for a completed delivery
     */
    @Transactional
    public DriverEarnings recordEarnings(Delivery delivery) {
        validateDeliveryForEarnings(delivery);

        DriverEarnings earnings = new DriverEarnings();
        earnings.setDriver(delivery.getDriver());
        earnings.setDelivery(delivery);
        earnings.setDeliveryFare(delivery.getFinalPrice());
        earnings.setPlatformCommissionRate(DeliveryConstants.PLATFORM_COMMISSION_RATE);
        earnings.setTips(delivery.getTip() != null ? delivery.getTip() : 0.0);
        earnings.setEarnedDate(LocalDate.now());
        earnings.setBonus(calculateBonus(delivery));

        earnings = earningsRepository.save(earnings);
        delivery.setDriverEarnings(earnings.getTotalEarning());

        log.info("Recorded earnings for delivery {}: {} MKD (Driver gets: {} MKD)",
                delivery.getId(), earnings.getDeliveryFare(), earnings.getTotalEarning());

        return earnings;
    }

    /**
     * Validate delivery is eligible for earnings
     */
    private void validateDeliveryForEarnings(Delivery delivery) {
        if (delivery.getDriver() == null) {
            throw new DeliveryException("Delivery has no assigned driver");
        }

        if (!delivery.isDelivered()) {
            throw new DeliveryException("Can only record earnings for delivered orders");
        }
    }

    /**
     * Calculate bonus for delivery
     */
    private double calculateBonus(Delivery delivery) {
        double bonus = 0.0;

        // Long distance bonus
        if (delivery.getDistance() != null &&
                delivery.getDistance() > DeliveryConstants.LONG_DISTANCE_THRESHOLD_KM) {
            bonus += DeliveryConstants.LONG_DISTANCE_BONUS;
            log.debug("Long distance bonus applied: {} MKD", DeliveryConstants.LONG_DISTANCE_BONUS);
        }

        return bonus;
    }

    /**
     * Get driver earnings summary for a period
     */
    public EarningsSummary getDriverEarningsSummary(AppUser driver, LocalDate startDate, LocalDate endDate) {
        List<DriverEarnings> earnings = earningsRepository
                .findByDriverAndEarnedDateBetweenOrderByEarnedDateDesc(driver, startDate, endDate);

        double totalEarnings = earnings.stream().mapToDouble(DriverEarnings::getTotalEarning).sum();
        double totalDeliveryFares = earnings.stream().mapToDouble(DriverEarnings::getDeliveryFare).sum();
        double totalCommission = earnings.stream().mapToDouble(DriverEarnings::getPlatformCommission).sum();
        double totalTips = earnings.stream().mapToDouble(DriverEarnings::getTips).sum();
        double totalBonus = earnings.stream().mapToDouble(DriverEarnings::getBonus).sum();

        Double pendingPayouts = earningsRepository.getPendingEarnings(
                driver, PayoutStatus.PENDING);

        return new EarningsSummary(
                totalEarnings,
                totalDeliveryFares,
                totalCommission,
                totalTips,
                totalBonus,
                (long) earnings.size(),
                pendingPayouts != null ? pendingPayouts : 0.0
        );
    }

    /**
     * Get driver earnings with pagination
     */
    public Page<DriverEarnings> getDriverEarnings(AppUser driver, Pageable pageable) {
        return earningsRepository.findByDriverOrderByEarnedDateDesc(driver, pageable);
    }

    /**
     * Create payout for driver
     */
    @Transactional
    public Payout createPayout(AppUser driver, LocalDate periodStart, LocalDate periodEnd,
                               PaymentMethod paymentMethod, String bankAccount) {

        List<DriverEarnings> pendingEarnings = getPendingEarningsForPeriod(driver, periodStart, periodEnd);

        if (pendingEarnings.isEmpty()) {
            throw new DeliveryException("No pending earnings found for the period");
        }

        Payout payout = buildPayout(driver, pendingEarnings, periodStart, periodEnd, paymentMethod, bankAccount);
        payout = payoutRepository.save(payout);

        linkEarningsToPayout(pendingEarnings, payout);

        log.info("Created payout {} for driver {} - Amount: {} MKD",
                payout.getId(), driver.getId(), payout.getPayoutAmount());

        return payout;
    }

    /**
     * Get pending earnings for period
     */
    private List<DriverEarnings> getPendingEarningsForPeriod(AppUser driver, LocalDate start, LocalDate end) {
        return earningsRepository
                .findByDriverAndPayoutStatusOrderByEarnedDateDesc(driver, PayoutStatus.PENDING)
                .stream()
                .filter(e -> !e.getEarnedDate().isBefore(start) && !e.getEarnedDate().isAfter(end))
                .toList();
    }

    /**
     * Build payout object
     */
    private Payout buildPayout(AppUser driver, List<DriverEarnings> earnings,
                               LocalDate start, LocalDate end,
                               PaymentMethod method, String account) {
        double totalAmount = earnings.stream().mapToDouble(DriverEarnings::getTotalEarning).sum();
        double totalTips = earnings.stream().mapToDouble(DriverEarnings::getTips).sum();
        double totalBonus = earnings.stream().mapToDouble(DriverEarnings::getBonus).sum();

        Payout payout = new Payout();
        payout.setDriver(driver);
        payout.setPayoutAmount(totalAmount);
        payout.setPeriodStart(start);
        payout.setPeriodEnd(end);
        payout.setTotalDeliveries(earnings.size());
        payout.setTotalEarnings(totalAmount - totalTips - totalBonus);
        payout.setTotalTips(totalTips);
        payout.setTotalBonus(totalBonus);
        payout.setPaymentMethod(method);
        payout.setBankAccount(account);
        payout.setStatus(PayoutStatus.PENDING);

        return payout;
    }

    /**
     * Link earnings to payout
     */
    private void linkEarningsToPayout(List<DriverEarnings> earnings, Payout payout) {
        earnings.forEach(earning -> {
            earning.setPayout(payout);
            earning.setPayoutStatus(PayoutStatus.PENDING);
        });
        earningsRepository.saveAll(earnings);
    }

    /**
     * Process payout (Admin only)
     */
    @Transactional
    public void processPayout(Long payoutId, String transactionReference, String adminEmail) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Payout", "id", payoutId));

        payout.setStatus(PayoutStatus.COMPLETED);
        payout.setTransactionReference(transactionReference);
        payout.setProcessedBy(adminEmail);
        payout.setProcessedAt(java.time.LocalDateTime.now());

        payoutRepository.save(payout);

        updateEarningsPayoutStatus(payout);

        log.info("Processed payout {} for driver {} - Reference: {}",
                payoutId, payout.getDriver().getId(), transactionReference);
    }

    /**
     * Update earnings payout status
     */
    private void updateEarningsPayoutStatus(Payout payout) {
        List<DriverEarnings> earnings = earningsRepository
                .findByDriverAndPayoutStatusOrderByEarnedDateDesc(
                        payout.getDriver(), PayoutStatus.PENDING);

        earnings.stream()
                .filter(e -> e.getPayout() != null && e.getPayout().getId().equals(payout.getId()))
                .forEach(e -> e.setPayoutStatus(PayoutStatus.PAID));

        earningsRepository.saveAll(earnings);
    }

    /**
     * Get all pending payouts (Admin)
     */
    public List<Payout> getPendingPayouts() {
        return payoutRepository.findByStatus(PayoutStatus.PENDING);
    }

    /**
     * Get driver payout history
     */
    public Page<Payout> getDriverPayoutHistory(AppUser driver, Pageable pageable) {
        return payoutRepository.findByDriverOrderByCreatedAtDesc(driver, pageable);
    }

    // Inner class for summary
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class EarningsSummary {
        private Double totalEarnings;
        private Double totalDeliveryFares;
        private Double totalCommission;
        private Double totalTips;
        private Double totalBonus;
        private Long totalDeliveries;
        private Double pendingPayouts;
    }
}