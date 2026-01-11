package com.deliverXY.backend.NewCode.earnings.service;

import com.deliverXY.backend.NewCode.earnings.dto.DriverEarningsDTO;
import com.deliverXY.backend.NewCode.earnings.dto.DriverPayoutDTO;
import com.deliverXY.backend.NewCode.earnings.dto.EarningsSummaryDTO;
import com.deliverXY.backend.NewCode.earnings.dto.PayoutRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface EarningsService {
    EarningsSummaryDTO getDriverSummary(Long driverId, LocalDate start, LocalDate end);

    Page<DriverEarningsDTO> getDriverEarnings(Long driverId, Pageable pageable);

    DriverPayoutDTO requestManualPayout(Long driverId, PayoutRequestDTO request);

    Page<DriverPayoutDTO> getPayoutHistory(Long driverId, Pageable pageable);

    Page<DriverPayoutDTO> getPendingPayouts(Pageable pageable);

    void processPayout(Long payoutId, String transactionRef, String processedBy);
}