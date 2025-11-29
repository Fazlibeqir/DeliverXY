package com.deliverXY.backend.NewCode.kyc.domain;

import com.deliverXY.backend.NewCode.common.enums.KYCStatus;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@Table(name = "app_user_kyc")
@Data
@NoArgsConstructor
public class AppUserKYC {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Enumerated(EnumType.STRING)
    private KYCStatus kycStatus = KYCStatus.PENDING;

    private String idFrontUrl;
    private String idBackUrl;
    private String selfieUrl;
    private String proofOfAddressUrl;

    private LocalDateTime submittedAt;
    private LocalDateTime verifiedAt;
    private String rejectionReason;
}
