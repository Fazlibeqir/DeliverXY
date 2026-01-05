package com.deliverXY.backend.NewCode.wallet.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.wallet.dto.TopUpInitDTO;
import com.deliverXY.backend.NewCode.wallet.dto.TopUpInitResponseDTO;
import com.deliverXY.backend.NewCode.wallet.dto.WalletTransactionDTO;
import com.deliverXY.backend.NewCode.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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
    public ApiResponse<TopUpInitResponseDTO> initiateTopUp(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TopUpInitDTO dto

    ){
        BigDecimal amount = dto.getAmount();
        if (amount == null) {
            return ApiResponse.error(
                    "Amount is required for top-up.",
                    400, "AMOUNT_REQUIRED",
                    "/api/wallet/topup/initiate"
            );
        }
        return ApiResponse.ok(
                walletService.initiateTopUp(
                principal.getUser().getId(),
                amount,
                dto.getProvider()
        ));
    }
    @PostMapping("/create-initial/{userId}")
    @PreAuthorize( "hasRole('ADMIN')")
    public ApiResponse<String> createInitialWalletForUser(@PathVariable Long userId) {
        // Calling a zero-amount deposit is a clean way to ensure the wallet is created
        // if it doesn't exist, without performing a real monetary transaction.
        // If your underlying service requires a dedicated 'create' method, use that instead.
        walletService.deposit(
                userId,
                BigDecimal.ZERO, // Amount zero to only create the wallet structure
                "Initial Wallet Setup"
        );
        return ApiResponse.ok("Initial wallet created for user: " + userId);
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
            @Valid @RequestBody WalletTransactionDTO dto
    ) {
        BigDecimal amount = dto.getAmount();
        if (amount == null) {
            return ApiResponse.error("Amount is required for deposit.", 400, "AMOUNT_REQUIRED", "/api/wallet/deposit");
        }
        walletService.deposit(
                principal.getUser().getId(),
                amount,
                dto.getReference()
        );
        return ApiResponse.ok("Deposit successful");
    }

    @PostMapping("/withdraw")
    public ApiResponse<?> withdraw(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody WalletTransactionDTO dto
    ) {
        BigDecimal amount = dto.getAmount();
        if (amount == null) {
            return ApiResponse.error("Amount is required for withdrawal.", 400, "AMOUNT_REQUIRED", "/api/wallet/withdraw");
        }
        try {
            walletService.withdraw(
                    principal.getUser().getId(),
                    amount,
                    dto.getReference()
            );
        }catch (NotFoundException e){
            return ApiResponse.error(
                    e.getMessage(),
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
