package com.deliverXY.backend.models.dto;

import lombok.Data;

@Data
public class KYCDTO {
    private Long userId;
    private String idFrontUrl;
    private String idBackUrl;
    private String selfieUrl;
    private String proofOfAddressUrl;
    
    public KYCDTO() {
    }
    
    public KYCDTO(Long userId, String idFrontUrl, String idBackUrl, 
                   String selfieUrl, String proofOfAddressUrl) {
        this.userId = userId;
        this.idFrontUrl = idFrontUrl;
        this.idBackUrl = idBackUrl;
        this.selfieUrl = selfieUrl;
        this.proofOfAddressUrl = proofOfAddressUrl;
    }
} 