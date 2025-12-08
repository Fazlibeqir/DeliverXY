package com.deliverXY.backend.NewCode.wallet.service.impl;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.common.enums.TopUpStatus;
import com.deliverXY.backend.NewCode.common.enums.TransactionType;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.repository.AppUserRepository;
import com.deliverXY.backend.NewCode.wallet.domain.TopUpRequest;
import com.deliverXY.backend.NewCode.wallet.domain.Wallet;
import com.deliverXY.backend.NewCode.wallet.domain.WalletTransaction;
import com.deliverXY.backend.NewCode.wallet.dto.WalletDTO;
import com.deliverXY.backend.NewCode.wallet.dto.WalletTransactionDTO;
import com.deliverXY.backend.NewCode.wallet.repository.TopUpRepository;
import com.deliverXY.backend.NewCode.wallet.repository.WalletRepository;
import com.deliverXY.backend.NewCode.wallet.repository.WalletTransactionRepository;
import com.deliverXY.backend.NewCode.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final AppUserRepository userRepo;
    private final TopUpRepository topUpRepository;

    private Wallet getWalletEntity(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(()-> new NotFoundException("Wallet not found for user:" + userId));
    }
    //Check and reset limits if needed
    private void checkAndResetLimits(Wallet wallet) {
        LocalDate now = LocalDate.now();
        LocalDateTime lastResetDateTime = wallet.getLastResetDate();
        LocalDate lastResetDate = lastResetDateTime.toLocalDate();

        // 1. Daily Reset: If the last reset was on a different day
        if (!now.isEqual(lastResetDate)) {
            wallet.setDailySpent(BigDecimal.ZERO);
            // We set the date here, but will refine the monthly logic below
        }

        // 2. Monthly Reset: If the last reset was in a different month
        if (now.getMonth() != lastResetDate.getMonth() || now.getYear() != lastResetDate.getYear()) {
            wallet.setMonthlySpent(BigDecimal.ZERO);
        }

        // Update the last reset date only if any reset occurred
        if (!now.isEqual(lastResetDate) || now.getMonth() != lastResetDate.getMonth()) {
            wallet.setLastResetDate(LocalDateTime.now());
        }
        // Note: The Wallet entity will be saved automatically by the @Transactional context.
    }
    @Override
    public Wallet getWallet(Long userId) {
        AppUser user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        return walletRepository.findByUser(user)
                .orElseGet(() -> walletRepository.save(new Wallet(user)));
    }

    @Override
    public void createWalletForUser(AppUser user) {
        walletRepository.findByUser(user)
                .orElseGet(() -> walletRepository.save(new Wallet(user)));

    }

    @Override
    public TopUpRequest initiateTopUp(Long userId, BigDecimal amount, PaymentProvider provider) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new NotFoundException("Top-up amount must be positive");

        TopUpRequest req = new TopUpRequest();
        req.setUserId(userId);
        req.setAmount(amount);

        // generate unique reference for bank/cpay/stripe/etc.
        req.setReferenceId(UUID.randomUUID().toString());
        req.setProvider(provider !=null ? provider.name() : "MOCK"); // Placeholder until CPay integration
        req.setStatus(TopUpStatus.PENDING);

        TopUpRequest savedReq = topUpRepository.save(req);
        if (provider == PaymentProvider.MOCK){
//            Thread.sleep(500);
            finalizeTopUp(savedReq.getId(), true, savedReq.getReferenceId());
        }
        return savedReq;
    }

    @Override
    public void finalizeTopUp(Long topUpId, boolean success, String referenceId) {
        TopUpRequest req = topUpRepository.findById(topUpId)
                .orElseThrow(()-> new NotFoundException("Top up request not found:" + topUpId));

        req.setReferenceId(referenceId);
        if (success){
            req.setStatus(TopUpStatus.SUCCESS);
            deposit(req.getUserId(), req.getAmount(), referenceId);
        } else {
            req.setStatus(TopUpStatus.FAILED);
        }
        topUpRepository.save(req);
    }

    @Override
    @Transactional
    public void deposit(Long userId, BigDecimal amount, String reference) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new NotFoundException("Deposit amount must be positive");

        Wallet wallet = getWallet(userId);
        wallet.addFunds(amount);
        walletRepository.save(wallet);

        saveTransaction(wallet, amount, TransactionType.DEPOSIT, reference);

    }

    @Override
    @Transactional
    public boolean withdraw(Long userId, BigDecimal amount, String reference) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Withdraw amount must be positive");

        Wallet wallet = getWalletEntity(userId);

        checkAndResetLimits(wallet);

        // not enough funds
        if (!wallet.canWithdraw(amount)) {
            throw new NotFoundException("Insufficient funds");
        }

        wallet.deductFunds(amount);
        walletRepository.save(wallet);

        saveTransaction(wallet, amount.negate(), TransactionType.WITHDRAW, reference);
        return true;
    }
    private void saveTransaction(Wallet wallet, BigDecimal amount, TransactionType type, String reference) {
        WalletTransaction tx = WalletTransaction.builder()
                .wallet(wallet)
                .type(type)
                .amount(amount)
                .reference(reference)
                .build();

        transactionRepository.save(tx);
    }

    @Override
    @Transactional
    public void addTransaction(Long userId, BigDecimal amount, String type, String reference) {
        // Convert old string type → enum
        TransactionType txType = TransactionType.valueOf(type.toUpperCase());

        Wallet wallet = getWallet(userId);
        saveTransaction(wallet, amount, txType, reference);
    }

    @Override
    public List<WalletTransactionDTO> getTransactions(Long userId) {
        return transactionRepository
                .findByWalletUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(tx ->{
                    WalletTransactionDTO dto = new WalletTransactionDTO();
                    dto.setId(tx.getId());
                    dto.setAmount(tx.getAmount());
                    dto.setType(tx.getType());
                    dto.setReference(tx.getReference());
                    dto.setCreatedAt(tx.getCreatedAt());
                    return dto;
                })
                .toList();
    }
}
