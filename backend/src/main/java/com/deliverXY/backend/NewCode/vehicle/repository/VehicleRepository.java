package com.deliverXY.backend.NewCode.vehicle.repository;

import com.deliverXY.backend.NewCode.vehicle.domain.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Page<Vehicle> findByOwnerId(Long ownerId, Pageable pageable);

    boolean existsByLicensePlate(String plate);

    Optional<Vehicle> findByLicensePlate(String plate);
}
