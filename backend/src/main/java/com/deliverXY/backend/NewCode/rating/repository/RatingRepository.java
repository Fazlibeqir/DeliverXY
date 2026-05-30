package com.deliverXY.backend.NewCode.rating.repository;

import com.deliverXY.backend.NewCode.rating.domain.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Page<Rating> findByDeliveryId(Long deliveryId, Pageable pageable);

    Page<Rating> findByTargetUserId(Long userId, Pageable pageable);

    boolean existsByDeliveryIdAndReviewerId(Long deliveryId, Long reviewerId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.targetUser.id = :userId")
    Double getAverageRatingForUser(Long userId);
}
