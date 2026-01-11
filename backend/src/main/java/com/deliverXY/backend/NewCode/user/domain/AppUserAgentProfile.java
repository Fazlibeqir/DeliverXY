package com.deliverXY.backend.NewCode.user.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_user_agent_profile")
@Data
@NoArgsConstructor
public class AppUserAgentProfile {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private AppUser user;

    // DRIVER LICENSE
    private String driversLicenseNumber;
    private LocalDateTime driversLicenseExpiry;
    private String driversLicenseFrontUrl;
    private String driversLicenseBackUrl;

    //DRIVER AVAILABILITY
    private Boolean isAvailable = false;

   }
