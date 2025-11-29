package com.deliverXY.backend.NewCode.kyc.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.kyc.domain.AppUserKYC;
import com.deliverXY.backend.NewCode.kyc.dto.KYCInfoDTO;
import com.deliverXY.backend.NewCode.kyc.dto.KYCUpdateDTO;
import com.deliverXY.backend.NewCode.kyc.service.AppUserKYCService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
public class KYCController {

    private final AppUserKYCService kycService;

    @GetMapping
    public ApiResponse<KYCInfoDTO> getMyKYC(@AuthenticationPrincipal UserPrincipal principal) {
        AppUserKYC kyc = kycService.getKYC(principal.getUser().getId());
        return ApiResponse.ok(toDTO(kyc));
    }

    @PutMapping
    public ApiResponse<KYCInfoDTO> submitKYC(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody KYCUpdateDTO dto
    ) {
        AppUserKYC entity = new AppUserKYC();
        entity.setIdFrontUrl(dto.getIdFrontUrl());
        entity.setIdBackUrl(dto.getIdBackUrl());
        entity.setSelfieUrl(dto.getSelfieUrl());
        entity.setProofOfAddressUrl(dto.getProofOfAddressUrl());

        AppUserKYC saved = kycService.submitKYC(principal.getUser().getId(), entity);

        return ApiResponse.ok(toDTO(saved));
    }

    private KYCInfoDTO toDTO(AppUserKYC k) {
        if (k == null) return null;

        KYCInfoDTO dto = new KYCInfoDTO();
        dto.setStatus(k.getKycStatus());
        dto.setIdFrontUrl(k.getIdFrontUrl());
        dto.setIdBackUrl(k.getIdBackUrl());
        dto.setSelfieUrl(k.getSelfieUrl());
        dto.setProofOfAddressUrl(k.getProofOfAddressUrl());
        dto.setSubmittedAt(k.getSubmittedAt());
        dto.setVerifiedAt(k.getVerifiedAt());
        dto.setRejectionReason(k.getRejectionReason());
        return dto;
    }
}
