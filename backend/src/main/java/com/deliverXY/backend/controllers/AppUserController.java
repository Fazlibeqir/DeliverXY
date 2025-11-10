package com.deliverXY.backend.controllers;

import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.dto.AppUserDTO;
import com.deliverXY.backend.models.dto.UserDTO;
import com.deliverXY.backend.service.AppUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class AppUserController {
    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public ResponseEntity<?> getUsers() {
        var authResult = authUser();
        if (authResult instanceof ResponseEntity) return (ResponseEntity<?>) authResult;
        AppUser currentUser = (AppUser) authResult;

        try {
            List<AppUser> users = appUserService.findAll();
            List<UserDTO> userDTOs = users.stream()
                    .map(UserDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to fetch users: " + e.getMessage());
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        var authResult = authUser();
        if (authResult instanceof ResponseEntity) return (ResponseEntity<?>) authResult;
        AppUser currentUser = (AppUser) authResult;
        try {

            AppUser appUser = appUserService.findById(id).orElseThrow();
            // Convert to DTO to prevent circular reference issues
            UserDTO userDTO = new UserDTO(appUser);
            return ResponseEntity.ok().body(userDTO);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to fetch user: " + e.getMessage());
        }
    }

    @PostMapping("/add-user")
    public ResponseEntity<?> addUser(@RequestBody AppUserDTO appUserDTO){
        var authResult = authUser();
        if (authResult instanceof ResponseEntity) return (ResponseEntity<?>) authResult;
        AppUser currentUser = (AppUser) authResult;
        try {

            // Validate input
            if(appUserDTO.getEmail() == null || appUserDTO.getUsername() == null || appUserDTO.getPassword() == null){
                return ResponseEntity.badRequest().body("Email, username, and password are required");
            }

            // Check if user already exists
            if (appUserService.findByUsername(appUserDTO.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body("Username already exists");
            }

            if (appUserService.findByEmail(appUserDTO.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Email already exists");
            }

            AppUser appUser = this.appUserService.create(appUserDTO);
            // Convert to DTO to prevent circular reference issues
            UserDTO userDTO = new UserDTO(appUser);
            return ResponseEntity.ok().body(userDTO);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create user: " + e.getMessage());
        }
    }

    @PostMapping("/edit-user/{id}")
    public ResponseEntity<?> editUser(@PathVariable Long id, @RequestBody AppUserDTO appUserDTO){
        var authResult = authUser();
        if (authResult instanceof ResponseEntity) return (ResponseEntity<?>) authResult;
        AppUser currentUser = (AppUser) authResult;
        try {

            // Validate input
            if(appUserDTO == null){
                return ResponseEntity.badRequest().body("User data is required");
            }
            if(appUserDTO.getEmail() == null || appUserDTO.getUsername() == null || appUserDTO.getPassword() == null){
                return ResponseEntity.badRequest().body("Email, username, and password are required");
            }
            if(id == null || appUserService.findById(id).isEmpty()){
                return ResponseEntity.notFound().build();
            }

            this.appUserService.update(id, appUserDTO);
            return ResponseEntity.ok("User updated successfully");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to update user: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
        var authResult = authUser();
        if (authResult instanceof ResponseEntity) return (ResponseEntity<?>) authResult;
        AppUser currentUser = (AppUser) authResult;
        try {

            if(id == null || appUserService.findById(id).isEmpty()){
                return ResponseEntity.notFound().build();
            }

            this.appUserService.deleteById(id);
            return ResponseEntity.ok("User deleted successfully");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete user: " + e.getMessage());
        }
    }
    private Object authUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        AppUser currentUser = appUserService.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + authentication.getName()));

        if (currentUser == null || !"ADMIN".equals(currentUser.getRole().name())) {
            return ResponseEntity.status(403).body("Access denied. Admin role required.");
        }
        return currentUser;
    }
}