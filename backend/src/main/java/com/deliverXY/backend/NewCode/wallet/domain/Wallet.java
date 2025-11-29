package com.deliverXY.backend.NewCode.wallet.domain;

import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@NoArgsConstructor
@Data
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"vehicles", "hibernateLazyInitializer", "handler"})
    private AppUser user;
    
    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "currency", nullable = false)
    private String currency = "Denar mkd";
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "daily_limit")
    private BigDecimal dailyLimit = new BigDecimal("1000.00"); // Default $1000 daily limit
    
    @Column(name = "monthly_limit")
    private BigDecimal monthlyLimit = new BigDecimal("10000.00"); // Default $10000 monthly limit
    
    @Column(name = "daily_spent")
    private BigDecimal dailySpent = BigDecimal.ZERO;
    
    @Column(name = "monthly_spent")
    private BigDecimal monthlySpent = BigDecimal.ZERO;
    
    @Column(name = "last_reset_date")
    private LocalDateTime lastResetDate = LocalDateTime.now();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public Wallet(AppUser user) {
        this.user = user;
    }
    
    public boolean canWithdraw(BigDecimal amount) {
        return balance.compareTo(amount) >= 0 && 
               dailySpent.add(amount).compareTo(dailyLimit) <= 0 &&
               monthlySpent.add(amount).compareTo(monthlyLimit) <= 0;
    }
    
    public void addFunds(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
    
    public boolean deductFunds(BigDecimal amount) {
        if (canWithdraw(amount)) {
            this.balance = this.balance.subtract(amount);
            this.dailySpent = this.dailySpent.add(amount);
            this.monthlySpent = this.monthlySpent.add(amount);
            return true;
        }
        return false;
    }
    
    public void resetDailyLimits() {
        this.dailySpent = BigDecimal.ZERO;
        this.lastResetDate = LocalDateTime.now();
    }
    
    public void resetMonthlyLimits() {
        this.monthlySpent = BigDecimal.ZERO;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 