package com.deliverXY.backend.models;

import com.deliverXY.backend.enums.KYCStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.deliverXY.backend.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
public class AppUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    // KYC Fields
    @Column(name = "kyc_status")
    @Enumerated(EnumType.STRING)
    private KYCStatus kycStatus = KYCStatus.PENDING;

    @Column(name = "id_front_url")
    private String idFrontUrl;

    @Column(name = "id_back_url")
    private String idBackUrl;

    @Column(name = "selfie_url")
    private String selfieUrl;

    @Column(name = "proof_of_address_url")
    private String proofOfAddressUrl;

    @Column(name = "kyc_submitted_at")
    private LocalDateTime kycSubmittedAt;

    @Column(name = "kyc_verified_at")
    private LocalDateTime kycVerifiedAt;

    @Column(name = "kyc_rejection_reason")
    private String kycRejectionReason;

    // Driver's License Information
    @Column(name = "drivers_license_number")
    private String driversLicenseNumber;

    @Column(name = "drivers_license_expiry")
    private LocalDateTime driversLicenseExpiry;

    @Column(name = "drivers_license_front_url")
    private String driversLicenseFrontUrl;

    @Column(name = "drivers_license_back_url")
    private String driversLicenseBackUrl;

    // Vehicle Information (for delivery agents)
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner"})
    private List<Vehicle> vehicles = new ArrayList<>();

    // Custom getter for vehicles that can be used in DTOs
    @JsonIgnore
    public List<Vehicle> getVehiclesForSerialization() {
        return vehicles;
    }

    // Check if user has vehicles without serializing the full list
    @JsonIgnore
    public boolean hasVehicles() {
        return vehicles != null && !vehicles.isEmpty();
    }

    // Get vehicle count without serializing the full list
    @JsonIgnore
    public int getVehicleCount() {
        return vehicles != null ? vehicles.size() : 0;
    }

    // Delivery Agent Specific Fields
    @Column(name = "is_available")
    private Boolean isAvailable = false;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;

    @Column(name = "total_earnings")
    private Double totalEarnings = 0.0;

    // Account Status
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();



    public AppUser(String username, String email, String password, String phoneNumber, UserRole role) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(isActive);
    }
}