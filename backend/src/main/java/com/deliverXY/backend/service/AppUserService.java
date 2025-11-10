package com.deliverXY.backend.service;

import com.deliverXY.backend.enums.KYCStatus;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.dto.AppUserDTO;

import java.util.List;
import java.util.Optional;

public interface AppUserService {
    Optional<AppUser> findById(Long id);
    List<AppUser> findAll();
    AppUser create(AppUserDTO appUserDTO);
    AppUser update(Long id, AppUserDTO appUserDTO);
    void updateUser(AppUser appUser);
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByUsername(String username);
    List<AppUser> findByKycStatus(KYCStatus kycStatus);

    void deleteById(Long id);
}