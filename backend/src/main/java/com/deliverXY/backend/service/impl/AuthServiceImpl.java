package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.enums.UserRole;
import com.deliverXY.backend.utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.dto.ReqRes;
import com.deliverXY.backend.repositories.AppUserRepository;
import com.deliverXY.backend.service.AuthService;

import java.util.HashMap;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {
    private final AppUserRepository appUserRepository;
    private final JwtUtil jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;


    public AuthServiceImpl(AppUserRepository appUserRepository,
                           JwtUtil jwtUtils,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager) {
        this.appUserRepository = appUserRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public ReqRes signUp(ReqRes registrationRequest) {
        ReqRes resp = new ReqRes();
        try {
            // Check if user exists
            if (appUserRepository.findByUsername(registrationRequest.getUsername()).isPresent() ||
                    appUserRepository.findByEmail(registrationRequest.getEmail()).isPresent()) {
                resp.setStatusCode(400);
                resp.setMessage("User already exists");
                return resp;
            }
            AppUser ourUsers = new AppUser();
            ourUsers.setUsername(registrationRequest.getUsername());
            ourUsers.setPhoneNumber(registrationRequest.getPhoneNumber());
            ourUsers.setEmail(registrationRequest.getEmail());
            ourUsers.setFirstName(registrationRequest.getFirstName());
            ourUsers.setLastName(registrationRequest.getLastName());
            ourUsers.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            ourUsers.setRole(registrationRequest.getRole());
            ourUsers.setIsActive(true);


            appUserRepository.save(ourUsers);

            resp.setStatusCode(200);
            resp.setAppUser(ourUsers);
            resp.setMessage("User Saved Successfully");

        }catch (Exception e){
            resp.setStatusCode(500);
            resp.setError("Error during signup: " + e.getMessage());
        }
        return resp;
    }

    @Override
    public ReqRes signIn(ReqRes loginRequest) {
        ReqRes response = new ReqRes();

        try {
            if (loginRequest.getIdentifier() == null || loginRequest.getIdentifier().isEmpty()) {
                throw new IllegalArgumentException("Username or email is required");
            }

            // Authenticate user first
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getIdentifier(),
                            loginRequest.getPassword()
                    )
            );

            AppUser user = loginRequest.getIdentifier().contains("@")
                    ? appUserRepository.findByEmail(loginRequest.getIdentifier())
                    .orElseThrow(() -> new Exception("User not found with email: " + loginRequest.getIdentifier()))
                    : appUserRepository.findByUsername(loginRequest.getIdentifier())
                    .orElseThrow(() -> new Exception("User not found with username: " + loginRequest.getIdentifier()));

            // Generate JWT
            String jwt = jwtUtils.generateToken(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            null,
                            List.of(() -> "ROLE_" + user.getRole().name())
                    )
            );

            response.setStatusCode(200);
            response.setAppUser(user);
            response.setToken(jwt);
            response.setRefreshToken(jwt);
            response.setExpirationTime("24Hr");
            response.setMessage("Successfully Signed In");

        } catch (IllegalArgumentException e) {
            response.setStatusCode(400);
            response.setError(e.getMessage());
        } catch (org.springframework.security.core.AuthenticationException e) {
            response.setStatusCode(401);
            response.setError("Invalid username/email or password");
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setError("Authentication failed: " + e.getMessage());
        }

        return response;
    }


    @Override
    public ReqRes refreshToken(ReqRes refreshTokenRequest) {
        ReqRes response = new ReqRes();

        try{
            String username = jwtUtils.getUsernameFromToken(refreshTokenRequest.getToken());
            if(username == null){
                throw new IllegalArgumentException("Invalid refresh token");
            }
            AppUser user = appUserRepository.findByUsername(username)
                    .orElseThrow(()->new Exception("User not found with username: " + username));
            if (!jwtUtils.validateToken(refreshTokenRequest.getToken())){
                throw new IllegalArgumentException("Invalid refresh token");
            }
            String newToken = jwtUtils.generateToken(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            null,
                            List.of(()->"ROLE_"+user.getRole().name()
                            )));

            response.setStatusCode(200);
            response.setToken(newToken);
            response.setRefreshToken(refreshTokenRequest.getToken());
            response.setExpirationTime("24Hr");
            response.setMessage("Successfully Refreshed Token");

        }catch (Exception e){
            response.setStatusCode(401);
            response.setError("Token refresh failed: " + e.getMessage());
        }


        return response;
    }
}