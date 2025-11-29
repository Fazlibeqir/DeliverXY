package com.deliverXY.backend.NewCode.rating.service;

import com.deliverXY.backend.NewCode.rating.domain.Rating;
import com.deliverXY.backend.NewCode.rating.dto.RatingRequestDTO;

import java.util.List;

public interface RatingService {

    Rating createRating(Long reviewerId, RatingRequestDTO dto);

    Rating updateRating(Long reviewerId, Long ratingId, RatingRequestDTO dto);

    void deleteRating(Long reviewerId, Long ratingId);

    List<Rating> getRatingsByDelivery(Long deliveryId);

    List<Rating> getRatingsByUser(Long userId);

    Double getAverageRating(Long targetUserId);
}
