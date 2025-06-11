package com.deliverXY.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.deliverXY.backend.models.AppUser;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    void deleteByUsername(String username);
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmail(String email);

}