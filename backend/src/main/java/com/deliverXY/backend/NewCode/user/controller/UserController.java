package com.deliverXY.backend.NewCode.user.controller;


import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.user.dto.UserResponseDTO;
import com.deliverXY.backend.NewCode.user.dto.UserUpdateDTO;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import com.deliverXY.backend.NewCode.user.service.UserMeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserController {
    private final AppUserService userService;
    private final UserMeService userMeService;

    @GetMapping("/me")
    public ApiResponse<UserResponseDTO> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(userMeService.getMe(principal.getUser()));
    }

    @PutMapping("/me")
    public ApiResponse<UserResponseDTO> updateMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserUpdateDTO dto
    ) {
        return ApiResponse.ok(userMeService.updateMe(principal.getUser(), dto));
    }

    @DeleteMapping("/me")
    public ApiResponse<String> deleteMe(@AuthenticationPrincipal UserPrincipal principal) {
        userService.deleteById(principal.getUser().getId());
        return ApiResponse.ok("Account deleted");
    }
}
