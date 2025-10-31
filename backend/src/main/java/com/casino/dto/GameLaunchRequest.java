package com.casino.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GameLaunchRequest {

    @NotNull(message = "Game ID is required")
    private Long gameId;

    private Boolean demoMode = false;
}
