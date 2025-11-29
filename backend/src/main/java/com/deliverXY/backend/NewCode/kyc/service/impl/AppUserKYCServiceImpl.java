package com.deliverXY.backend.NewCode.kyc.service.impl;

import com.deliverXY.backend.NewCode.common.enums.KYCStatus;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.kyc.domain.AppUserKYC;
import com.deliverXY.backend.NewCode.kyc.repository.AppUserKYCRepository;
import com.deliverXY.backend.NewCode.kyc.service.AppUserKYCService;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AppUserKYCServiceImpl implements AppUserKYCService {
    private final AppUserKYCRepository repo;
    private final AppUserService userService;
    @Override
    public AppUserKYC submitKYC(Long userId, AppUserKYC kyc) {
        AppUser user = userService.requireById(userId);

        AppUserKYC existing = repo.findById(userId).orElse(new AppUserKYC());
        existing.setUser(user);
        existing.setId(userId);

        existing.setIdFrontUrl(kyc.getIdFrontUrl());
        existing.setIdBackUrl(kyc.getIdBackUrl());
        existing.setSelfieUrl(kyc.getSelfieUrl());
        existing.setProofOfAddressUrl(kyc.getProofOfAddressUrl());

        existing.setKycStatus(KYCStatus.PENDING);
        existing.setSubmittedAt(LocalDateTime.now());

        return repo.save(existing);
    }

    @Override
    public AppUserKYC approveKYC(Long userId, String reviewer) {
        AppUserKYC kyc = getKYC(userId);

        kyc.setKycStatus(KYCStatus.APPROVED);
        kyc.setVerifiedAt(LocalDateTime.now());
        kyc.setRejectionReason(null);

        return repo.save(kyc);
    }

    @Override
    public AppUserKYC rejectKYC(Long userId, String reason) {
        AppUserKYC kyc = getKYC(userId);


        kyc.setKycStatus(KYCStatus.REJECTED);
        kyc.setRejectionReason(reason);

        return repo.save(kyc);
    }

    @Override
    public AppUserKYC getKYC(Long userId) {
        return repo.findById(userId)
                .orElseThrow(() -> new NotFoundException("KYC not found"));
    }

    @Override
    public long countByStatus(KYCStatus  kycStatus) {
        return repo.countByKycStatus(kycStatus);
    }
}
