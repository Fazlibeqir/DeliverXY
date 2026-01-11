package com.deliverXY.backend.NewCode.user.controller;


import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.dto.UserResponseDTO;
import com.deliverXY.backend.NewCode.user.dto.UserUpdateDTO;
import com.deliverXY.backend.NewCode.user.mapper.UserMapper;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import com.deliverXY.backend.NewCode.user.service.UserProfileLoaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final AppUserService userService;
    private final UserProfileLoaderService profileLoader;
    private final UserMapper mapper;

    @GetMapping("/me")
    public ApiResponse<UserResponseDTO> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        AppUser user = principal.getUser();
        Long id = user.getId();
        return ApiResponse.ok(
                mapper.toFullDTO(
                        user,
                        profileLoader.getKYC(id),
                        profileLoader.getProfile(id),
                        profileLoader.getLocation(id),
                        profileLoader.getStats(id)
                ));
    }
    @PutMapping("/me")
    public ApiResponse<UserResponseDTO> updateMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UserUpdateDTO dto
    ) {
        AppUser user = principal.getUser();
        mapper.updateEntity(user, dto);
        userService.update(user);

        Long id = user.getId();

        return ApiResponse.ok(mapper.toFullDTO(
                user,
                profileLoader.getKYC(id),
                profileLoader.getProfile(id),
                profileLoader.getLocation(id),
                profileLoader.getStats(id)
        ));
    }
    @DeleteMapping("/me")
    public ApiResponse<String> deleteMe(@AuthenticationPrincipal UserPrincipal principal) {
        userService.deleteById(principal.getUser().getId());
        return ApiResponse.ok("Account deleted.");
    }
}
