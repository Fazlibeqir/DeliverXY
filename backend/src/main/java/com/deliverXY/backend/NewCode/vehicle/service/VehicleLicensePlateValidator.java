package com.deliverXY.backend.NewCode.vehicle.service;

import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.vehicle.domain.Vehicle;
import com.deliverXY.backend.NewCode.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VehicleLicensePlateValidator {

    private final VehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public void assertUnique(Long currentVehicleId, String licensePlate) {
        if (!vehicleRepository.existsByLicensePlate(licensePlate)) {
            return;
        }
        Vehicle existing = vehicleRepository.findByLicensePlate(licensePlate).orElse(null);
        if (existing != null && !Objects.equals(existing.getId(), currentVehicleId)) {
            throw new BadRequestException("License plate already registered for another vehicle");
        }
    }
}
