package com.deliverXY.backend.NewCode.auth.service;

import com.deliverXY.backend.NewCode.auth.dto.AuthResponseDTO;
import com.deliverXY.backend.NewCode.auth.dto.LoginRequest;
import com.deliverXY.backend.NewCode.auth.dto.RefreshTokenRequest;
import com.deliverXY.backend.NewCode.auth.dto.RegisterRequest;
import com.deliverXY.backend.NewCode.auth.factory.UserFactory;
import com.deliverXY.backend.NewCode.auth.mapper.AuthMapper;
import com.deliverXY.backend.NewCode.auth.validator.AuthValidationService;
import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import com.deliverXY.backend.NewCode.security.JwtService;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import com.deliverXY.backend.NewCode.user.dto.UserResponseDTO;
import com.deliverXY.backend.NewCode.exceptions.UnauthorizedException;
import com.deliverXY.backend.NewCode.wallet.service.WalletService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AppUserService userService;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final WalletService walletService;
    private final UserFactory userFactory;
    private final AuthMapper mapper;
    private final AuthValidationService authValidationService;
    private final TokenBlacklistService tokenBlacklistService;


    @Override
    public AuthResponseDTO register(RegisterRequest req) {

        authValidationService.validateRegistration(req.getUsername(), req.getEmail());

        AppUser user = userFactory.create(req);

        userService.save(user);

        walletService.createWalletForUser(user);

        return buildTokens(user);
    }

    @Override
    public AuthResponseDTO  login(LoginRequest req) {


        AppUser user = userService.findByIdentifier(req.getIdentifier())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.getIdentifier(),
                            req.getPassword()
                    )
            );
        }catch (Exception e){
            throw new UnauthorizedException("Invalid username/email or password");
        }

        return buildTokens(user);
    }



    @Override
    public AuthResponseDTO refresh(RefreshTokenRequest req) {

        String token = req.getRefreshToken();

        if (jwtService.isExpired(token)){
            throw new UnauthorizedException("Refresh token expired");
        }

        // Validate type
        Claims claims = jwtService.parseClaims(token);
        String type = claims.get("type", String.class);

        if (!"refresh".equals(type)) {
            throw new BadRequestException("Invalid token type");
        }

        String username = claims.getSubject();

        AppUser user = userService.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        String newAccess = jwtService.generateAccessToken(user);

        return mapper.toAuthResponse(
                newAccess,
                token,
                jwtService.getRefreshTokenExpirySeconds(),
                mapper.toBasicUser(user)
        );
    }


    @Override
    public UserResponseDTO getCurrentUser(UserPrincipal principal){
        if (principal == null){
            throw new UnauthorizedException("User not logged in");
        }
        return mapper.toBasicUser(principal.getUser());
    }

    @Override
    public void logout(String authHeader, UserPrincipal principal) {
        // Allow logout even if principal is null (token might be expired)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklist(token);
        }
        // If principal is null or authHeader is invalid, just return (already logged out)
    }

    private AuthResponseDTO buildTokens(AppUser user) {
        return mapper.toAuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                jwtService.getAccessTokenExpirySeconds(),
                mapper.toBasicUser(user));

    }

}