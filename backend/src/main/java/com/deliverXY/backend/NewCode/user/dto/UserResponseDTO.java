package com.deliverXY.backend.NewCode.user.dto;

import com.deliverXY.backend.NewCode.common.enums.UserRole;
import com.deliverXY.backend.NewCode.kyc.dto.KYCInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;

    private UserRole role;
    private Boolean isActive;
    private Boolean isVerified;

    private KYCInfoDTO kyc;
    private AgentProfileDTO agentProfile;
    private LocationDTO location;
    private StatsDTO stats;


}

