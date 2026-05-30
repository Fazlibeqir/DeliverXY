package com.deliverXY.backend.NewCode.wallet.service.impl;

import com.deliverXY.backend.NewCode.common.enums.PaymentProvider;
import com.deliverXY.backend.NewCode.common.enums.TopUpStatus;
import com.deliverXY.backend.NewCode.common.enums.TransactionType;
import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.repository.AppUserRepository;
import com.deliverXY.backend.NewCode.wallet.domain.TopUpRequest;
import com.deliverXY.backend.NewCode.wallet.domain.Wallet;
import com.deliverXY.backend.NewCode.wallet.domain.WalletTransaction;
import com.deliverXY.backend.NewCode.wallet.dto.TopUpInitResponseDTO;
import com.deliverXY.backend.NewCode.wallet.dto.WalletTransactionDTO;
import com.deliverXY.backend.NewCode.wallet.repository.TopUpRepository;
import com.deliverXY.backend.NewCode.wallet.repository.WalletRepository;
import com.deliverXY.backend.NewCode.wallet.repository.WalletTransactionRepository;
import com.deliverXY.backend.NewCode.wallet.service.WalletService;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final AppUserRepository userRepo;
    private final TopUpRepository topUpRepository;
    private final WalletService self;

    public WalletServiceImpl(
            WalletRepository walletRepository,
            WalletTransactionRepository transactionRepository,
            AppUserRepository userRepo,
            TopUpRepository topUpRepository,
            @Lazy WalletService self) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.userRepo = userRepo;
        this.topUpRepository = topUpRepository;
        this.self = self;
    }

    private Wallet getWalletEntity(Long userId) {
        return self.getWallet(userId);
    }

    private void checkAndResetLimits(Wallet wallet) {
        LocalDate now = LocalDate.now();
        LocalDateTime lastResetDateTime = wallet.getLastResetDate();
        LocalDate lastResetDate = lastResetDateTime.toLocalDate();

        if (!now.isEqual(lastResetDate)) {
            wallet.setDailySpent(BigDecimal.ZERO);
        }

        if (now.getMonth() != lastResetDate.getMonth() || now.getYear() != lastResetDate.getYear()) {
            wallet.setMonthlySpent(BigDecimal.ZERO);
        }

        if (!now.isEqual(lastResetDate) || now.getMonth() != lastResetDate.getMonth()) {
            wallet.setLastResetDate(LocalDateTime.now());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Wallet getWallet(Long userId) {
        Long id = Objects.requireNonNull(userId, "userId");
        AppUser user = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        return walletRepository.findByUser(user)
                .orElseGet(() -> walletRepository.save(new Wallet(user)));
    }

    @Override
    @Transactional
    public void createWalletForUser(AppUser user) {
        walletRepository.findByUser(user)
                .orElseGet(() -> walletRepository.save(new Wallet(user)));
    }

    @Override
    public TopUpInitResponseDTO initiateTopUp(Long userId, BigDecimal amount, PaymentProvider provider) {
        Long id = Objects.requireNonNull(userId, "userId");

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NotFoundException("Top-up amount must be positive");
        }

        TopUpRequest req = new TopUpRequest();
        req.setUserId(id);
        req.setAmount(amount);
        req.setStatus(TopUpStatus.PENDING);
        req.setProvider(provider != null ? provider.name() : PaymentProvider.MOCK.name());

        topUpRepository.save(req);

        req.setReferenceId(UUID.randomUUID().toString());
        topUpRepository.save(req);

        if (provider == PaymentProvider.MOCK) {
            Long topUpId = Objects.requireNonNull(req.getId(), "topUpId");
            self.finalizeTopUp(topUpId, true, "MOCK-" + topUpId);
            return new TopUpInitResponseDTO(
                    req.getId(),
                    amount,
                    null,
                    PaymentProvider.MOCK.name()
            );
        }

        if (provider == PaymentProvider.STRIPE) {
            try {
                PaymentIntent intent = PaymentIntent.create(
                        PaymentIntentCreateParams.builder()
                                .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                                .setCurrency("eur")
                                .putMetadata("topup_id", req.getId().toString())
                                .putMetadata("user_id", id.toString())
                                .build()
                );

                req.setReferenceId(intent.getId());
                topUpRepository.save(req);

                return new TopUpInitResponseDTO(
                        req.getId(),
                        amount,
                        intent.getClientSecret(),
                        PaymentProvider.STRIPE.name()
                );
            } catch (Exception e) {
                throw new RuntimeException("Stripe PaymentIntent creation failed", e);
            }
        }

        throw new IllegalArgumentException("Unsupported payment provider: " + provider);
    }

    @Override
    @Transactional
    public void finalizeTopUp(Long topUpId, boolean success, String referenceId) {
        Long id = Objects.requireNonNull(topUpId, "topUpId");
        TopUpRequest req = topUpRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Top up request not found:" + id));

        req.setReferenceId(referenceId);
        if (success) {
            req.setStatus(TopUpStatus.SUCCESS);
            self.deposit(req.getUserId(), req.getAmount(), referenceId);
        } else {
            req.setStatus(TopUpStatus.FAILED);
        }
        topUpRepository.save(req);
    }

    @Override
    @Transactional
    public void deposit(Long userId, BigDecimal amount, String reference) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NotFoundException("Deposit amount must be positive");
        }

        Wallet wallet = self.getWallet(userId);
        wallet.addFunds(amount);
        walletRepository.save(wallet);

        saveTransaction(wallet, amount, TransactionType.DEPOSIT, reference);
    }

    @Override
    @Transactional
    public void ensureSufficientBalance(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Wallet wallet = getWalletEntity(userId);

        Long walletId = Objects.requireNonNull(wallet.getId(), "Wallet not found");
        wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));

        BigDecimal balance = wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO;

        log.info("Checking wallet balance for user {}: Required={}, Available={}", userId, amount, balance);

        if (balance.compareTo(amount) < 0) {
            String errorMsg = String.format(
                    "Insufficient wallet balance. Required: %.2f MKD, Available: %.2f MKD",
                    amount, balance);
            log.warn("Balance check failed for user {}: {}", userId, errorMsg);
            throw new BadRequestException(errorMsg);
        }

        log.info("Balance check passed for user {}: Available={} >= Required={}", userId, balance, amount);
    }

    @Override
    @Transactional
    public boolean withdraw(Long userId, BigDecimal amount, String reference) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdraw amount must be positive");
        }

        Wallet wallet = getWalletEntity(userId);

        checkAndResetLimits(wallet);

        if (!wallet.canWithdraw(amount)) {
            throw new NotFoundException("Insufficient funds");
        }

        wallet.deductFunds(amount);
        walletRepository.save(wallet);

        saveTransaction(wallet, amount.negate(), TransactionType.WITHDRAW, reference);
        return true;
    }

    private void saveTransaction(Wallet wallet, BigDecimal amount, TransactionType type, String reference) {
        WalletTransaction tx = Objects.requireNonNull(WalletTransaction.builder()
                .wallet(wallet)
                .type(type)
                .amount(amount)
                .reference(reference)
                .build());

        transactionRepository.save(tx);
    }

    @Override
    @Transactional
    public void addTransaction(Long userId, BigDecimal amount, String type, String reference) {
        TransactionType txType = TransactionType.valueOf(type.toUpperCase());

        Wallet wallet = self.getWallet(userId);
        saveTransaction(wallet, amount, txType, reference);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransactionDTO> getTransactions(Long userId) {
        Long id = Objects.requireNonNull(userId, "userId");
        return transactionRepository
                .findByWalletUserIdOrderByCreatedAtDesc(id, PageRequest.of(0, 100))
                .getContent()
                .stream()
                .map(tx -> {
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
