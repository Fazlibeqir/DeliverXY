package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.enums.KYCStatus;
import com.deliverXY.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.dto.AppUserDTO;
import com.deliverXY.backend.repositories.AppUserRepository;
import com.deliverXY.backend.service.AppUserService;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<AppUser> findById(Long id) {
        return appUserRepository.findById(id);
    }

    @Override
    public List<AppUser> findAll() {
        return appUserRepository.findAll();
    }

    @Override
    @Transactional
    public AppUser create(AppUserDTO appUserDTO) {
        // Check if username already exists
        if (appUserRepository.findByUsername(appUserDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + appUserDTO.getUsername());
        }
        // Check if email already exists
        if (appUserRepository.findByEmail(appUserDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + appUserDTO.getEmail());
        }
        AppUser user = new AppUser(
                appUserDTO.getUsername(),
                appUserDTO.getEmail(),
                passwordEncoder.encode(appUserDTO.getPassword()),
                appUserDTO.getPhoneNumber(),
                appUserDTO.getRole()
        );// Set additional fields
        user.setFirstName(appUserDTO.getFirstName());
        user.setLastName(appUserDTO.getLastName());
        user.setKycStatus(appUserDTO.getKycStatus() != null ? appUserDTO.getKycStatus() : KYCStatus.PENDING);
        user.setIsActive(appUserDTO.getIsActive() != null ? appUserDTO.getIsActive() : true);
        user.setIsVerified(appUserDTO.getIsVerified() != null ? appUserDTO.getIsVerified() : false);

        AppUser savedUser = appUserRepository.save(user);
        log.info("Created new user: {} with role: {}", savedUser.getUsername(), savedUser.getRole());

        return savedUser;
    }

    @Override
    @Transactional
    public AppUser update(Long id, AppUserDTO appUserDTO) {
        // Find the user - throw exception if not found
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Update basic fields
        if (appUserDTO.getUsername() != null && !appUserDTO.getUsername().equals(user.getUsername())) {
            // Check if new username is already taken
            if (appUserRepository.findByUsername(appUserDTO.getUsername()).isPresent()) {
                throw new RuntimeException("Username already exists: " + appUserDTO.getUsername());
            }
            user.setUsername(appUserDTO.getUsername());
        }

        if (appUserDTO.getEmail() != null && !appUserDTO.getEmail().equals(user.getEmail())) {
            // Check if new email is already taken
            if (appUserRepository.findByEmail(appUserDTO.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists: " + appUserDTO.getEmail());
            }
            user.setEmail(appUserDTO.getEmail());
        }

        // Update password only if provided
        if (appUserDTO.getPassword() != null && !appUserDTO.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(appUserDTO.getPassword()));
        }

        // Update other fields
        if (appUserDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(appUserDTO.getPhoneNumber());
        }

        if (appUserDTO.getFirstName() != null) {
            user.setFirstName(appUserDTO.getFirstName());
        }

        if (appUserDTO.getLastName() != null) {
            user.setLastName(appUserDTO.getLastName());
        }

        // Update KYC fields
        if (appUserDTO.getKycStatus() != null) {
            user.setKycStatus(appUserDTO.getKycStatus());
        }

        if (appUserDTO.getIdFrontUrl() != null) {
            user.setIdFrontUrl(appUserDTO.getIdFrontUrl());
        }

        if (appUserDTO.getIdBackUrl() != null) {
            user.setIdBackUrl(appUserDTO.getIdBackUrl());
        }

        if (appUserDTO.getSelfieUrl() != null) {
            user.setSelfieUrl(appUserDTO.getSelfieUrl());
        }

        if (appUserDTO.getProofOfAddressUrl() != null) {
            user.setProofOfAddressUrl(appUserDTO.getProofOfAddressUrl());
        }

        // Update agent-specific fields
        if (appUserDTO.getIsAvailable() != null) {
            user.setIsAvailable(appUserDTO.getIsAvailable());
        }

        if (appUserDTO.getCurrentLatitude() != null) {
            user.setCurrentLatitude(appUserDTO.getCurrentLatitude());
        }

        if (appUserDTO.getCurrentLongitude() != null) {
            user.setCurrentLongitude(appUserDTO.getCurrentLongitude());
        }

        // Update account status
        if (appUserDTO.getIsActive() != null) {
            user.setIsActive(appUserDTO.getIsActive());
        }

        if (appUserDTO.getIsVerified() != null) {
            user.setIsVerified(appUserDTO.getIsVerified());
        }

        AppUser updatedUser = appUserRepository.save(user);
        log.info("Updated user: {}", updatedUser.getUsername());

        return updatedUser;
    }

    @Override
    @Transactional
    public void updateUser(AppUser appUser) {
        appUserRepository.save(appUser);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!appUserRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        appUserRepository.deleteById(id);
        log.info("Deleted user with id: {}", id);
    }

    @Override
    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

   

    @Override
    public Optional<AppUser> findByUsername(String username) {
        return appUserRepository.findByUsername(username);
    }

    @Override
    public List<AppUser> findByKycStatus(KYCStatus kycStatus) {
        return appUserRepository.findByKycStatus(kycStatus);
    }
}