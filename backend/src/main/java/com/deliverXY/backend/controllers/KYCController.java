package com.deliverXY.backend.controllers;

import com.deliverXY.backend.enums.KYCStatus;
import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.Vehicle;
import com.deliverXY.backend.models.dto.KYCRequestDTO;
import com.deliverXY.backend.service.AppUserService;
import com.deliverXY.backend.service.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/kyc")
@CrossOrigin(origins = "*")
public class KYCController {
    
    private final AppUserService appUserService;
    private final VehicleService vehicleService;
    
    public KYCController(AppUserService appUserService, VehicleService vehicleService) {
        this.appUserService = appUserService;
        this.vehicleService = vehicleService;
    }
    
    @PostMapping("/submit")
    public ResponseEntity<?> submitKYC(@Valid @RequestBody KYCRequestDTO kycRequest) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            AppUser user = appUserService.findByUsername(username).orElseThrow();

            // Update user information
            user.setFirstName(kycRequest.getFirstName());
            user.setLastName(kycRequest.getLastName());
            user.setPhoneNumber(kycRequest.getPhoneNumber());
            
            // Update KYC document URLs
            user.setIdFrontUrl(kycRequest.getIdFrontUrl());
            user.setIdBackUrl(kycRequest.getIdBackUrl());
            user.setSelfieUrl(kycRequest.getSelfieUrl());
            user.setProofOfAddressUrl(kycRequest.getProofOfAddressUrl());
            
            // Update driver's license information if provided
            if (kycRequest.getDriversLicenseNumber() != null) {
                user.setDriversLicenseNumber(kycRequest.getDriversLicenseNumber());
                user.setDriversLicenseExpiry(kycRequest.getDriversLicenseExpiry());
                user.setDriversLicenseFrontUrl(kycRequest.getDriversLicenseFrontUrl());
                user.setDriversLicenseBackUrl(kycRequest.getDriversLicenseBackUrl());
            }
            
            // Update KYC status
            user.setKycStatus(KYCStatus.SUBMITTED);
            user.setKycSubmittedAt(LocalDateTime.now());
            
            // Save updated user
            appUserService.updateUser(user);
            
            // Handle vehicle information if provided
            if (kycRequest.getVehicles() != null && !kycRequest.getVehicles().isEmpty()) {
                for (KYCRequestDTO.VehicleDTO vehicleDTO : kycRequest.getVehicles()) {
                    Vehicle vehicle = new Vehicle();
                    vehicle.setOwner(user);
                    vehicle.setVehicleType(Vehicle.VehicleType.valueOf(vehicleDTO.getVehicleType().toUpperCase()));
                    vehicle.setMake(vehicleDTO.getMake());
                    vehicle.setModel(vehicleDTO.getModel());
                    vehicle.setVehicleYear(vehicleDTO.getVehicleYear());
                    vehicle.setLicensePlate(vehicleDTO.getLicensePlate());
                    vehicle.setColor(vehicleDTO.getColor());
                    vehicle.setPassengerCapacity(vehicleDTO.getPassengerCapacity());
                    vehicle.setCargoCapacityKg(vehicleDTO.getCargoCapacityKg());
                    vehicle.setCargoVolumeCubicMeters(vehicleDTO.getCargoVolumeCubicMeters());
                    vehicle.setInsuranceProvider(vehicleDTO.getInsuranceProvider());
                    vehicle.setInsurancePolicyNumber(vehicleDTO.getInsurancePolicyNumber());
                    vehicle.setInsuranceExpiryDate(vehicleDTO.getInsuranceExpiryDate());
                    vehicle.setRegistrationExpiryDate(vehicleDTO.getRegistrationExpiryDate());
                    
                    if (vehicleDTO.getVehicleCondition() != null) {
                        vehicle.setVehicleCondition(Vehicle.VehicleCondition.valueOf(vehicleDTO.getVehicleCondition().toUpperCase()));
                    }
                    
                    vehicleService.saveVehicle(vehicle);
                }
            }
            
            return ResponseEntity.ok().body("KYC submitted successfully");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to submit KYC: " + e.getMessage());
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<?> getKYCStatus() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            AppUser user = appUserService.findByUsername(username).orElseThrow();

            return ResponseEntity.ok().body(user.getKycStatus());
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get KYC status: " + e.getMessage());
        }
    }
    
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingKYC() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            AppUser user = appUserService.findByUsername(username).orElseThrow();

            // Check if user is admin
            if (!"ADMIN".equals(user.getRole().name())) {
                return ResponseEntity.status(403).body("Access denied. Admin role required.");
            }
            
            // Get all users with pending KYC status
            List<AppUser> pendingUsers = appUserService.findByKycStatus(KYCStatus.SUBMITTED);
            
            return ResponseEntity.ok().body(pendingUsers);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get pending KYC: " + e.getMessage());
        }
    }
    
    @PostMapping("/{userId}/approve")
    public ResponseEntity<?> approveKYC(@PathVariable Long userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            AppUser adminUser = appUserService.findByUsername(username).orElseThrow();

            // Check if user is admin
            if (!"ADMIN".equals(adminUser.getRole().name())) {
                return ResponseEntity.status(403).body("Access denied. Admin role required.");
            }

            // Find the user to approve
            AppUser userToApprove = appUserService.findById(userId).orElseThrow();

            // Update KYC status
            userToApprove.setKycStatus(KYCStatus.VERIFIED);
            userToApprove.setKycVerifiedAt(LocalDateTime.now());
            appUserService.updateUser(userToApprove);

            return ResponseEntity.ok().body("KYC approved successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to approve KYC: " + e.getMessage());
        }
    }
    
    @PostMapping("/{userId}/reject")
    public ResponseEntity<?> rejectKYC(@PathVariable Long userId, @RequestParam String reason) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            AppUser adminUser = appUserService.findByUsername(username).orElseThrow();

            // Check if user is admin
            if (!"ADMIN".equals(adminUser.getRole().name())) {
                return ResponseEntity.status(403).body("Access denied. Admin role required.");
            }
            
            // Find the user to reject
            AppUser userToReject = appUserService.findById(userId).orElseThrow();

            // Update KYC status
            userToReject.setKycStatus(KYCStatus.REJECTED);
            userToReject.setKycRejectionReason(reason);
            appUserService.updateUser(userToReject);
            
            return ResponseEntity.ok().body("KYC rejected successfully");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to reject KYC: " + e.getMessage());
        }
    }
    
    @GetMapping("/{userId}/status")
    public ResponseEntity<?> getKYCStatusByUserId(@PathVariable Long userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            AppUser currentUser = appUserService.findByUsername(username).orElseThrow();

            // Check if user is admin or requesting their own status
            if (!"ADMIN".equals(currentUser.getRole().name()) && !currentUser.getId().equals(userId)) {
                return ResponseEntity.status(403).body("Access denied. Can only view own KYC status.");
            }
            
            // Find the user whose status is requested
            AppUser user = appUserService.findById(userId).orElseThrow();

            return ResponseEntity.ok().body(user.getKycStatus());
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get KYC status: " + e.getMessage());
        }
    }
} 