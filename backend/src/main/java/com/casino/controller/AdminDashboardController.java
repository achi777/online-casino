package com.casino.controller;

import com.casino.entity.Transaction;
import com.casino.entity.User;
import com.casino.repository.GameRepository;
import com.casino.repository.TransactionRepository;
import com.casino.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final TransactionRepository transactionRepository;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        // User statistics
        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.countByStatus(User.UserStatus.ACTIVE));
        stats.setPendingKYC(userRepository.countByKycStatus(User.KYCStatus.PENDING));

        // Game statistics
        stats.setTotalGames((int) gameRepository.count());
        stats.setActiveGames((int) gameRepository.countByStatus(com.casino.entity.Game.GameStatus.ACTIVE));

        // Today's date range
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);

        // Today's financial stats
        stats.setTotalDepositsToday(
            transactionRepository.sumByTypeAndStatusAndCreatedAtBetween(
                Transaction.TransactionType.DEPOSIT,
                Transaction.TransactionStatus.COMPLETED,
                startOfToday,
                endOfToday
            ).orElse(BigDecimal.ZERO).doubleValue()
        );

        stats.setTotalWithdrawalsToday(
            transactionRepository.sumByTypeAndStatusAndCreatedAtBetween(
                Transaction.TransactionType.WITHDRAW,
                Transaction.TransactionStatus.COMPLETED,
                startOfToday,
                endOfToday
            ).orElse(BigDecimal.ZERO).doubleValue()
        );

        stats.setTotalBetsToday(
            transactionRepository.sumByTypeAndStatusAndCreatedAtBetween(
                Transaction.TransactionType.BET,
                Transaction.TransactionStatus.COMPLETED,
                startOfToday,
                endOfToday
            ).orElse(BigDecimal.ZERO).doubleValue()
        );

        double totalWinsToday = transactionRepository.sumByTypeAndStatusAndCreatedAtBetween(
            Transaction.TransactionType.WIN,
            Transaction.TransactionStatus.COMPLETED,
            startOfToday,
            endOfToday
        ).orElse(BigDecimal.ZERO).doubleValue();

        stats.setGgrToday(stats.getTotalBetsToday() - totalWinsToday);

        // This month's date range
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().atTime(LocalTime.MAX);

        // Month's financial stats
        stats.setTotalDepositsMonth(
            transactionRepository.sumByTypeAndStatusAndCreatedAtBetween(
                Transaction.TransactionType.DEPOSIT,
                Transaction.TransactionStatus.COMPLETED,
                startOfMonth,
                endOfMonth
            ).orElse(BigDecimal.ZERO).doubleValue()
        );

        stats.setTotalWithdrawalsMonth(
            transactionRepository.sumByTypeAndStatusAndCreatedAtBetween(
                Transaction.TransactionType.WITHDRAW,
                Transaction.TransactionStatus.COMPLETED,
                startOfMonth,
                endOfMonth
            ).orElse(BigDecimal.ZERO).doubleValue()
        );

        stats.setTotalBetsMonth(
            transactionRepository.sumByTypeAndStatusAndCreatedAtBetween(
                Transaction.TransactionType.BET,
                Transaction.TransactionStatus.COMPLETED,
                startOfMonth,
                endOfMonth
            ).orElse(BigDecimal.ZERO).doubleValue()
        );

        double totalWinsMonth = transactionRepository.sumByTypeAndStatusAndCreatedAtBetween(
            Transaction.TransactionType.WIN,
            Transaction.TransactionStatus.COMPLETED,
            startOfMonth,
            endOfMonth
        ).orElse(BigDecimal.ZERO).doubleValue();

        stats.setGgrMonth(stats.getTotalBetsMonth() - totalWinsMonth);

        // Pending withdrawals
        stats.setPendingWithdrawals((int) transactionRepository.countByTypeAndStatus(
            Transaction.TransactionType.WITHDRAW,
            Transaction.TransactionStatus.PENDING
        ));

        return ResponseEntity.ok(stats);
    }

    @Data
    public static class DashboardStats {
        private Long totalUsers;
        private Long activeUsers;
        private Long pendingKYC;
        private Integer totalGames;
        private Integer activeGames;
        private Double totalDepositsToday;
        private Double totalWithdrawalsToday;
        private Double totalBetsToday;
        private Double ggrToday;
        private Double totalDepositsMonth;
        private Double totalWithdrawalsMonth;
        private Double totalBetsMonth;
        private Double ggrMonth;
        private Integer pendingWithdrawals;
    }
}
