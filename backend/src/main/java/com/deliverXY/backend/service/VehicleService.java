package com.deliverXY.backend.service;

import com.deliverXY.backend.models.Vehicle;
import java.util.List;

public interface VehicleService {
    void saveVehicle(Vehicle vehicle);
    Vehicle findById(Long id);
    List<Vehicle> findByOwnerId(Long ownerId);
    Vehicle updateVehicle(Vehicle vehicle);
    void deleteVehicle(Long id);
} 