package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.models.Vehicle;
import com.deliverXY.backend.repositories.VehicleRepository;
import com.deliverXY.backend.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {
    
    private final VehicleRepository vehicleRepository;
    
    @Autowired
    public VehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }
    
    @Override
    public void saveVehicle(Vehicle vehicle) {
        vehicleRepository.save(vehicle);
    }
    
    @Override
    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<Vehicle> findByOwnerId(Long ownerId) {
        return vehicleRepository.findByOwnerId(ownerId);
    }
    
    @Override
    public Vehicle updateVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }
    
    @Override
    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }
} 