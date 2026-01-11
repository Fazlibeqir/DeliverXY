package com.deliverXY.backend.service;

import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.dto.AppUserDTO;

import java.util.List;

public interface AppUserService {
    AppUser findById(Long id);
    List<AppUser> findAll();
    AppUser create(AppUserDTO appUserDTO);
    AppUser update(Long id, AppUserDTO appUserDTO);
    AppUser findByEmail(String email);

    void deleteById(Long id);
}