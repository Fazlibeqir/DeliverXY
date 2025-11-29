package com.deliverXY.backend.NewCode.rating.service.impl;

import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.rating.domain.Rating;
import com.deliverXY.backend.NewCode.rating.dto.RatingRequestDTO;
import com.deliverXY.backend.NewCode.rating.repository.RatingRepository;
import com.deliverXY.backend.NewCode.rating.service.RatingService;
import com.deliverXY.backend.NewCode.user.repository.AppUserRepository;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository repo;
    private final DeliveryRepository deliveryRepo;
    private final AppUserRepository userRepo;

    @Override
    public Rating createRating(Long reviewerId, RatingRequestDTO dto) {

        if (repo.existsByDeliveryIdAndReviewerId(dto.getDeliveryId(), reviewerId))
            throw new RuntimeException("You already rated this delivery");

        Rating r = new Rating();
        r.setDelivery(deliveryRepo.findById(dto.getDeliveryId())
                .orElseThrow(() -> new NotFoundException("Delivery not found")));

        r.setReviewer(userRepo.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("User not found")));

        r.setTargetUser(r.getDelivery().getAgent()); // the agent being rated

        r.setRating(dto.getRating());
        r.setReview(dto.getReview());

        return repo.save(r);
    }

    @Override
    public Rating updateRating(Long reviewerId, Long ratingId, RatingRequestDTO dto) {
        Rating r = repo.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating not found"));

        if (!r.getReviewer().getId().equals(reviewerId))
            throw new RuntimeException("You can update only your rating");

        r.setRating(dto.getRating());
        r.setReview(dto.getReview());

        return repo.save(r);
    }

    @Override
    public void deleteRating(Long reviewerId, Long ratingId) {
        Rating r = repo.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating not found"));

        if (!r.getReviewer().getId().equals(reviewerId))
            throw new RuntimeException("You can delete only your rating");

        repo.delete(r);
    }

    @Override
    public List<Rating> getRatingsByDelivery(Long deliveryId) {
        return repo.findByDeliveryId(deliveryId);
    }

    @Override
    public List<Rating> getRatingsByUser(Long userId) {
        return repo.findByTargetUserId(userId);
    }

    @Override
    public Double getAverageRating(Long userId) {
        return repo.findByTargetUserId(userId)
                .stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0);
    }
}
