package com.deliverXY.backend.NewCode.admin.dto;

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

    public AdminUserDTO(AppUser user) {
        this.id = user.getId();
        this.fullName = user.getFirstName() + " " + user.getLastName();
        this.email = user.getEmail();
        this.phone = user.getPhoneNumber();
        this.role = user.getRole().name();
        this.isActive = user.getIsActive();
        this.kycVerified = user.getIsVerified();
    }
}
