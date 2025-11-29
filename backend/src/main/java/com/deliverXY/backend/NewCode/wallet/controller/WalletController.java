package com.deliverXY.backend.NewCode.wallet.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.wallet.dto.WalletTransactionDTO;
import com.deliverXY.backend.NewCode.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wallet")
public class WalletController {
    private final WalletService walletService;

    @GetMapping
    public ApiResponse<?> getWallet(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(walletService.getWallet(principal.getUser().getId()));
    }
    @PostMapping("/topup/initiate")
    public ApiResponse<?> initiateTopUp(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody WalletTransactionDTO dto

    ){
        return ApiResponse.ok(walletService.initiateTopUp(
                principal.getUser().getId(),
                dto.getAmount()
        ));
    }
    @PostMapping("/topup/callback")
    public ApiResponse<?> finishTopUp(
            @RequestParam Long topUpId,
            @RequestParam boolean success,
            @RequestParam(required = false) String referenceId
    ) {
        walletService.finalizeTopUp(topUpId, success, referenceId);
        return ApiResponse.ok("Top-up finalized");
    }


    @PostMapping("/deposit")
    public ApiResponse<String> deposit(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody WalletTransactionDTO dto
    ) {
        walletService.deposit(
                principal.getUser().getId(),
                dto.getAmount(),
                dto.getReference()
        );
        return ApiResponse.ok("Deposit successful");
    }

    @PostMapping("/withdraw")
    public ApiResponse<?> withdraw(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody WalletTransactionDTO dto
    ) {
        boolean success = walletService.withdraw(
                principal.getUser().getId(),
                dto.getAmount(),
                dto.getReference()
        );

        if (!success) {
            return new ApiResponse<>(
                    false,
                    "Insufficient funds or daily/monthly limit reached",
                    System.currentTimeMillis(),
                    400,
                    "WALLET_WITHDRAW_DENIED",
                    "/api/wallet/withdraw"
            );
        }

        return ApiResponse.ok("Withdraw successful");
    }

    @GetMapping("/transactions")
    public ApiResponse<?> getTransactions(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(walletService.getTransactions(principal.getUser().getId()));
    }
}
