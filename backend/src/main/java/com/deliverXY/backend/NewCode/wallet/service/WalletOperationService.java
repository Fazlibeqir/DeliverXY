package com.deliverXY.backend.NewCode.wallet.service;

import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WalletOperationService {

    private final WalletService walletService;

    public void withdraw(Long userId, BigDecimal amount, String reference) {
        Long id = Objects.requireNonNull(userId, "userId");
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required for withdrawal.");
        }
        try {
            walletService.withdraw(id, amount, reference);
        } catch (NotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
    }
}
