package com.deliverXY.backend.controllers;

import com.deliverXY.backend.models.Rating;
import com.deliverXY.backend.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@CrossOrigin(origins = "*")
public class RatingController {
    
    private final RatingService ratingService;
    
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }
    
    @PostMapping
    public ResponseEntity<Rating> createRating(@Valid @RequestBody Rating rating) {
        try {
            Rating createdRating = ratingService.createRating(rating);
            return ResponseEntity.ok(createdRating);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/delivery/{deliveryId}")
    public ResponseEntity<List<Rating>> getRatingsByDelivery(@PathVariable Long deliveryId) {
        try {
            List<Rating> ratings = ratingService.getRatingsByDelivery(deliveryId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Rating>> getRatingsByUser(@PathVariable Long userId) {
        try {
            List<Rating> ratings = ratingService.getRatingsByUser(userId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/average/{userId}")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long userId) {
        try {
            Double averageRating = ratingService.getAverageRating(userId);
            return ResponseEntity.ok(averageRating);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Rating> updateRating(@PathVariable Long id, @Valid @RequestBody Rating rating) {
        try {
            Rating updatedRating = ratingService.updateRating(id, rating);
            return ResponseEntity.ok(updatedRating);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRating(@PathVariable Long id) {
        try {
            ratingService.deleteRating(id);
            return ResponseEntity.ok("Rating deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete rating: " + e.getMessage());
        }
    }
} 