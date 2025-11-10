package com.deliverXY.backend.repositories;

import com.deliverXY.backend.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.AppUser;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery,Long> {

    List<Delivery> findByStatus(DeliveryStatus status);
    List<Delivery> findByClient(AppUser client);
    List<Delivery> findByClientId(Long clientId);
    Page<Delivery> findByClientIdOrderByCreatedAtDesc(Long clientId, Pageable pageable);
    Long countByClientId(Long clientId);
    List<Delivery> findByAgent(AppUser agent);
    List<Delivery> findByAgentId(Long agentId);
    Page<Delivery> findByAgentIdOrderByCreatedAtDesc(Long agentId, Pageable pageable);
    // Find by date range
    List<Delivery> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Delivery> findByAgentIdAndCreatedAtBetween(Long agentId, LocalDateTime startDate, LocalDateTime endDate);

    List<Delivery> findByClientIdAndCreatedAtBetween(Long clientId, LocalDateTime startDate, LocalDateTime endDate);

    // Find nearby deliveries
    @Query(value = "SELECT * FROM deliveries d " +
            "WHERE d.status IN ('PENDING', 'ASSIGNED') " +
            "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(d.pickup_latitude)) * " +
            "cos(radians(d.pickup_longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(d.pickup_latitude)))) <= :radiusKm",
            nativeQuery = true)
    List<Delivery> findNearbyDeliveries(@Param("latitude") Double latitude,
                                        @Param("longitude") Double longitude,
                                        @Param("radiusKm") Double radiusKm);
    // Find by tracking code
    Delivery findByTrackingCode(String trackingCode);
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.client.id = :clientId AND d.status = :status")
    Long countByClientIdAndStatus(@Param("clientId") Long clientId,
                                  @Param("status") DeliveryStatus status);

    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.agent.id = :agentId AND d.status = :status")
    Long countByAgentIdAndStatus(@Param("agentId") Long agentId,
                                 @Param("status") DeliveryStatus status);

    List<Delivery> findByStatusAndAgentIsNotNull(DeliveryStatus status);
    List<Delivery> findByStatusAndAgentIsNull(DeliveryStatus status);


}