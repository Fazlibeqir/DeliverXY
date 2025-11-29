package com.deliverXY.backend.NewCode.user.validator;

import com.deliverXY.backend.NewCode.exceptions.ConflictException;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.user.repository.AppUserRepository;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserValidationService {
    private final AppUserRepository userRepo;

    public void assertEmailNotTaken(String email) {
        if (userRepo.existsByEmail(email)) {
            throw new ConflictException("Email already taken");
        }
    }

    public void assertUsernameNotTaken(String username) {
        if (userRepo.existsByUsername(username)) {
            throw new ConflictException("Username already taken");
        }
    }

    public void assertUserExists(Long id) {
        if (!userRepo.existsById(id)) {
            throw new NotFoundException("User not found: " + id);
        }
    }
}
