package com.casino.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "vip_points_transactions")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class VIPPointsTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private Integer points;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 19, scale = 2)
    private BigDecimal relatedAmount; // თანხა რომელზეც დაირიცხა ქულები

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id")
    @JsonIgnore
    private GameSession gameSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    @JsonIgnore
    private Transaction transaction;

    public enum TransactionType {
        WAGERING,           // თამაშზე დარიცხული
        DEPOSIT,            // დეპოზიტზე დარიცხული
        BONUS,              // ბონუსად მიღებული
        LEVEL_UP,           // ლეველ-აპ ბონუსი
        MANUAL_ADJUSTMENT,  // ადმინის მიერ დამატებული/მოხსნილი
        PROMO,              // პრომოციის ქულები
        REFERRAL,           // რეფერალური ქულები
        EXPIRED             // ვადაგასული ქულები
    }
}
