package com.deliverXY.backend.models.dto;

import com.deliverXY.backend.enums.UserRole;
import lombok.Data;

@Data
public class AuthResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private UserRole role;
    private Boolean isVerified;
    private String message;
    
    public AuthResponseDTO() {
    }
    
    public AuthResponseDTO(String accessToken, String refreshToken, Long userId, 
                          String username, String email, UserRole role, Boolean isVerified) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.isVerified = isVerified;
    }
    
    public AuthResponseDTO(String message) {
        this.message = message;
    }
} 