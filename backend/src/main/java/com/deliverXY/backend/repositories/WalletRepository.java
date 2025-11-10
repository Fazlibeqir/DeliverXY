package com.deliverXY.backend.repositories;

import com.deliverXY.backend.models.Wallet;
import com.deliverXY.backend.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    Optional<Wallet> findByUser(AppUser user);
    Optional<Wallet> findByUserId(Long userId);
} 