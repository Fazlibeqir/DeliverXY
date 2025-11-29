package com.deliverXY.backend.NewCode.rating.domain;

import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ratings",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"delivery_id", "reviewer_id"},
                        name = "unique_reviewer_delivery"
                )
        }
)
@Data
@NoArgsConstructor
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** --------------------------------------------------
     * Relations
     * -------------------------------------------------- */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private AppUser reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private AppUser targetUser;   // the person being rated (most often the driver)

    /** --------------------------------------------------
     * Rating info
     * -------------------------------------------------- */

    @Column(nullable = false)
    private int rating; // 1–5 stars

    @Column(columnDefinition = "TEXT")
    private String review;

    /** --------------------------------------------------
     * Timestamps
     * -------------------------------------------------- */
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
