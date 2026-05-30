package com.deliverXY.backend.NewCode.kyc.service;

import com.deliverXY.backend.NewCode.common.enums.KYCStatus;
import com.deliverXY.backend.NewCode.kyc.domain.AppUserKYC;
import com.deliverXY.backend.NewCode.kyc.dto.KYCUpdateDTO;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface AppUserKYCService {
    AppUserKYC submitKYC(@NonNull Long userId, KYCUpdateDTO kyc);
    AppUserKYC approveKYC(@NonNull Long userId, String reviewer);
    AppUserKYC rejectKYC(@NonNull Long userId, String reason, String reviewer);
    AppUserKYC getKYC(@NonNull Long userId);
    Optional<AppUserKYC> findKYC(@NonNull Long userId);
    long countByStatus(KYCStatus kycStatus);
}
