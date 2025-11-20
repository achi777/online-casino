package com.casino.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GameBetRequest {

    @NotBlank(message = "Round ID is required")
    private String roundId;

    @NotBlank(message = "Session token is required")
    private String sessionToken;

    @NotNull(message = "Bet amount is required")
    @DecimalMin(value = "0.01", message = "Minimum bet is 0.01")
    private BigDecimal betAmount;

    private String ipAddress; // Client IP for security validation
}
