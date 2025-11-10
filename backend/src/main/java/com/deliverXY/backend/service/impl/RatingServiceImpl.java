package com.deliverXY.backend.service.impl;

import com.deliverXY.backend.models.Rating;
import com.deliverXY.backend.repositories.RatingRepository;
import com.deliverXY.backend.service.RatingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.OptionalDouble;

@Service
public class RatingServiceImpl implements RatingService {
    
    private final RatingRepository ratingRepository;
    
    public RatingServiceImpl(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }
    
    @Override
    @Transactional
    public Rating createRating(Rating rating) {
        // Validate that only one rating per delivery per user pair
        List<Rating> existingRatings = ratingRepository.findByDeliveryAndReviewer(
            rating.getDelivery(), rating.getReviewer());
        
        if (!existingRatings.isEmpty()) {
            throw new RuntimeException("Rating already exists for this delivery by this user");
        }
        
        return ratingRepository.save(rating);
    }
    
    @Override
    public List<Rating> getRatingsByDelivery(Long deliveryId) {
        // This would need a custom query method in the repository
        // For now, return empty list
        return List.of();
    }
    
    @Override
    public List<Rating> getRatingsByUser(Long userId) {
        // This would need a custom query method in the repository
        // For now, return empty list
        return List.of();
    }
    
    @Override
    public Double getAverageRating(Long userId) {
        // This would need a custom query method in the repository
        // For now, return 0.0
        return 0.0;
    }
    
    @Override
    @Transactional
    public Rating updateRating(Long id, Rating rating) {
        Rating existingRating = ratingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Rating not found"));
        
        existingRating.setRating(rating.getRating());
        existingRating.setReview(rating.getReview());
        
        return ratingRepository.save(existingRating);
    }
    
    @Override
    @Transactional
    public void deleteRating(Long id) {
        if (!ratingRepository.existsById(id)) {
            throw new RuntimeException("Rating not found");
        }
        ratingRepository.deleteById(id);
    }
} 