package com.casino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KYCStatus kycStatus = KYCStatus.PENDING;

    private LocalDateTime lastLoginAt;

    // Responsible Gaming fields
    @Column(precision = 19, scale = 2)
    private BigDecimal dailyDepositLimit;

    @Column(precision = 19, scale = 2)
    private BigDecimal weeklyDepositLimit;

    @Column(precision = 19, scale = 2)
    private BigDecimal monthlyDepositLimit;

    private Integer dailyTimeLimit; // in minutes

    private LocalDateTime selfExclusionUntil;

    private Boolean temporaryBlock = false;

    // VIP & Loyalty Program fields
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vip_tier_id")
    private VIPTier vipTier;

    @Column(nullable = false)
    private Integer vipPoints = 0;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalWagered = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal lifetimeDeposits = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal lifetimeWithdrawals = BigDecimal.ZERO;

    public enum UserStatus {
        ACTIVE, SUSPENDED, BLOCKED, CLOSED
    }

    public enum KYCStatus {
        PENDING, VERIFIED, REJECTED
    }
}
