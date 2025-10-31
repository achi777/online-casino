package com.casino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "banners")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Banner extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String imageUrl;

    private String link;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BannerPosition position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BannerStatus status = BannerStatus.ACTIVE;

    @Column(nullable = false)
    private Integer displayOrder = 0;

    private Boolean openInNewTab = false;

    public enum BannerPosition {
        HOME_TOP,
        HOME_MIDDLE,
        HOME_BOTTOM,
        GAMES_TOP,
        SIDEBAR,
        POPUP
    }

    public enum BannerStatus {
        ACTIVE,
        INACTIVE,
        SCHEDULED,
        EXPIRED
    }
}
