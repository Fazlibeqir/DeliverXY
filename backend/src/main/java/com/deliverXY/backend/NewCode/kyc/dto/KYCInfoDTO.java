package com.deliverXY.backend.NewCode.kyc.dto;

import com.deliverXY.backend.NewCode.common.enums.KYCStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KYCInfoDTO {
    private KYCStatus status;
    private String idFrontUrl;
    private String idBackUrl;
    private String selfieUrl;
    private String proofOfAddressUrl;

    private LocalDateTime submittedAt;
    private LocalDateTime verifiedAt;
    private String rejectionReason;
}
