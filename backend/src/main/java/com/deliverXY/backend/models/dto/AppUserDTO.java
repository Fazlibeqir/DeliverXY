package com.deliverXY.backend.models.dto;

import com.deliverXY.backend.enums.UserRole;
import com.deliverXY.backend.enums.KYCStatus;
import lombok.Data;

@Data
public class AppUserDTO {
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private UserRole role;
    
    // KYC Fields
    private KYCStatus kycStatus;
    private String idFrontUrl;
    private String idBackUrl;
    private String selfieUrl;
    private String proofOfAddressUrl;
    private String kycRejectionReason;
    
    // Agent Specific Fields
    private Boolean isAvailable;
    private Double currentLatitude;
    private Double currentLongitude;
    private Boolean isActive;
    private Boolean isVerified;
    
    public AppUserDTO() {
    }
    
    public AppUserDTO(String username, String email, String password, String phoneNumber, UserRole role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
}