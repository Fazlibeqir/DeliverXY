package com.deliverXY.backend.NewCode.kyc.service;

import com.deliverXY.backend.NewCode.common.enums.KYCStatus;
import com.deliverXY.backend.NewCode.kyc.domain.AppUserKYC;
import com.deliverXY.backend.NewCode.kyc.dto.KYCUpdateDTO;

import java.util.Optional;

public interface AppUserKYCService {
    AppUserKYC submitKYC(Long userId, KYCUpdateDTO kyc);
    AppUserKYC approveKYC(Long userId, String reviewer);
    AppUserKYC rejectKYC(Long userId, String reason, String reviewer);
    AppUserKYC  getKYC(Long userId);
    Optional<AppUserKYC> findKYC(Long userId);
    long countByStatus(KYCStatus kycStatus);
}
