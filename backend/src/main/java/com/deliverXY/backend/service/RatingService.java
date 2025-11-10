package com.deliverXY.backend.service;

import com.deliverXY.backend.models.Rating;
import java.util.List;

public interface RatingService {
    Rating createRating(Rating rating);
    List<Rating> getRatingsByDelivery(Long deliveryId);
    List<Rating> getRatingsByUser(Long userId);
    Double getAverageRating(Long userId);
    Rating updateRating(Long id, Rating rating);
    void deleteRating(Long id);
} 