package com.deliverXY.backend.models.dto;

import com.deliverXY.backend.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import com.deliverXY.backend.models.AppUser;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReqRes {
    private String username;
    private String email;
    private String phoneNumber;
    private String identifier;
    private String password;
    private String firstName;
    private String lastName;
    private UserRole role;

    private int statusCode;
    private String message;
    private String error;
    private String token;
    private String refreshToken;
    private String expirationTime;

    private AppUser appUser;
}
