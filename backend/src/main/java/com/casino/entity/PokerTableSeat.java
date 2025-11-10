package com.casino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "poker_table_seats")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PokerTableSeat extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "table_id", nullable = false)
    private PokerTable table;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Integer seatNumber; // 0-8 for 9-player table

    @Column(precision = 19, scale = 2)
    private BigDecimal chipStack = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.EMPTY;

    @Column
    private Boolean isSittingOut = false;

    public enum SeatStatus {
        EMPTY,      // No player
        OCCUPIED,   // Player seated
        RESERVED    // Temporarily reserved
    }
}
