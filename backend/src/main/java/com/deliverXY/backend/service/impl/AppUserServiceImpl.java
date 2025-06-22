package com.deliverXY.backend.service.impl;

import org.springframework.stereotype.Service;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.dto.AppUserDTO;
import com.deliverXY.backend.repositories.AppUserRepository;
import com.deliverXY.backend.service.AppUserService;

import jakarta.transaction.Transactional;

import java.util.List;

@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserServiceImpl(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public AppUser findById(Long id) {
        return appUserRepository.findById(id).orElse(null);
    }

    @Override
    public List<AppUser> findAll() {
        return appUserRepository.findAll();
    }

    @Override
    @Transactional
    public AppUser create(AppUserDTO appUserDTO) {
        this.appUserRepository.deleteByUsername(appUserDTO.getUsername());// Why ?
        AppUser user = new AppUser(appUserDTO.getUsername(), appUserDTO.getEmail(), appUserDTO.getPassword(), appUserDTO.getPhoneNumber(),appUserDTO.getRole());
        return appUserRepository.save(user);
    }

    @Override
    @Transactional
    public AppUser update(Long id, AppUserDTO appUserDTO) {
        AppUser user = this.findById(id);
        user.setUsername(appUserDTO.getUsername());
        user.setEmail(appUserDTO.getEmail());
        user.setPassword(appUserDTO.getPassword());
        user.setPhoneNumber(appUserDTO.getPhoneNumber());

        return appUserRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        this.appUserRepository.deleteById(id);
    }


   

    @Override
    public AppUser findByEmail(String email) {
        return appUserRepository.findByEmail(email).orElse(null);   
    }
}