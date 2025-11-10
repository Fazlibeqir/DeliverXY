package com.deliverXY.backend.controllers;

import com.deliverXY.backend.enums.PaymentMethod;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.DriverEarnings;
import com.deliverXY.backend.models.Payout;
import com.deliverXY.backend.service.AppUserService;
import com.deliverXY.backend.service.impl.EarningsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/earnings")
@RequiredArgsConstructor
@Slf4j
public class EarningsController {

    private final EarningsService earningsService;
    private final AppUserService appUserService;

    /**
     * Get driver earnings summary
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<EarningsService.EarningsSummary> getEarningsSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            AppUser driver = appUserService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            if (startDate == null) startDate = LocalDate.now().minusMonths(1);
            if (endDate == null) endDate = LocalDate.now();

            EarningsService.EarningsSummary summary =
                    earningsService.getDriverEarningsSummary(driver, startDate, endDate);

            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            log.error("Error fetching earnings summary: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get driver earnings with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Page<DriverEarnings>> getEarnings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            AppUser driver = appUserService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            Pageable pageable = PageRequest.of(page, size);
            Page<DriverEarnings> earnings = earningsService.getDriverEarnings(driver, pageable);

            return ResponseEntity.ok(earnings);

        } catch (Exception e) {
            log.error("Error fetching earnings: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Request payout
     */

// Update the requestPayout method to use String for paymentMethod

    @PostMapping("/request-payout")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<?> requestPayout(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) {

        try {
            AppUser driver = appUserService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            LocalDate periodStart = LocalDate.parse((String) request.get("periodStart"));
            LocalDate periodEnd = LocalDate.parse((String) request.get("periodEnd"));

            // Parse payment method from string
            String paymentMethodStr = (String) request.get("paymentMethod");
            PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentMethodStr.toUpperCase());

            String bankAccount = (String) request.get("bankAccount");

            Payout payout = earningsService.createPayout(driver, periodStart, periodEnd, paymentMethod, bankAccount);

            return ResponseEntity.ok(payout);

        } catch (IllegalArgumentException e) {
            log.error("Invalid payment method: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid payment method");
        } catch (Exception e) {
            log.error("Error requesting payout: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get driver payout history
     */
    @GetMapping("/payouts")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Page<Payout>> getPayoutHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            AppUser driver = appUserService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            Pageable pageable = PageRequest.of(page, size);
            Page<Payout> payouts = earningsService.getDriverPayoutHistory(driver, pageable);

            return ResponseEntity.ok(payouts);

        } catch (Exception e) {
            log.error("Error fetching payout history: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get pending payouts (Admin only)
     */
    @GetMapping("/pending-payouts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Payout>> getPendingPayouts() {
        try {
            List<Payout> payouts = earningsService.getPendingPayouts();
            return ResponseEntity.ok(payouts);
        } catch (Exception e) {
            log.error("Error fetching pending payouts: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Process payout (Admin only)
     */
    @PostMapping("/process-payout/{payoutId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> processPayout(
            @PathVariable Long payoutId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {

        try {
            String transactionReference = request.get("transactionReference");

            earningsService.processPayout(payoutId, transactionReference, userDetails.getUsername());

            return ResponseEntity.ok("Payout processed successfully");

        } catch (Exception e) {
            log.error("Error processing payout: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}