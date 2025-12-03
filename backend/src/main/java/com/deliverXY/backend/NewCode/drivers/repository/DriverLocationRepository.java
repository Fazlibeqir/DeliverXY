package com.deliverXY.backend.NewCode.drivers.repository;

import com.deliverXY.backend.NewCode.drivers.domain.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {

    @Query(value = """
            SELECT * FROM driver_locations d 
            WHERE (
              6371 * acos(
                cos(radians(:lat)) *
                cos(radians(d.latitude)) *
                cos(radians(d.longitude) - radians(:lon)) +
                sin(radians(:lat)) *
                sin(radians(d.latitude))
              )
            ) <= :radius
            ORDER BY 
              6371 * acos(
                cos(radians(:lat)) *
                cos(radians(d.latitude)) *
                cos(radians(d.longitude) - radians(:lon)) +
                sin(radians(:lat)) *
                sin(radians(d.latitude))
              )
            ASC
            """,
            nativeQuery = true)
    List<DriverLocation> findNearbyDrivers(
            @Param("lat") Double lat,
            @Param("lon") Double lon,
            @Param("radius") Double radiusKm
    );
}
