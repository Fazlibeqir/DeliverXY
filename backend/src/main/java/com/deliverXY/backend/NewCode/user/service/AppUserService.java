package com.deliverXY.backend.NewCode.user.service;


import com.deliverXY.backend.NewCode.user.domain.AppUser;

import java.util.List;
import java.util.Optional;

public interface AppUserService {

    Optional<AppUser> findById(Long id);
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByIdentifier(String identifier);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    List<AppUser> findAll();

    AppUser save(AppUser user);
    AppUser update(AppUser user);

    void deleteById(Long id);
    void deleteByUsername(String username);

    AppUser requireById(Long id);
    AppUser requireByUsername(String username);

    void activateUser(Long id);
    void deactivateUser(Long id);
}
