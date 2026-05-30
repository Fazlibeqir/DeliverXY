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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Long deliveryId = Objects.requireNonNull(dto.getDeliveryId(), "deliveryId");
        Long reviewerUserId = Objects.requireNonNull(reviewerId, "reviewerId");

        if (repo.existsByDeliveryIdAndReviewerId(deliveryId, reviewerUserId)) {
            throw new BadRequestException("You already rated this delivery");
        }

        Delivery delivery = deliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery not found"));
        AppUser reviewer = userRepo.findById(reviewerUserId)
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        if (delivery.getStatus() != DeliveryStatus.DELIVERED)
            throw new BadRequestException("You can rate only delivered deliveries");

        AppUser targetUser;
        if (Objects.equals(delivery.getClient().getId(), reviewerUserId)){
            if (delivery.getAgent() == null) throw new BadRequestException("No agent assigned to this delivery");
            targetUser = delivery.getAgent();
        } else if(delivery.getAgent() != null && Objects.equals(delivery.getAgent().getId(), reviewerUserId)){
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
        Long id = Objects.requireNonNull(ratingId, "ratingId");
        Rating r = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Rating not found"));

        if (!r.getReviewer().getId().equals(reviewerId)) {
            throw new BadRequestException("You can update only your rating");
        }

        r.setRating(dto.getRating());
        r.setReview(dto.getReview());

        return repo.save(r);
    }

    @Override
    @Transactional
    public void deleteRating(Long reviewerId, Long ratingId) {
        Long id = Objects.requireNonNull(ratingId, "ratingId");
        Rating r = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Rating not found"));

        if (!r.getReviewer().getId().equals(reviewerId)) {
            throw new BadRequestException("You can delete only your own rating");
        }

        repo.delete(r);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rating> getRatingsByDelivery(Long deliveryId) {
        return repo.findByDeliveryId(deliveryId, PageRequest.of(0, 50)).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rating> getRatingsByUser(Long userId) {
        return repo.findByTargetUserId(userId, PageRequest.of(0, 50)).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long userId) {
        Double avg = repo.getAverageRatingForUser(userId);
        return avg != null ? avg : 0.0;
    }
}
