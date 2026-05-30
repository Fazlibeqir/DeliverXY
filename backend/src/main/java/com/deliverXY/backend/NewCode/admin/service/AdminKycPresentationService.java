package com.deliverXY.backend.NewCode.admin.service;

import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.kyc.domain.AppUserKYC;
import com.deliverXY.backend.NewCode.kyc.dto.KYCInfoDTO;
import com.deliverXY.backend.NewCode.kyc.service.AppUserKYCService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminKycPresentationService {

    private final AppUserKYCService kycService;

    @Transactional(readOnly = true)
    public KYCInfoDTO getKycInfo(@NonNull Long userId) {
        try {
            AppUserKYC kyc = kycService.getKYC(Objects.requireNonNull(userId, "userId"));
            if (kyc == null) {
                return null;
            }
            return toDto(kyc);
        } catch (NotFoundException e) {
            return null;
        }
    }

    private KYCInfoDTO toDto(AppUserKYC kyc) {
        KYCInfoDTO dto = new KYCInfoDTO();
        dto.setStatus(kyc.getKycStatus());
        dto.setIdFrontUrl(kyc.getIdFrontUrl());
        dto.setIdBackUrl(kyc.getIdBackUrl());
        dto.setSelfieUrl(kyc.getSelfieUrl());
        dto.setProofOfAddressUrl(kyc.getProofOfAddressUrl());
        dto.setSubmittedAt(kyc.getSubmittedAt());
        dto.setVerifiedAt(kyc.getVerifiedAt());
        dto.setRejectionReason(kyc.getRejectionReason());
        dto.setReviewedBy(kyc.getReviewedBy());
        return dto;
    }
}
