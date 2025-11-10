package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.service.AppUserService;
import com.deliverXY.backend.service.CustomUserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Service
@Slf4j
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    private final AppUserService appUserService;

    public CustomUserDetailsServiceImpl(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", identifier);

        AppUser appUser = identifier.contains("@")
                ? appUserService.findByEmail(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + identifier))
                : appUserService.findByUsername(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + identifier));

        log.debug("User found: {}, role: {}, active: {}", appUser.getUsername(), appUser.getRole(), appUser.getIsActive());

        return new User(
            appUser.getUsername(),
            appUser.getPassword(),
            appUser.getIsActive(),
            true, // accountNonExpired
            true, // credentialsNonExpired
            true, // accountNonLocked
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()))
        );
    }
} 