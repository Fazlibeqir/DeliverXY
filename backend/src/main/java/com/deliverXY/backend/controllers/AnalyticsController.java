package com.deliverXY.backend.controllers;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.service.AnalyticsService;
import com.deliverXY.backend.service.impl.AnalyticsServiceImpl;
import com.deliverXY.backend.service.AppUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AppUserService appUserService;

    /**
     * Get user trip history
     */
    @GetMapping("/my-trips")
    public ResponseEntity<Page<Delivery>> getMyTrips(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            AppUser user = appUserService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Pageable pageable = PageRequest.of(page, size);
            Page<Delivery> trips = analyticsService.getUserTripHistory(user.getId(), pageable);

            return ResponseEntity.ok(trips);
        } catch (Exception e) {
            log.error("Error fetching trip history: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get user statistics
     */
    @GetMapping("/my-stats")
    public ResponseEntity<AnalyticsServiceImpl.UserStatistics> getMyStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            AppUser user = appUserService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            AnalyticsServiceImpl.UserStatistics stats = analyticsService.getUserStatistics(user.getId());
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching user statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get driver statistics
     */
    @GetMapping("/driver/stats")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<AnalyticsServiceImpl.DriverStatistics> getDriverStatistics(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            AppUser driver = appUserService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            if (startDate == null) startDate = LocalDate.now().minusMonths(1);
            if (endDate == null) endDate = LocalDate.now();

            AnalyticsServiceImpl.DriverStatistics stats =
                    analyticsService.getDriverStatistics(driver.getId(), startDate, endDate);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching driver statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get driver delivery history
     */
    @GetMapping("/driver/deliveries")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Page<Delivery>> getDriverDeliveries(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            AppUser driver = appUserService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            Pageable pageable = PageRequest.of(page, size);
            Page<Delivery> deliveries = analyticsService.getDriverDeliveryHistory(driver.getId(), pageable);

            return ResponseEntity.ok(deliveries);

        } catch (Exception e) {
            log.error("Error fetching driver deliveries: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get platform statistics (Admin only)
     */
    @GetMapping("/platform/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsServiceImpl.PlatformStatistics> getPlatformStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            if (startDate == null) startDate = LocalDate.now().minusMonths(1);
            if (endDate == null) endDate = LocalDate.now();

            AnalyticsServiceImpl.PlatformStatistics stats =
                    analyticsService.getPlatformStatistics(startDate, endDate);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching platform statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Trigger daily stats generation (Admin only)
     */
    @PostMapping("/generate-daily-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateDailyStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            if (date == null) date = LocalDate.now().minusDays(1);

            analyticsService.generateDailyStats(date);
            return ResponseEntity.ok("Daily stats generated for " + date);

        } catch (Exception e) {
            log.error("Error generating daily stats: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}