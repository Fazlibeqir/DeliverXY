package com.deliverXY.backend.repositories;

import com.deliverXY.backend.enums.KYCStatus;
import com.deliverXY.backend.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.deliverXY.backend.models.AppUser;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    void deleteByUsername(String username);
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmail(String email);
    List<AppUser> findByKycStatus(KYCStatus kycStatus);
    Long countByRoleAndIsActiveTrue(UserRole role);
}