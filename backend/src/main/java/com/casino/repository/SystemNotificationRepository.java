package com.casino.repository;

import com.casino.entity.SystemNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {
    List<SystemNotification> findByStatus(SystemNotification.NotificationStatus status);
    List<SystemNotification> findByStatusAndValidFromBeforeAndValidToAfter(
            SystemNotification.NotificationStatus status,
            LocalDateTime now1,
            LocalDateTime now2
    );
}
