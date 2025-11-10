package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.enums.KYCStatus;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.dto.AppUserDTO;
import com.deliverXY.backend.models.dto.KYCDTO;
import com.deliverXY.backend.service.KYCService;
import com.deliverXY.backend.service.AppUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KYCServiceImpl implements KYCService {
    
    private final AppUserService appUserService;
    
    public KYCServiceImpl(AppUserService appUserService) {
        this.appUserService = appUserService;
    }
    
    @Override
    @Transactional
    public void submitKYC(KYCDTO kycDTO) {
        AppUser user = appUserService.findById(kycDTO.getUserId()).orElseThrow();

        // Update user with KYC information
        user.setIdFrontUrl(kycDTO.getIdFrontUrl());
        user.setIdBackUrl(kycDTO.getIdBackUrl());
        user.setSelfieUrl(kycDTO.getSelfieUrl());
        user.setProofOfAddressUrl(kycDTO.getProofOfAddressUrl());
        user.setKycStatus(KYCStatus.SUBMITTED);
        user.setKycSubmittedAt(LocalDateTime.now());
        
        appUserService.update(user.getId(), convertToDTO(user));
    }
    
    @Override
    public List<KYCDTO> getPendingKYC() {
        //TODO
        // This would typically query the database for users with SUBMITTED KYC status
        // For now, return empty list - implement when repository methods are added
        return List.of();
    }
    
    @Override
    @Transactional
    public void approveKYC(Long userId) {
        AppUser user = appUserService.findById(userId).orElseThrow();

        user.setKycStatus(KYCStatus.VERIFIED);
        user.setKycVerifiedAt(LocalDateTime.now());
        user.setIsVerified(true);
        
        appUserService.update(user.getId(), convertToDTO(user));
    }
    
    @Override
    @Transactional
    public void rejectKYC(Long userId, String reason) {
        AppUser user = appUserService.findById(userId).orElseThrow();

        user.setKycStatus(KYCStatus.REJECTED);
        user.setKycRejectionReason(reason);
        
        appUserService.update(user.getId(), convertToDTO(user));
    }
    
    @Override
    public String getKYCStatus(Long userId) {
        AppUser user = appUserService.findById(userId).orElseThrow();

        return user.getKycStatus().name();
    }
    
    private AppUserDTO convertToDTO(AppUser user) {
        // Create a minimal DTO for update purposes
        var dto = new AppUserDTO();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setKycStatus(user.getKycStatus());
        dto.setIdFrontUrl(user.getIdFrontUrl());
        dto.setIdBackUrl(user.getIdBackUrl());
        dto.setSelfieUrl(user.getSelfieUrl());
        dto.setProofOfAddressUrl(user.getProofOfAddressUrl());
        dto.setIsVerified(user.getIsVerified());
        return dto;
    }
} 