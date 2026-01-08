package com.deliverXY.backend.NewCode.payments.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.payments.dto.PaymentInitRequest;
import com.deliverXY.backend.NewCode.payments.dto.PaymentResultDTO;
import com.deliverXY.backend.NewCode.payments.service.PaymentService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/init")
    public ApiResponse<PaymentResultDTO> initPayment(
            @RequestBody PaymentInitRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PaymentResultDTO result = paymentService.initializePayment(
                request.getDeliveryId(),
                request.getAmount(),
                request.getProvider(),
                principal.getUser().getId()
        );

        return ApiResponse.ok(result);
    }

    @PostMapping("/confirm/{reference}")
    public ApiResponse<PaymentResultDTO> confirmPayment(@PathVariable String reference) {
        return ApiResponse.ok(paymentService.confirmPayment(reference));
    }

    @PostMapping("/refund/{paymentId}")
    public ApiResponse<String> refund(
            @PathVariable Long paymentId,
            @RequestParam BigDecimal amount,
            @RequestParam String reason
    ) {
        paymentService.refund(paymentId, amount, reason);
        return ApiResponse.ok("Refund completed");
    }

    @GetMapping("/my")
    public ApiResponse<?> getMyPayments(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(paymentService.getPaymentsByUser(principal.getUser().getId()));
    }
}
