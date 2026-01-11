package com.deliverXY.backend.NewCode.notifications.repository;

import com.deliverXY.backend.NewCode.notifications.domain.Notification;
import com.deliverXY.backend.NewCode.user.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(AppUser user);

}