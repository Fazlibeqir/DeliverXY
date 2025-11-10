package com.deliverXY.backend.repositories;

import com.deliverXY.backend.models.Rating;
import com.deliverXY.backend.models.Delivery;
import com.deliverXY.backend.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    List<Rating> findByDelivery(Delivery delivery);
    List<Rating> findByReviewer(AppUser reviewer);
    List<Rating> findByReviewed(AppUser reviewed);
    List<Rating> findByDeliveryAndReviewer(Delivery delivery, AppUser reviewer);
    List<Rating> findByRatingType(Rating.RatingType ratingType);
} 