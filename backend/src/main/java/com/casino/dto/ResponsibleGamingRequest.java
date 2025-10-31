package com.casino.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ResponsibleGamingRequest {

    @DecimalMin(value = "0.0", message = "Limit must be positive")
    private BigDecimal dailyDepositLimit;

    @DecimalMin(value = "0.0", message = "Limit must be positive")
    private BigDecimal weeklyDepositLimit;

    @DecimalMin(value = "0.0", message = "Limit must be positive")
    private BigDecimal monthlyDepositLimit;

    @Min(value = 1, message = "Time limit must be at least 1 minute")
    private Integer dailyTimeLimit;

    private LocalDateTime selfExclusionUntil;

    private Boolean temporaryBlock;
}
