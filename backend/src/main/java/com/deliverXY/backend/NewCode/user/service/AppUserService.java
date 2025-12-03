package com.deliverXY.backend.NewCode.user.service;


import com.deliverXY.backend.NewCode.common.enums.UserRole;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AppUserService {

    Optional<AppUser> findById(Long id);
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByIdentifier(String identifier);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Page<AppUser> findAll(Pageable pageable);

    AppUser save(AppUser user);
    AppUser update(AppUser user);

    void deleteById(Long id);
    void deleteByUsername(String username);

    AppUser requireById(Long id);
    AppUser requireByUsername(String username);

    void activateUser(Long id);
    void deactivateUser(Long id);

    long countALL();

    long countByRole(UserRole userRole);
}
