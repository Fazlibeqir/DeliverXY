package com.deliverXY.backend.NewCode.user.service.impl;

import com.deliverXY.backend.NewCode.common.enums.UserRole;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.repository.AppUserRepository;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository repo;

    @Override
    public Optional<AppUser> findById(Long id) {
        return repo.findById(Objects.requireNonNull(id, "id"));
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
        return repo.findByUsername(username);
    }

    @Override
    public Optional<AppUser> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    @Override
    public Optional<AppUser> findByIdentifier(String identifier) {
        return repo.findByIdentifier(identifier);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repo.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return repo.existsByUsername(username);
    }

    @Override
    public Page<AppUser> findAll(Pageable pageable) {
        return repo.findAll(Objects.requireNonNull(pageable, "pageable"));
    }

    @Override
    public AppUser save(AppUser user) {
        return repo.save(Objects.requireNonNull(user, "user"));
    }

    @Override
    public AppUser update(AppUser user) {
        return repo.save(Objects.requireNonNull(user, "user"));
    }

    @Override
    public void deleteById(Long id) {
        Long userId = Objects.requireNonNull(id, "id");
        if (!repo.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
        repo.deleteById(userId);
    }

    @Override
    public void deleteByUsername(String username) {
        if (!repo.existsByUsername(username)) {
            throw new NotFoundException("User not found: " + username);
        }
        repo.deleteByUsername(username);
    }
    @Override
    public AppUser requireById(Long id) {
        Long userId = Objects.requireNonNull(id, "id");
        return repo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }
    @Override
    public AppUser requireByUsername(String username) {
        return repo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }
    @Override
    public void activateUser(Long id) {
        AppUser user = requireById(id);
        user.setIsActive(true);
        repo.save(user);
    }
    @Override
    public void deactivateUser(Long id) {
        AppUser user = requireById(id);
        user.setIsActive(false);
        repo.save(user);
    }

    @Override
    public long countALL() {
        return repo.countAll();
    }

    @Override
    public long countByRole(UserRole userRole) {
        return repo.countByRole(userRole);
    }
}
