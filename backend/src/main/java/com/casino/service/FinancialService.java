package com.casino.service;

import com.casino.dto.FinancialReportResponse;
import com.casino.entity.Transaction;
import com.casino.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialService {

    private final TransactionRepository transactionRepository;

    public FinancialReportResponse getDailyReport(LocalDate date) {
        LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);

        BigDecimal totalDeposits = sumTransactionsByTypeAndDateRange(
                Transaction.TransactionType.DEPOSIT, startOfDay, endOfDay);

        BigDecimal totalWithdrawals = sumTransactionsByTypeAndDateRange(
                Transaction.TransactionType.WITHDRAW, startOfDay, endOfDay);

        BigDecimal totalBets = sumTransactionsByTypeAndDateRange(
                Transaction.TransactionType.BET, startOfDay, endOfDay);

        BigDecimal totalWins = sumTransactionsByTypeAndDateRange(
                Transaction.TransactionType.WIN, startOfDay, endOfDay);

        BigDecimal ggr = totalBets.subtract(totalWins);
        BigDecimal netRevenue = ggr; // Simplified - in production, subtract bonuses, costs, etc.

        return new FinancialReportResponse(
                date, totalDeposits, totalWithdrawals, totalBets, totalWins, ggr, netRevenue);
    }

    public List<FinancialReportResponse> getMonthlyReport(int year, int month) {
        List<FinancialReportResponse> reports = new ArrayList<>();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            reports.add(getDailyReport(date));
        }

        return reports;
    }

    public FinancialReportResponse getDateRangeReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);

        BigDecimal totalDeposits = sumTransactionsByTypeAndDateRange(
                Transaction.TransactionType.DEPOSIT, start, end);

        BigDecimal totalWithdrawals = sumTransactionsByTypeAndDateRange(
                Transaction.TransactionType.WITHDRAW, start, end);

        BigDecimal totalBets = sumTransactionsByTypeAndDateRange(
                Transaction.TransactionType.BET, start, end);

        BigDecimal totalWins = sumTransactionsByTypeAndDateRange(
                Transaction.TransactionType.WIN, start, end);

        BigDecimal ggr = totalBets.subtract(totalWins);
        BigDecimal netRevenue = ggr;

        return new FinancialReportResponse(
                startDate, totalDeposits, totalWithdrawals, totalBets, totalWins, ggr, netRevenue);
    }

    private BigDecimal sumTransactionsByTypeAndDateRange(
            Transaction.TransactionType type, LocalDateTime start, LocalDateTime end) {
        // This is a simplified implementation
        // In production, you'd use a custom query like:
        // SELECT SUM(amount) FROM transactions WHERE type = ? AND created_at BETWEEN ? AND ? AND status = 'COMPLETED'
        return transactionRepository.findAll().stream()
                .filter(t -> t.getType() == type)
                .filter(t -> t.getStatus() == Transaction.TransactionStatus.COMPLETED)
                .filter(t -> t.getCreatedAt().isAfter(start) && t.getCreatedAt().isBefore(end))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
