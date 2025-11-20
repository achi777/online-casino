package com.casino.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GameWinRequest {

    @NotBlank(message = "Round ID is required")
    private String roundId;

    @NotBlank(message = "Session token is required")
    private String sessionToken;

    @NotNull(message = "Win amount is required")
    @DecimalMin(value = "0.00", message = "Win amount cannot be negative")
    private BigDecimal winAmount;

    private String ipAddress; // Client IP for security validation
}
