package com.casino.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class FinancialReportResponse {
    private LocalDate date;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal totalBets;
    private BigDecimal totalWins;
    private BigDecimal ggr; // Gross Gaming Revenue (Bets - Wins)
    private BigDecimal netRevenue; // GGR - Bonuses - Costs
}
