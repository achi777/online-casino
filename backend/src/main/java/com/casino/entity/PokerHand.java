package com.casino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "poker_hands")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PokerHand extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "table_id", nullable = false)
    private PokerTable table;

    @Column(nullable = false, unique = true)
    private String handId;

    @Column(nullable = false)
    private Integer dealerPosition;

    @Column(nullable = false)
    private Integer smallBlindPosition;

    @Column(nullable = false)
    private Integer bigBlindPosition;

    @Column(columnDefinition = "TEXT")
    private String deck; // JSON array of remaining cards

    @Column(columnDefinition = "TEXT")
    private String communityCards; // JSON array of flop/turn/river

    @Column(columnDefinition = "TEXT")
    private String playerHands; // JSON object of player hole cards

    @Column(columnDefinition = "TEXT")
    private String activePlayers; // JSON array of active player IDs

    @Column(precision = 19, scale = 2)
    private BigDecimal potAmount = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal currentBet = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HandStage stage = HandStage.PRE_FLOP;

    @Column
    private Integer currentPlayerPosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HandStatus status = HandStatus.IN_PROGRESS;

    @Column(columnDefinition = "TEXT")
    private String winners; // JSON array of winner data

    public enum HandStage {
        PRE_FLOP,
        FLOP,
        TURN,
        RIVER,
        SHOWDOWN
    }

    public enum HandStatus {
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
