package com.deliverXY.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.dto.ReqRes;
import com.deliverXY.backend.service.AppUserService;
import com.deliverXY.backend.service.AuthService;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
     private final AppUserService appUserService;


    public AuthController(AuthService authService, AppUserService appUserService) {
        this.authService = authService;
        this.appUserService = appUserService;
    }
    @GetMapping("/me")
    public ResponseEntity<AppUser> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) {
        return ResponseEntity.status(401).build(); // Not authenticated
    }

    String identifier  = userDetails.getUsername(); // Usually the email or username
        AppUser user = appUserService.findByUsername(identifier)
                .or(() -> appUserService.findByEmail(identifier))
                .orElseThrow(() -> new NoSuchElementException("User not found")); // You need to implement this

        return ResponseEntity.ok(user);
    }
    @PostMapping("/register")
    public ResponseEntity<ReqRes> register(@RequestBody ReqRes registerRequest){
        return ResponseEntity.ok().body(authService.signUp(registerRequest));
    }
    @PostMapping("/login")
    public ResponseEntity<ReqRes> signIn(@RequestBody ReqRes loginRequest){
        return ResponseEntity.ok().body(authService.signIn(loginRequest));
    }
    @PostMapping("/refresh")
    public ResponseEntity<ReqRes> refresh(
            @RequestBody(required = false) ReqRes refreshRequest,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = (refreshRequest != null && refreshRequest.getToken() != null)
                ? refreshRequest.getToken()
                : (authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : null);

        if (token == null) {
            ReqRes response = new ReqRes();
            response.setStatusCode(400);
            response.setError("Missing token in body or header");
            return ResponseEntity.badRequest().body(response);
        }
        ReqRes request = new ReqRes();
        request.setToken(token);
        request.setRefreshToken(token);

        ReqRes response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

}