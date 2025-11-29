package com.deliverXY.backend.NewCode.kyc.service;

import com.deliverXY.backend.NewCode.common.enums.KYCStatus;
import com.deliverXY.backend.NewCode.kyc.domain.AppUserKYC;

public interface AppUserKYCService {
    AppUserKYC submitKYC(Long userId, AppUserKYC kyc);
    AppUserKYC approveKYC(Long userId, String reviewer);
    AppUserKYC rejectKYC(Long userId, String reason);
    AppUserKYC  getKYC(Long userId);
    long countByStatus(KYCStatus kycStatus);
}
