package com.deliverXY.backend.NewCode.rating.repository;

import com.deliverXY.backend.NewCode.rating.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByDeliveryId(Long deliveryId);

    List<Rating> findByTargetUserId(Long userId);

    boolean existsByDeliveryIdAndReviewerId(Long deliveryId, Long reviewerId);
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.targetUser.id = :userId")
    Double getAverageRatingForUser(Long userId);
}
