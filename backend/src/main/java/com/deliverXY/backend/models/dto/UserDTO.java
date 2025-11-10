package com.deliverXY.backend.models.dto;

import com.deliverXY.backend.enums.KYCStatus;
import com.deliverXY.backend.enums.UserRole;
import com.deliverXY.backend.models.AppUser;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class UserDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private KYCStatus kycStatus;
    private String idFrontUrl;
    private String idBackUrl;
    private String selfieUrl;
    private String proofOfAddressUrl;
    private LocalDateTime kycSubmittedAt;
    private LocalDateTime kycVerifiedAt;
    private String kycRejectionReason;
    private String driversLicenseNumber;
    private LocalDateTime driversLicenseExpiry;
    private String driversLicenseFrontUrl;
    private String driversLicenseBackUrl;
    private Boolean isAvailable;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime lastLocationUpdate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int vehicleCount;

    public UserDTO() {}

    public UserDTO(AppUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.role = user.getRole();
        this.kycStatus = user.getKycStatus();
        this.idFrontUrl = user.getIdFrontUrl();
        this.idBackUrl = user.getIdBackUrl();
        this.selfieUrl = user.getSelfieUrl();
        this.proofOfAddressUrl = user.getProofOfAddressUrl();
        this.kycSubmittedAt = user.getKycSubmittedAt();
        this.kycVerifiedAt = user.getKycVerifiedAt();
        this.kycRejectionReason = user.getKycRejectionReason();
        this.driversLicenseNumber = user.getDriversLicenseNumber();
        this.driversLicenseExpiry = user.getDriversLicenseExpiry();
        this.driversLicenseFrontUrl = user.getDriversLicenseFrontUrl();
        this.driversLicenseBackUrl = user.getDriversLicenseBackUrl();
        this.isAvailable = user.getIsAvailable();
        this.currentLatitude = user.getCurrentLatitude();
        this.currentLongitude = user.getCurrentLongitude();
        this.lastLocationUpdate = user.getLastLocationUpdate();
        this.isActive = user.getIsActive();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.vehicleCount = user.getVehicleCount();
    }

   }