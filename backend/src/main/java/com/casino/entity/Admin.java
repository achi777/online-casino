package com.casino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admins")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminStatus status = AdminStatus.ACTIVE;

    private LocalDateTime lastLoginAt;

    public enum AdminRole {
        OWNER,          // სრული წვდომა ყველაფერზე
        ADMIN,          // თითქმის სრული წვდომა (გარდა სისტემის პარამეტრები)
        FINANCE,        // ფინანსური ოპერაციები, ტრანზაქციები
        SUPPORT,        // მომხმარებლების მხარდაჭერა, KYC
        CONTENT,        // თამაშები, პროვაიდერები, კონტენტი
        ANALYST,        // რეპორტები, სტატისტიკა (read-only)
        COMPLIANCE      // რეგულაციები, KYC, AML
    }

    public enum AdminStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}
