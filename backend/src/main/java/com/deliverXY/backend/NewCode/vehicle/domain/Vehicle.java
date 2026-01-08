package com.deliverXY.backend.NewCode.vehicle.domain;

import com.deliverXY.backend.NewCode.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({"vehicles", "hibernateLazyInitializer", "handler"})
    private AppUser owner;
    
    @Column(name = "vehicle_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;
    
    @Column(name = "make", nullable = false)
    private String make;
    
    @Column(name = "model", nullable = false)
    private String model;
    
    @Column(name = "vehicle_year")
    private Integer vehicleYear;
    
    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;
    
    @Column(name = "color")
    private String color;
    
    @Column(name = "passenger_capacity")
    private Integer passengerCapacity;
    
    @Column(name = "cargo_capacity_kg")
    private Double cargoCapacityKg;
    
    @Column(name = "cargo_volume_cubic_meters")
    private Double cargoVolumeCubicMeters;
    
    @Column(name = "is_available")
    private Boolean isAvailable = true;
    
    @Column(name = "insurance_provider")
    private String insuranceProvider;
    
    @Column(name = "insurance_policy_number")
    private String insurancePolicyNumber;
    
    @Column(name = "insurance_expiry_date")
    private LocalDateTime insuranceExpiryDate;
    
    @Column(name = "registration_expiry_date")
    private LocalDateTime registrationExpiryDate;
    
    @Column(name = "vehicle_condition")
    @Enumerated(EnumType.STRING)
    private VehicleCondition vehicleCondition = VehicleCondition.GOOD;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Column(name = "image_url")
    private String imageUrl;

    public enum VehicleType {
        CAR, MOTORCYCLE, VAN, TRUCK, BICYCLE, SCOOTER, OTHER
    }
    
    public enum VehicleCondition {
        EXCELLENT, GOOD, FAIR, POOR, MAINTENANCE_NEEDED
    }
    
    public Vehicle(AppUser owner, VehicleType vehicleType, String make, String model, String licensePlate) {
        this.owner = owner;
        this.vehicleType = vehicleType;
        this.make = make;
        this.model = model;
        this.licensePlate = licensePlate;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 