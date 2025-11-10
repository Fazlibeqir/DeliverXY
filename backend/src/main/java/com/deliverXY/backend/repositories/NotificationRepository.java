package com.deliverXY.backend.repositories;

import com.deliverXY.backend.models.AppUser;
import com.deliverXY.backend.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(AppUser user);

    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(AppUser user);

    Long countByUserAndIsReadFalse(AppUser user);
}