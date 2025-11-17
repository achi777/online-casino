package com.casino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_bonuses")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserBonus extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bonus_id", nullable = false)
    private Bonus bonus;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal bonusAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal wageringRequired;

    @Column(precision = 19, scale = 2)
    private BigDecimal wageringCompleted = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserBonusStatus status = UserBonusStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime claimedAt;

    private LocalDateTime expiresAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    public enum UserBonusStatus {
        ACTIVE,      // Bonus is active and can be used
        WAGERING,    // User is completing wagering requirements
        COMPLETED,   // Wagering completed, bonus converted to cash
        EXPIRED,     // Bonus expired before completion
        CANCELLED,   // Bonus cancelled by user or admin
        FORFEITED    // Bonus forfeited due to violation
    }

    public BigDecimal getRemainingWagering() {
        if (wageringRequired == null) {
            return BigDecimal.ZERO;
        }
        return wageringRequired.subtract(wageringCompleted).max(BigDecimal.ZERO);
    }

    public boolean isWageringComplete() {
        if (wageringRequired == null || wageringRequired.equals(BigDecimal.ZERO)) {
            return true;
        }
        return wageringCompleted.compareTo(wageringRequired) >= 0;
    }
}
