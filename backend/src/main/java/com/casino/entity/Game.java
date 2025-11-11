package com.casino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "games")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Game extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String gameCode;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private GameProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameCategory category;

    private String thumbnailUrl;

    private String iframeUrl;

    @Column(precision = 5, scale = 2)
    private BigDecimal rtp; // Return to Player percentage

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status = GameStatus.ACTIVE;

    private Integer sortOrder = 0;

    private Boolean featured = false;

    public enum GameCategory {
        SLOTS, TABLE_GAMES, LIVE_CASINO, JACKPOT, VIDEO_POKER, ARCADE, OTHER
    }

    public enum GameStatus {
        ACTIVE, INACTIVE, MAINTENANCE
    }
}
