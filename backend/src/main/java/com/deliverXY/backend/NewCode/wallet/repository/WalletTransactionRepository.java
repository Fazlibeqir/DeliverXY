package com.deliverXY.backend.NewCode.wallet.repository;

import com.deliverXY.backend.NewCode.wallet.domain.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
    List<WalletTransaction> findByWalletUserIdOrderByCreatedAtDesc(Long userId);
}