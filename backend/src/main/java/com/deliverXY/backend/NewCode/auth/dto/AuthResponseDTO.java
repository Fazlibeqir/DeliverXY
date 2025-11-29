package com.deliverXY.backend.NewCode.auth.dto;

import com.deliverXY.backend.NewCode.user.dto.UserResponseDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserResponseDTO user;
}