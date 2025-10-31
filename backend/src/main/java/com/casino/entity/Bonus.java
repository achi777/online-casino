package com.casino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bonuses")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Bonus extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BonusType type;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(precision = 19, scale = 2)
    private BigDecimal minDeposit;

    @Column(precision = 19, scale = 2)
    private BigDecimal maxBonus;

    private Integer wageringRequirement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BonusStatus status = BonusStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validTo;

    private Integer usageLimit;

    private Integer usedCount = 0;

    private String termsAndConditions;

    public enum BonusType {
        WELCOME,
        DEPOSIT,
        NO_DEPOSIT,
        FREE_SPINS,
        CASHBACK,
        RELOAD,
        REFERRAL,
        VIP
    }

    public enum BonusStatus {
        ACTIVE,
        INACTIVE,
        EXPIRED,
        PAUSED
    }
}
