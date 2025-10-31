package com.casino.controller;

import com.casino.entity.Transaction;
import com.casino.repository.TransactionRepository;
import com.casino.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ReportData> getReport(
            @RequestParam(required = false) String type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        LocalDateTime startDate = dateFrom.atStartOfDay();
        LocalDateTime endDate = dateTo.atTime(LocalTime.MAX);

        ReportData report = new ReportData();

        // Financial stats
        report.setTotalDeposits(
            transactionRepository.sumByTypeAndStatusAndCreatedAtBetween(
                Transaction.TransactionType.DEPOSIT,
                Transaction.TransactionStatus.COMPLETED,
                startDate,
                endDate
            ).orElse(BigDecimal.ZERO).doubleValue()
        );

        report.setTotalWithdrawals(
            transactionRepository.sumByTypeAndStatusAndCreatedAtBetween(
                Transaction.TransactionType.WITHDRAW,
                Transaction.TransactionStatus.COMPLETED,
                startDate,
                endDate
            ).orElse(BigDecimal.ZERO).doubleValue()
        );

        report.setTotalBets(
            transactionRepository.sumByTypeAndStatusAndCreatedAtBetween(
                Transaction.TransactionType.BET,
                Transaction.TransactionStatus.COMPLETED,
                startDate,
                endDate
            ).orElse(BigDecimal.ZERO).doubleValue()
        );

        double totalWins = transactionRepository.sumByTypeAndStatusAndCreatedAtBetween(
            Transaction.TransactionType.WIN,
            Transaction.TransactionStatus.COMPLETED,
            startDate,
            endDate
        ).orElse(BigDecimal.ZERO).doubleValue();

        report.setTotalWins(totalWins);
        report.setGgr(report.getTotalBets() - totalWins);
        report.setNgr(report.getTotalDeposits() - report.getTotalWithdrawals());

        // User stats
        report.setActiveUsers((int) userRepository.count());
        report.setNewUsers(0); // TODO: implement with date filtering

        // Mock data for top games and users
        report.setTopGames(new ArrayList<>());
        report.setTopUsers(new ArrayList<>());

        return ResponseEntity.ok(report);
    }

    @Data
    public static class ReportData {
        private Double totalDeposits;
        private Double totalWithdrawals;
        private Double totalBets;
        private Double totalWins;
        private Double ggr;
        private Double ngr;
        private Integer activeUsers;
        private Integer newUsers;
        private Integer totalGames;
        private List<TopGame> topGames;
        private List<TopUser> topUsers;
    }

    @Data
    public static class TopGame {
        private Long gameId;
        private String gameName;
        private Double totalBets;
        private Double totalWins;
        private Double ggr;
    }

    @Data
    public static class TopUser {
        private Long userId;
        private String userEmail;
        private Double totalBets;
        private Double totalWins;
        private Double ggr;
    }
}
