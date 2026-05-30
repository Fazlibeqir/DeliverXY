package com.deliverXY.backend.NewCode.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeviceTokenRequest {

    @NotBlank
    @Size(max = 512)
    private String token;

    @Pattern(regexp = "ANDROID|IOS|WEB", message = "platform must be ANDROID, IOS, or WEB")
    private String platform = "ANDROID";
}
