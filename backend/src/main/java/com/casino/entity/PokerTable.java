package com.casino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "poker_tables")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PokerTable extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String tableCode;

    @Column(nullable = false)
    private String tableName;

    @Column(nullable = false)
    private Integer maxPlayers = 9; // Max seats at table

    @Column(nullable = false)
    private Integer minPlayers = 2; // Min to start game

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal smallBlind;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal bigBlind;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal minBuyIn;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal maxBuyIn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableStatus status = TableStatus.WAITING;

    @Column
    private Integer currentPlayers = 0;

    @Column
    private Integer dealerPosition = 0;

    @Column
    private String currentHandId; // Reference to current PokerHand

    public enum TableStatus {
        WAITING,    // Waiting for players
        PLAYING,    // Game in progress
        PAUSED,     // Temporarily paused
        CLOSED      // Table closed
    }
}
