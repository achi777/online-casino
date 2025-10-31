package com.casino.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long totalGames;
    private long activeGames;
    private BigDecimal totalDepositsToday;
    private BigDecimal totalWithdrawalsToday;
    private BigDecimal totalBetsToday;
    private BigDecimal ggrToday;
    private BigDecimal totalDepositsMonth;
    private BigDecimal totalWithdrawalsMonth;
    private BigDecimal totalBetsMonth;
    private BigDecimal ggrMonth;
    private long pendingKYC;
    private long pendingWithdrawals;
}
