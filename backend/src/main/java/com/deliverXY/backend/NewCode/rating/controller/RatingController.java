package com.deliverXY.backend.NewCode.rating.controller;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.rating.domain.Rating;
import com.deliverXY.backend.NewCode.rating.dto.RatingRequestDTO;
import com.deliverXY.backend.NewCode.rating.service.RatingService;
import com.deliverXY.backend.NewCode.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ApiResponse<Rating> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody RatingRequestDTO dto
    ) {
        return ApiResponse.ok(
                ratingService.createRating(principal.getUser().getId(), dto)
        );
    }

    @GetMapping("/delivery/{deliveryId}")
    public ApiResponse<List<Rating>> getByDelivery(@PathVariable Long deliveryId) {
        return ApiResponse.ok(ratingService.getRatingsByDelivery(deliveryId));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<Rating>> getByUser(@PathVariable Long userId) {
        return ApiResponse.ok(ratingService.getRatingsByUser(userId));
    }

    @GetMapping("/user/{userId}/average")
    public ApiResponse<Double> average(@PathVariable Long userId) {
        return ApiResponse.ok(ratingService.getAverageRating(userId));
    }

    @PutMapping("/{id}")
    public ApiResponse<Rating> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody RatingRequestDTO dto
    ) {
        return ApiResponse.ok(
                ratingService.updateRating(principal.getUser().getId(), id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        ratingService.deleteRating(principal.getUser().getId(), id);
        return ApiResponse.ok("Rating deleted");
    }
}
