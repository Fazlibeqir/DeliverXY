package com.deliverXY.backend.NewCode.notifications.domain;

import com.deliverXY.backend.NewCode.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_device_tokens",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "deviceToken", "platform"})
        }
)
@Data
@NoArgsConstructor
public class UserDeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false)
    private String deviceToken;

    private String platform; // ANDROID / IOS / WEB

    @Column(nullable = false)
    private boolean active = true;
}
