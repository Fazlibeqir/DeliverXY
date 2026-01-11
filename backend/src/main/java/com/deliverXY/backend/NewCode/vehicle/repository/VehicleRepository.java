package com.deliverXY.backend.NewCode.vehicle.repository;

import com.deliverXY.backend.NewCode.vehicle.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByOwnerId(Long ownerId);

    boolean existsByLicensePlate(String plate);

    Optional<Vehicle> findByLicensePlate(String plate);
}
