package com.deliverXY.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
@NoArgsConstructor
@Data
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Delivery delivery;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    @JsonIgnoreProperties({"vehicles", "hibernateLazyInitializer", "handler"})
    private AppUser reviewer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_id", nullable = false)
    @JsonIgnoreProperties({"vehicles", "hibernateLazyInitializer", "handler"})
    private AppUser reviewed;
    
    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5 stars
    
    @Column(name = "review", columnDefinition = "TEXT")
    private String review;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "rating_type", nullable = false)
    private RatingType ratingType;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum RatingType {
        CLIENT_TO_AGENT, AGENT_TO_CLIENT
    }
    
    public Rating(Delivery delivery, AppUser reviewer, AppUser reviewed, 
                  Integer rating, String review, RatingType ratingType) {
        this.delivery = delivery;
        this.reviewer = reviewer;
        this.reviewed = reviewed;
        this.rating = rating;
        this.review = review;
        this.ratingType = ratingType;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 