package com.deliverXY.backend.NewCode.user.repository;

import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.common.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmail(String email);

    default Optional<AppUser> findByIdentifier(String identifier){
        return identifier.contains("@")
                ? findByEmail(identifier)
                : findByUsername(identifier);
    }
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Long countByRoleAndIsActiveTrue(UserRole role);

    void deleteByUsername(String username);
    long countAll();
    long countByRole(UserRole role);
}