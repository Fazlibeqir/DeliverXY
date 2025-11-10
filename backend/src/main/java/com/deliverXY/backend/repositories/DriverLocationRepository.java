package com.deliverXY.backend.repositories;

import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {

    Optional<DriverLocation> findByDriver(AppUser driver);

    Optional<DriverLocation> findByDriverId(Long driverId);

    List<DriverLocation> findByIsAvailableTrue();

    // Find nearby available drivers using Haversine formula
    @Query(value = "SELECT * FROM driver_locations dl " +
            "WHERE dl.is_available = true " +
            "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(dl.latitude)) * " +
            "cos(radians(dl.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(dl.latitude)))) <= :radiusKm " +
            "ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(dl.latitude)) * " +
            "cos(radians(dl.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(dl.latitude))))",
            nativeQuery = true)
    List<DriverLocation> findNearbyDrivers(@Param("latitude") Double latitude,
                                           @Param("longitude") Double longitude,
                                           @Param("radiusKm") Double radiusKm);
}