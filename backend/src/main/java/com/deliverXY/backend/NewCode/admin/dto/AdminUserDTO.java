package com.deliverXY.backend.NewCode.admin.dto;

import com.deliverXY.backend.NewCode.common.enums.KYCStatus;
import com.deliverXY.backend.NewCode.kyc.domain.AppUserKYC;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminUserDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private Boolean isActive;
    private Boolean kycVerified;
    private KYCStatus kycStatus; // Add KYC status for filtering

    public AdminUserDTO(AppUser user, AppUserKYC kyc) {
        this.id = user.getId();
        this.fullName = (user.getFirstName() == null ? "" : user.getFirstName()) + " " +
                (user.getLastName() == null ? "" : user.getLastName());
        this.email = user.getEmail();
        this.phone = user.getPhoneNumber();
        this.role = user.getRole().name();
        this.isActive = user.getIsActive();
        this.kycStatus = kyc != null ? kyc.getKycStatus() : null;
        this.kycVerified = (kyc != null && kyc.getKycStatus() == KYCStatus.APPROVED);
    }
}
