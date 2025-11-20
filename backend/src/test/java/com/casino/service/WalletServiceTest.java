package com.casino.service;

import com.casino.dto.DepositRequest;
import com.casino.dto.TransactionResponse;
import com.casino.dto.WithdrawRequest;
import com.casino.entity.Transaction;
import com.casino.entity.User;
import com.casino.exception.BadRequestException;
import com.casino.repository.TransactionRepository;
import com.casino.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WalletService}.
 *
 * <p>These tests verify:</p>
 * <ul>
 *   <li>Deposit processing and validation</li>
 *   <li>Withdrawal processing and KYC checks</li>
 *   <li>Balance management</li>
 *   <li>Transaction recording</li>
 * </ul>
 *
 * @author Casino Platform
 * @version 1.0
 * @since 2025-11-19
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WalletService Tests")
class WalletServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private WalletService walletService;
    private AuditService auditService;
    private VIPService vipService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create stub implementations to avoid Java 23 + Mockito compatibility issues
        auditService = new AuditService(null) {
            @Override
            public void logUserAction(Long userId, String action, String entityType, Long entityId, String oldValue, String newValue) {
                // Stub implementation - do nothing
            }
        };

        vipService = new VIPService(null, null, null) {
            @Override
            public void addPointsForDeposit(Long userId, java.math.BigDecimal depositAmount, com.casino.entity.Transaction transaction) {
                // Stub implementation - do nothing
            }
        };

        // Manually instantiate WalletService with all dependencies
        walletService = new WalletService(
            userRepository,
            transactionRepository,
            auditService,
            vipService
        );
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@casino.ge");
        testUser.setBalance(new BigDecimal("1000.00"));
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setKycStatus(User.KYCStatus.VERIFIED);
        testUser.setVipPoints(100);
        testUser.setLifetimeDeposits(BigDecimal.ZERO);
        testUser.setLifetimeWithdrawals(BigDecimal.ZERO);
    }

    // ===== Deposit Tests =====

    @Test
    @DisplayName("Should successfully process deposit")
    void shouldProcessDepositSuccessfully() {
        // Given
        DepositRequest request = new DepositRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setPaymentMethod("BANK_CARD");
        request.setExternalReference("REF_12345");

        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(1L);
        savedTransaction.setTransactionId("TXN_001");
        savedTransaction.setAmount(new BigDecimal("100.00"));
        savedTransaction.setType(Transaction.TransactionType.DEPOSIT);
        savedTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        TransactionResponse response = walletService.deposit(1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(testUser.getBalance()).isEqualByComparingTo("1100.00");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should reject deposit for inactive user")
    void shouldRejectDepositForInactiveUser() {
        // Given
        testUser.setStatus(User.UserStatus.SUSPENDED);
        DepositRequest request = new DepositRequest();
        request.setAmount(new BigDecimal("100.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> walletService.deposit(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not active");
    }

    // ===== Withdrawal Tests =====

    @Test
    @DisplayName("Should successfully process withdrawal for KYC verified user")
    void shouldProcessWithdrawalSuccessfully() {
        // Given
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setPaymentMethod("BANK_TRANSFER");

        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(1L);
        savedTransaction.setTransactionId("TXN_002");
        savedTransaction.setAmount(new BigDecimal("100.00"));
        savedTransaction.setType(Transaction.TransactionType.WITHDRAW);
        savedTransaction.setStatus(Transaction.TransactionStatus.PENDING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        TransactionResponse response = walletService.withdraw(1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(testUser.getBalance()).isEqualByComparingTo("900.00");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should reject withdrawal without KYC verification")
    void shouldRejectWithdrawalWithoutKyc() {
        // Given
        testUser.setKycStatus(User.KYCStatus.PENDING);
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(new BigDecimal("100.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> walletService.withdraw(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("KYC verification required");
    }

    @Test
    @DisplayName("Should reject withdrawal for insufficient balance")
    void shouldRejectWithdrawalForInsufficientBalance() {
        // Given
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(new BigDecimal("5000.00")); // More than balance

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> walletService.withdraw(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    @DisplayName("Should reject withdrawal for inactive user")
    void shouldRejectWithdrawalForInactiveUser() {
        // Given
        testUser.setStatus(User.UserStatus.BLOCKED);
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(new BigDecimal("100.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> walletService.withdraw(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not active");
    }

    // ===== Balance Validation Tests =====

    @Test
    @DisplayName("Should maintain accurate balance after multiple deposits")
    void shouldMaintainAccurateBalanceAfterMultipleDeposits() {
        // Given
        DepositRequest request1 = new DepositRequest();
        request1.setAmount(new BigDecimal("50.00"));
        DepositRequest request2 = new DepositRequest();
        request2.setAmount(new BigDecimal("75.00"));

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setType(Transaction.TransactionType.DEPOSIT);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        walletService.deposit(1L, request1);
        walletService.deposit(1L, request2);

        // Then
        assertThat(testUser.getBalance()).isEqualByComparingTo("1125.00");
    }

    @Test
    @DisplayName("Should maintain accurate balance after deposit and withdrawal")
    void shouldMaintainAccurateBalanceAfterDepositAndWithdrawal() {
        // Given
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setAmount(new BigDecimal("200.00"));
        WithdrawRequest withdrawRequest = new WithdrawRequest();
        withdrawRequest.setAmount(new BigDecimal("150.00"));

        Transaction depositTransaction = new Transaction();
        depositTransaction.setId(1L);
        depositTransaction.setType(Transaction.TransactionType.DEPOSIT);
        depositTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);

        Transaction withdrawTransaction = new Transaction();
        withdrawTransaction.setId(2L);
        withdrawTransaction.setType(Transaction.TransactionType.WITHDRAW);
        withdrawTransaction.setStatus(Transaction.TransactionStatus.PENDING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(depositTransaction)
                .thenReturn(withdrawTransaction);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        walletService.deposit(1L, depositRequest);
        walletService.withdraw(1L, withdrawRequest);

        // Then
        assertThat(testUser.getBalance()).isEqualByComparingTo("1050.00");
    }
}
