package com.casino.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SpinResponse {
    private List<String> results;
    private boolean isWin;
    private BigDecimal winAmount;
    private BigDecimal newBalance;
    private String message;
}
