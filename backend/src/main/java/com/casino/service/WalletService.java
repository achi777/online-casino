package com.casino.service;

import com.casino.dto.DepositRequest;
import com.casino.dto.TransactionResponse;
import com.casino.dto.WithdrawRequest;
import com.casino.entity.Transaction;
import com.casino.entity.User;
import com.casino.exception.BadRequestException;
import com.casino.repository.TransactionRepository;
import com.casino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;
    private final VIPService vipService;

    @Transactional
    public TransactionResponse deposit(Long userId, DepositRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BadRequestException("User account is not active");
        }

        // Check deposit limits
        checkDepositLimits(user, request.getAmount());

        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(request.getAmount());

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setType(Transaction.TransactionType.DEPOSIT);
        transaction.setAmount(request.getAmount());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setDescription("Deposit");
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setExternalReference(request.getExternalReference());

        transaction = transactionRepository.save(transaction);

        user.setBalance(balanceAfter);
        userRepository.save(user);

        auditService.logUserAction(userId, "DEPOSIT", "Transaction", transaction.getId(),
                balanceBefore.toString(), balanceAfter.toString());

        // Add VIP points for deposit
        vipService.addPointsForDeposit(userId, request.getAmount(), transaction);

        return TransactionResponse.fromEntity(transaction);
    }

    @Transactional
    public TransactionResponse withdraw(Long userId, WithdrawRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BadRequestException("User account is not active");
        }

        if (user.getKycStatus() != User.KYCStatus.VERIFIED) {
            throw new BadRequestException("KYC verification required for withdrawal");
        }

        if (user.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(request.getAmount());

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setType(Transaction.TransactionType.WITHDRAW);
        transaction.setAmount(request.getAmount());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setDescription("Withdrawal");
        transaction.setPaymentMethod(request.getPaymentMethod());

        transaction = transactionRepository.save(transaction);

        user.setBalance(balanceAfter);
        userRepository.save(user);

        auditService.logUserAction(userId, "WITHDRAW", "Transaction", transaction.getId(),
                balanceBefore.toString(), balanceAfter.toString());

        return TransactionResponse.fromEntity(transaction);
    }

    public Page<TransactionResponse> getTransactionHistory(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable)
                .map(TransactionResponse::fromEntity);
    }

    public BigDecimal getBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return user.getBalance();
    }

    @Transactional
    public void deductBalance(Long userId, BigDecimal amount, String gameType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BadRequestException("User account is not active");
        }

        if (user.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setType(Transaction.TransactionType.BET);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setDescription("Game bet: " + gameType);

        transactionRepository.save(transaction);

        user.setBalance(balanceAfter);
        userRepository.save(user);

        auditService.logUserAction(userId, "GAME_BET", "Transaction", transaction.getId(),
                balanceBefore.toString(), balanceAfter.toString());
    }

    @Transactional
    public void addWinnings(Long userId, BigDecimal amount, String gameType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BadRequestException("User account is not active");
        }

        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setType(Transaction.TransactionType.WIN);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setDescription("Game win: " + gameType);

        transactionRepository.save(transaction);

        user.setBalance(balanceAfter);
        userRepository.save(user);

        auditService.logUserAction(userId, "GAME_WIN", "Transaction", transaction.getId(),
                balanceBefore.toString(), balanceAfter.toString());

        // Add VIP points for wins (if method exists)
        // vipService.addPointsForWin(userId, amount, transaction);
    }

    private void checkDepositLimits(User user, BigDecimal amount) {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime startOfWeek = LocalDateTime.now().minusWeeks(1);
        LocalDateTime startOfMonth = LocalDateTime.now().minusMonths(1);

        // Check daily limit
        if (user.getDailyDepositLimit() != null) {
            BigDecimal dailyTotal = transactionRepository.sumByUserIdAndTypeAndDateAfter(
                    user.getId(), Transaction.TransactionType.DEPOSIT, startOfDay);
            if (dailyTotal == null) dailyTotal = BigDecimal.ZERO;

            if (dailyTotal.add(amount).compareTo(user.getDailyDepositLimit()) > 0) {
                throw new BadRequestException("Daily deposit limit exceeded");
            }
        }

        // Check weekly limit
        if (user.getWeeklyDepositLimit() != null) {
            BigDecimal weeklyTotal = transactionRepository.sumByUserIdAndTypeAndDateAfter(
                    user.getId(), Transaction.TransactionType.DEPOSIT, startOfWeek);
            if (weeklyTotal == null) weeklyTotal = BigDecimal.ZERO;

            if (weeklyTotal.add(amount).compareTo(user.getWeeklyDepositLimit()) > 0) {
                throw new BadRequestException("Weekly deposit limit exceeded");
            }
        }

        // Check monthly limit
        if (user.getMonthlyDepositLimit() != null) {
            BigDecimal monthlyTotal = transactionRepository.sumByUserIdAndTypeAndDateAfter(
                    user.getId(), Transaction.TransactionType.DEPOSIT, startOfMonth);
            if (monthlyTotal == null) monthlyTotal = BigDecimal.ZERO;

            if (monthlyTotal.add(amount).compareTo(user.getMonthlyDepositLimit()) > 0) {
                throw new BadRequestException("Monthly deposit limit exceeded");
            }
        }
    }
}
