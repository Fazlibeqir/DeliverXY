package com.deliverXY.backend.NewCode.earnings.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DriverPayoutService {

    private final DriverPayoutJobService payoutJobService;

    @Scheduled(cron = "0 0 3 * * MON")
    public void generateWeeklyPayout() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastWeek = now.minusDays(7);
        payoutJobService.processWeeklyPayouts(lastWeek, now);
    }
}
