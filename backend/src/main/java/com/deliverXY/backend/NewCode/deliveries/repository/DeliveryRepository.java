package com.deliverXY.backend.NewCode.deliveries.repository;

import com.deliverXY.backend.NewCode.common.enums.DeliveryStatus;
import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery,Long> {
    //BASIC FILTERS
    List<Delivery> findByStatus(DeliveryStatus status);
    List<Delivery> findByAgentId(Long agentId);
    List<Delivery> findByClientId(Long clientId);

    long countByStatus(DeliveryStatus status);
    long countByAgentId(Long agentId);
    long countByClientId(Long clientId);
    Page<Delivery> findByClientIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Delivery> findByAgentIdOrderByCreatedAtDesc(Long driverId, Pageable pageable);
    List<Delivery> findByAgentIdAndCreatedAtBetween(Long agent_id, LocalDateTime createdAt, LocalDateTime createdAt2);
    List<Delivery> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    // PROXIMITY SEARCH
    @Query(value = """
            SELECT * FROM deliveries d 
            WHERE d.status IN ('REQUESTED')
            AND d.agent_id IS NULL
            AND (
              6371 * acos(
                cos(radians(:lat)) * 
                cos(radians(d.pickup_latitude)) *
                cos(radians(d.pickup_longitude) - radians(:lng)) +
                sin(radians(:lat)) * 
                sin(radians(d.pickup_latitude))
                )
            ) <= :radius
            """,
            nativeQuery = true)
    List<Delivery> findNearbyDeliveries(@Param("lat") Double latitude,
                                        @Param("lng") Double longitude,
                                        @Param("radius") Double radiusKm);

    Optional<Delivery> findFirstByAgentIdAndStatusInOrderByAssignedAtDesc(
            Long agentId,
            List<DeliveryStatus> statuses
    );

}