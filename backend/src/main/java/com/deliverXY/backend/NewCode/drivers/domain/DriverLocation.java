package com.deliverXY.backend.NewCode.drivers.domain;

import com.deliverXY.backend.NewCode.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "driver_locations")
@Data
@NoArgsConstructor
public class DriverLocation {

    @Id
    private Long driverId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "driver_id")
    private AppUser driver;

    private Double latitude;
    private Double longitude;

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
