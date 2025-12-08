package com.deliverXY.backend.NewCode.wallet.service;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.wallet.domain.TopUpRequest;
import com.deliverXY.backend.NewCode.wallet.domain.Wallet;
import com.deliverXY.backend.NewCode.wallet.dto.WalletTransactionDTO;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    Wallet getWallet(Long userId);
    void createWalletForUser(AppUser user);
    TopUpRequest initiateTopUp(Long userId, BigDecimal amount, PaymentProvider provider);
    void finalizeTopUp(Long topUpId, boolean success, String referenceId);
    void deposit(Long userId, BigDecimal amount, String reference);

    boolean withdraw(Long userId, BigDecimal amount, String reference);

    void addTransaction(Long userId, BigDecimal amount, String type, String reference);

    List<WalletTransactionDTO> getTransactions(Long userId);
}
