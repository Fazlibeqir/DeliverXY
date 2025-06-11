package com.deliverXY.backend.models.dto;

import lombok.Data;

@Data
public class AppUserDTO {
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    private String role;



    public AppUserDTO() {
    }

    public AppUserDTO(String username, String email, String password, String phoneNumber,String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
}