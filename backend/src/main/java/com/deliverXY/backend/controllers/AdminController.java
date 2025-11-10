package com.deliverXY.backend.controllers;

import com.deliverXY.backend.enums.KYCStatus;
import com.deliverXY.backend.enums.UserRole;
import com.deliverXY.backend.enums.DeliveryStatus;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.dto.AppUserDTO;
import com.deliverXY.backend.service.AppUserService;
import com.deliverXY.backend.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    private final AppUserService appUserService;
    private final DeliveryService deliveryService;
    
    public AdminController(AppUserService appUserService, DeliveryService deliveryService) {
        this.appUserService = appUserService;
        this.deliveryService = deliveryService;
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // User statistics
            List<AppUser> allUsers = appUserService.findAll();
            stats.put("totalUsers", allUsers.size());
            stats.put("totalClients", allUsers.stream()
                    .filter(u -> u.getRole() != null && u.getRole() == UserRole.CLIENT)
                    .count());
            stats.put("totalAgents", allUsers.stream()
                    .filter(u -> u.getRole() != null && u.getRole()  == UserRole.AGENT)
                    .count());
            stats.put("pendingKYC", allUsers.stream()
                    .filter(u -> u.getKycStatus() != null && u.getKycStatus()  == KYCStatus.SUBMITTED)
                    .count());
            
            // Delivery statistics
            List<Delivery> allDeliveries = deliveryService.findAll();
            stats.put("totalDeliveries", allDeliveries.size());
            stats.put("pendingDeliveries", allDeliveries.stream().filter(d -> d.getStatus() == DeliveryStatus.REQUESTED).count());
            stats.put("activeDeliveries", allDeliveries.stream().filter(d -> d.getStatus() == DeliveryStatus.IN_TRANSIT).count());
            stats.put("completedDeliveries", allDeliveries.stream().filter(d -> d.getStatus() == DeliveryStatus.DELIVERED).count());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            // print stack trace to console for debugging
            e.printStackTrace();

            // return JSON error body in Postman
            Map<String, Object> error = new HashMap<>();
            error.put("errorType", e.getClass().getSimpleName());
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<AppUser>> getAllUsers() {
        try {
            List<AppUser> users = appUserService.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PostMapping("/users/{userId}/block")
    public ResponseEntity<String> blockUser(@PathVariable Long userId) {
        try {
            AppUser user = appUserService.findById(userId).orElseThrow();

            user.setIsActive(false);
            appUserService.update(userId, convertToDTO(user));
            
            return ResponseEntity.ok("User blocked successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to block user: " + e.getMessage());
        }
    }
    
    @PostMapping("/users/{userId}/unblock")
    public ResponseEntity<String> unblockUser(@PathVariable Long userId) {
        try {
            AppUser user = appUserService.findById(userId).orElseThrow();

            user.setIsActive(true);
            appUserService.update(userId, convertToDTO(user));
            
            return ResponseEntity.ok("User unblocked successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to unblock user: " + e.getMessage());
        }
    }
    
    @GetMapping("/deliveries")
    public ResponseEntity<List<Delivery>> getAllDeliveries() {
        try {
            List<Delivery> deliveries = deliveryService.findAll();
            return ResponseEntity.ok(deliveries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PostMapping("/deliveries/{deliveryId}/assign")
    public ResponseEntity<String> assignDeliveryToAgent(@PathVariable Long deliveryId, 
                                                      @RequestParam Long agentId) {
        try {
            AppUser agent = appUserService.findById(agentId).orElseThrow();

            deliveryService.assignToAgent(deliveryId, agent);
            return ResponseEntity.ok("Delivery assigned successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to assign delivery: " + e.getMessage());
        }
    }
    
    private AppUserDTO convertToDTO(AppUser user) {
        var dto = new AppUserDTO();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setIsActive(user.getIsActive());
        return dto;
    }
} 