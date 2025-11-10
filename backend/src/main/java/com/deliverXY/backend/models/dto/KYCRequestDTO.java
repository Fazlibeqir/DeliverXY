package com.deliverXY.backend.models.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class KYCRequestDTO {
    // Personal Information
    private String firstName;
    private String lastName;
    private String phoneNumber;
    
    // Identity Documents
    private String idFrontUrl;
    private String idBackUrl;
    private String selfieUrl;
    private String proofOfAddressUrl;
    
    // Driver's License Information
    private String driversLicenseNumber;
    private LocalDateTime driversLicenseExpiry;
    private String driversLicenseFrontUrl;
    private String driversLicenseBackUrl;
    
    // Vehicle Information
    private List<VehicleDTO> vehicles;
    
    @Data
    public static class VehicleDTO {
        private String vehicleType;
        private String make;
        private String model;
        private Integer vehicleYear;
        private String licensePlate;
        private String color;
        private Integer passengerCapacity;
        private Double cargoCapacityKg;
        private Double cargoVolumeCubicMeters;
        private String insuranceProvider;
        private String insurancePolicyNumber;
        private LocalDateTime insuranceExpiryDate;
        private LocalDateTime registrationExpiryDate;
        private String vehicleCondition;
    }
} 