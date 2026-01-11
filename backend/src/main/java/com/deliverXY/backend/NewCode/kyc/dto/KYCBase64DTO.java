package com.deliverXY.backend.NewCode.kyc.dto;

import lombok.Data;

@Data
public class KYCBase64DTO {
    private String documentType; // ID_FRONT, ID_BACK, SELFIE, PROOF_OF_ADDRESS
    private String base64;       // image base64 (NO data:image/... prefix)
}
