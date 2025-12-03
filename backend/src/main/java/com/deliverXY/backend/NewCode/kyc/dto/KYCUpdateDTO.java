package com.deliverXY.backend.NewCode.kyc.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class KYCUpdateDTO {
    private static final String URL_PATTERN = "^/uploads/.*$";

    @Pattern(regexp = URL_PATTERN, message = "Invalid front ID URL format")
    private String idFrontUrl;

    @Pattern(regexp = URL_PATTERN, message = "Invalid back ID URL format")
    private String idBackUrl;

    @Pattern(regexp = URL_PATTERN, message = "Invalid selfie URL format")
    private String selfieUrl;

    @Pattern(regexp = URL_PATTERN, message = "Invalid proof of address URL format")
    private String proofOfAddressUrl;
}

