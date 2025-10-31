package com.casino.dto;

import com.casino.entity.Game;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateGameRequest {

    @NotBlank(message = "Game code is required")
    private String gameCode;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Provider ID is required")
    private Long providerId;

    @NotNull(message = "Category is required")
    private Game.GameCategory category;

    private String thumbnailUrl;

    private String iframeUrl;

    @DecimalMin(value = "0.0", message = "RTP must be positive")
    @DecimalMax(value = "100.0", message = "RTP cannot exceed 100")
    private BigDecimal rtp;

    private Integer sortOrder = 0;

    private Boolean featured = false;
}
