package com.deliverXY.backend.NewCode.rating.service.impl;

import com.deliverXY.backend.NewCode.common.enums.DeliveryStatus;
import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.rating.domain.Rating;
import com.deliverXY.backend.NewCode.rating.dto.RatingRequestDTO;
import com.deliverXY.backend.NewCode.rating.repository.RatingRepository;
import com.deliverXY.backend.NewCode.rating.service.RatingService;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import com.deliverXY.backend.NewCode.user.repository.AppUserRepository;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository repo;
    private final DeliveryRepository deliveryRepo;
    private final AppUserRepository userRepo;

    @Override
    @Transactional
    public Rating createRating(Long reviewerId, RatingRequestDTO dto) {

        if (repo.existsByDeliveryIdAndReviewerId(dto.getDeliveryId(), reviewerId))
            throw new RuntimeException("You already rated this delivery");

        Delivery delivery = deliveryRepo.findById(dto.getDeliveryId())
                .orElseThrow(() -> new NotFoundException("Delivery not found"));
        AppUser reviewer = userRepo.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        if (delivery.getStatus() != DeliveryStatus.DELIVERED)
            throw new BadRequestException("You can rate only delivered deliveries");

        AppUser targetUser;
        if (Objects.equals(delivery.getClient().getId(), reviewerId)){
            if (delivery.getAgent() == null) throw new BadRequestException("No agent assigned to this delivery");
            targetUser = delivery.getAgent();
        } else if(delivery.getAgent() != null && Objects.equals(delivery.getAgent().getId(), reviewerId)){
            targetUser = delivery.getClient();
        }else{
            throw new BadRequestException("You are not part of this delivery transaction.");
        }

        Rating r = new Rating();
        r.setDelivery(delivery);
        r.setReviewer(reviewer);
        r.setTargetUser(targetUser);
        r.setRating(dto.getRating());
        r.setReview(dto.getReview());

        return repo.save(r);
    }

    @Override
    @Transactional
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
            throw new RuntimeException("You can delete only your own rating");

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
        Double avg = repo.getAverageRatingForUser(userId);
        return avg != null ? avg : 0.0;
    }
}
