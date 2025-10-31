package com.casino.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpinRequest {
    private String sessionToken;
    private Long gameId;
    private BigDecimal betAmount;
    private String roundId;
}
