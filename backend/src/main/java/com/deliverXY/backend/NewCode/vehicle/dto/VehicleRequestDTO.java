package com.deliverXY.backend.NewCode.vehicle.dto;

import com.deliverXY.backend.NewCode.vehicle.domain.Vehicle.VehicleType;
import com.deliverXY.backend.NewCode.vehicle.domain.Vehicle.VehicleCondition;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class VehicleRequestDTO {

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotBlank(message = "Make is required")
    @Length(max = 50)
    private String make;

    @NotBlank(message = "Model is required")
    @Length(max = 50)
    private String model;

    @Min(value = 1900, message = "Year must be 1900 or later")
    @Max(value = 2099, message = "Year must be a reasonable future year")
    private Integer vehicleYear;

    @NotBlank(message = "License plate is required")
    @Pattern(
            regexp = "^[A-Z]{2}[ ]?[0-9]{3}[ ]?[A-Z]{2}$", // Regex for XX 000 XX format
            message = "License plate must be in the format XX 000 XX"
    )
    @Length(min = 3, max = 20, message = "License plate length must be between {min} and {max}")
    private String licensePlate;
    private String color;

    @Min(value = 1, message = "Passenger capacity must be at least 1")
    private Integer passengerCapacity;
    @Min(value = 0, message = "Cargo capacity must be non-negative")
    private Double cargoCapacityKg;
    @Min(value = 0, message = "Cargo volume must be non-negative")
    private Double cargoVolumeCubicMeters;

    @NotNull(message = "Vehicle condition is required")
    private VehicleCondition vehicleCondition;


    private String insuranceProvider;
    private String insurancePolicyNumber;

    @FutureOrPresent(message = "Insurance expiry date must be present or in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime insuranceExpiryDate;
    @FutureOrPresent(message = "Registration expiry date must be present or in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime registrationExpiryDate;
}
