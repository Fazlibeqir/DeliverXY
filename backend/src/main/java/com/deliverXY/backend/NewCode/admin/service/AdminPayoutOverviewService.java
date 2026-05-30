package com.deliverXY.backend.NewCode.admin.service;

import com.deliverXY.backend.NewCode.admin.dto.AdminPayoutDTO;
import com.deliverXY.backend.NewCode.earnings.domain.DriverPayout;
import com.deliverXY.backend.NewCode.earnings.service.EarningsService;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminPayoutOverviewService {

    private final EarningsService earningsService;
    private final AppUserService userService;

    @Transactional(readOnly = true)
    public Page<AdminPayoutDTO> listAllPayouts(Pageable pageable) {
        Page<DriverPayout> payouts = earningsService.findAllPayouts(Objects.requireNonNull(pageable, "pageable"));

        Map<Long, AppUser> driverMap = new HashMap<>();
        payouts.getContent().forEach(p -> {
            if (p.getDriverId() != null && !driverMap.containsKey(p.getDriverId())) {
                userService.findById(Objects.requireNonNull(p.getDriverId(), "driverId")).ifPresent(user -> driverMap.put(p.getDriverId(), user));
            }
        });

        return payouts.map(p -> toDto(p, driverMap.get(p.getDriverId())));
    }

    private AdminPayoutDTO toDto(DriverPayout payout, AppUser driver) {
        String driverName = driver != null
                ? (driver.getFirstName() != null ? driver.getFirstName() : "") + " "
                + (driver.getLastName() != null ? driver.getLastName() : "").trim()
                : "Unknown User";
        if (driverName.trim().isEmpty()) {
            driverName = driver != null ? driver.getEmail() : "Unknown User";
        }

        return new AdminPayoutDTO(
                payout.getId(),
                payout.getDriverId(),
                driverName,
                driver != null ? driver.getEmail() : null,
                payout.getAmountPaid(),
                payout.getPeriodStart(),
                payout.getPeriodEnd(),
                payout.getStatus() != null ? payout.getStatus().name() : "PENDING",
                payout.getPaidAt(),
                payout.getTransactionRef(),
                payout.getProcessedBy()
        );
    }
}
