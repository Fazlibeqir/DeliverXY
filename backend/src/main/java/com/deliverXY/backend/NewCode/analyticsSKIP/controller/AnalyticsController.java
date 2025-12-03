package com.deliverXY.backend.NewCode.analyticsSKIP.controller;

import com.deliverXY.backend.NewCode.analyticsSKIP.dto.*;
import com.deliverXY.backend.NewCode.analyticsSKIP.service.AnalyticsService;
import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AppUserService userService;

    @GetMapping("/my-trips")
    public ApiResponse<Page<TripDTO>> getMyTrips(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

      AppUser user = userService.findById(principal.getUser().getId())
              .orElseThrow(() -> new NotFoundException("User not found"));
        var trips = analyticsService.getUserTripHistory(user.getId(), PageRequest.of(page, size));

        return ApiResponse.ok(trips);
    }

    @GetMapping("/my-stats")
    public ApiResponse<UserStatsDTO> getMyStats(
            @AuthenticationPrincipal UserPrincipal principal) {

        AppUser user = userService.findById(principal.getUser().getId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        var stats = analyticsService.getUserStatistics(user.getId());

        return ApiResponse.ok(stats);
    }

    @GetMapping("/driver/stats")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<DriverStatsDTO> getDriverStats(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();

        var stats = analyticsService.getDriverStatistics(principal.getUser().getId(), startDate, endDate);

        return ApiResponse.ok(stats);
    }

    @GetMapping("/driver/deliveries")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<Page<TripDTO>> getDriverDeliveries(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        var trips = analyticsService.getDriverDeliveryHistory(
                principal.getUser().getId(),
                PageRequest.of(page, size));
        return ApiResponse.ok(trips);
    }

    @GetMapping("/platform/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PlatformStatsDTO> getPlatformStatistics(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {

        if (start == null) start = LocalDate.now().minusMonths(1);
        if (end == null) end = LocalDate.now();

        return ApiResponse.ok(analyticsService.getPlatformStatistics(start, end));
    }

    @PostMapping("/generate-daily-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> generateDailyStats(
            @RequestParam(required = false) LocalDate date) {

        if (date == null) date = LocalDate.now().minusDays(1);

        analyticsService.generateDailyStats(date);
        return ApiResponse.ok("Daily stats generated for " + date);
    }
}
