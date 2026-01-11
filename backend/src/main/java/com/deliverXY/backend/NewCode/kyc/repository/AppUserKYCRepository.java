package com.deliverXY.backend.NewCode.kyc.repository;

import com.deliverXY.backend.NewCode.common.enums.KYCStatus;
import com.deliverXY.backend.NewCode.kyc.domain.AppUserKYC;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserKYCRepository extends JpaRepository<AppUserKYC, Long> {
    long countByKycStatus(KYCStatus status);
}
