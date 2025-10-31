package com.casino.dto;

import com.casino.entity.Game;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateGameRequest {
    private String name;
    private String description;
    private Game.GameCategory category;
    private String thumbnailUrl;
    private String iframeUrl;

    @DecimalMin(value = "0.0", message = "RTP must be positive")
    @DecimalMax(value = "100.0", message = "RTP cannot exceed 100")
    private BigDecimal rtp;

    private Integer sortOrder;
    private Boolean featured;
    private Game.GameStatus status;
}
