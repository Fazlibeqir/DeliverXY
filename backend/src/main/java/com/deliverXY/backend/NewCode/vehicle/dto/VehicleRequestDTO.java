package com.deliverXY.backend.NewCode.vehicle.dto;

import com.deliverXY.backend.NewCode.vehicle.domain.Vehicle.VehicleType;
import com.deliverXY.backend.NewCode.vehicle.domain.Vehicle.VehicleCondition;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VehicleRequestDTO {

    private VehicleType vehicleType;

    private String make;
    private String model;
    private Integer vehicleYear;

    private String licensePlate;
    private String color;

    private Integer passengerCapacity;
    private Double cargoCapacityKg;
    private Double cargoVolumeCubicMeters;

    private VehicleCondition vehicleCondition;

    private String insuranceProvider;
    private String insurancePolicyNumber;
    private LocalDateTime insuranceExpiryDate;
    private LocalDateTime registrationExpiryDate;
}
