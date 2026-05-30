package com.deliverXY.backend.NewCode.user.service;

import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.dto.UserResponseDTO;
import com.deliverXY.backend.NewCode.user.dto.UserUpdateDTO;
import com.deliverXY.backend.NewCode.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserMeService {

    private final AppUserService userService;
    private final UserProfileLoaderService profileLoader;
    private final UserMapper mapper;

    @Transactional(readOnly = true)
    public UserResponseDTO getMe(AppUser user) {
        return buildFullProfile(user);
    }

    @Transactional
    public UserResponseDTO updateMe(AppUser user, UserUpdateDTO dto) {
        mapper.updateEntity(user, dto);
        userService.update(user);
        return buildFullProfile(user);
    }

    private UserResponseDTO buildFullProfile(AppUser user) {
        Long id = user.getId();
        return mapper.toFullDTO(
                user,
                profileLoader.getKYC(id),
                profileLoader.getProfile(id),
                profileLoader.getLocation(id),
                profileLoader.getStats(id)
        );
    }
}
