package com.deliverXY.backend.service;

import com.deliverXY.backend.models.dto.KYCDTO;
import java.util.List;

public interface KYCService {
    void submitKYC(KYCDTO kycDTO);
    List<KYCDTO> getPendingKYC();
    void approveKYC(Long userId);
    void rejectKYC(Long userId, String reason);
    String getKYCStatus(Long userId);
} 