package com.casino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "vip_tiers")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class VIPTier extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private Integer minPoints;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal cashbackPercentage;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal bonusMultiplier;

    @Column(precision = 19, scale = 2)
    private BigDecimal monthlyBonus;

    private Integer prioritySupport;

    private Boolean personalAccountManager = false;

    private Boolean exclusivePromotions = false;

    private Integer withdrawalLimit;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private Integer sortOrder = 0;
}
