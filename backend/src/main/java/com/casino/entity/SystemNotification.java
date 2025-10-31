package com.casino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_notifications")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SystemNotification extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime validFrom = LocalDateTime.now();

    private LocalDateTime validTo;

    private Boolean showToAllUsers = true;

    public enum NotificationType {
        INFO,
        WARNING,
        ERROR,
        SUCCESS,
        MAINTENANCE,
        PROMOTION
    }

    public enum NotificationStatus {
        ACTIVE,
        INACTIVE,
        EXPIRED
    }
}
