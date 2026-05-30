package com.deliverXY.backend.NewCode.notifications.repository;

import com.deliverXY.backend.NewCode.notifications.domain.Notification;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserOrderByCreatedAtDesc(AppUser user, Pageable pageable);
}
