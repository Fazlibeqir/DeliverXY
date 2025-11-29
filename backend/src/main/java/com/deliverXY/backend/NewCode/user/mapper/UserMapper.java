package com.deliverXY.backend.NewCode.user.mapper;

import com.deliverXY.backend.NewCode.kyc.domain.AppUserKYC;
import com.deliverXY.backend.NewCode.kyc.dto.KYCInfoDTO;
import com.deliverXY.backend.NewCode.user.domain.*;
import com.deliverXY.backend.NewCode.user.dto.*;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponseDTO toFullDTO(
            AppUser user,
            AppUserKYC kyc,
            AppUserAgentProfile profile,
            AppUserLocation location,
            AppUserStats stats
    ) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getIsActive(),
                user.getIsVerified(),

                toKYCInfoDTO(kyc),
                toAgentProfileDTO(profile),
                toLocationDTO(location),
                toStatsDTO(stats)
        );
    }
    private KYCInfoDTO toKYCInfoDTO(AppUserKYC kyc) {
        if (kyc == null) return null;
        KYCInfoDTO dto = new KYCInfoDTO();

        dto.setStatus(kyc.getKycStatus());
        dto.setIdFrontUrl(kyc.getIdFrontUrl());
        dto.setIdBackUrl(kyc.getIdBackUrl());
        dto.setSelfieUrl(kyc.getSelfieUrl());
        dto.setProofOfAddressUrl(kyc.getProofOfAddressUrl());
        dto.setSubmittedAt(kyc.getSubmittedAt());
        dto.setVerifiedAt(kyc.getVerifiedAt());
        dto.setRejectionReason(kyc.getRejectionReason());

        return dto;
    }
    private AgentProfileDTO toAgentProfileDTO(AppUserAgentProfile profile) {
        if (profile == null) return null;
        AgentProfileDTO dto = new AgentProfileDTO();

        dto.setDriversLicenseNumber(profile.getDriversLicenseNumber());
        dto.setDriversLicenseExpiry(profile.getDriversLicenseExpiry());
        dto.setDriversLicenseFrontUrl(profile.getDriversLicenseFrontUrl());
        dto.setDriversLicenseBackUrl(profile.getDriversLicenseBackUrl());
        dto.setIsAvailable(profile.getIsAvailable());

        return dto;
    }
    private LocationDTO toLocationDTO(AppUserLocation loc) {
        if (loc == null) return null;
        LocationDTO dto = new LocationDTO();

        dto.setLatitude(loc.getLatitude());
        dto.setLongitude(loc.getLongitude());
        dto.setUpdatedAt(loc.getUpdatedAt());

        return dto;
    }
    private StatsDTO toStatsDTO(AppUserStats stats) {
        if (stats == null) return null;
        StatsDTO dto = new StatsDTO();

        dto.setRating(stats.getRating());
        dto.setTotalDeliveries(stats.getTotalDeliveries());
        dto.setTotalEarnings(stats.getTotalEarnings());

        return dto;
    }

    public void updateEntity(AppUser user, UserUpdateDTO dto) {
        if (dto.getPhoneNumber() != null) user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
    }
}
