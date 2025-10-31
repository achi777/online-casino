package com.casino.controller;

import com.casino.dto.DepositRequest;
import com.casino.dto.TransactionResponse;
import com.casino.dto.WithdrawRequest;
import com.casino.entity.User;
import com.casino.repository.UserRepository;
import com.casino.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/user/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final UserRepository userRepository;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            Authentication authentication,
            @Valid @RequestBody DepositRequest request) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(walletService.deposit(userId, request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            Authentication authentication,
            @Valid @RequestBody WithdrawRequest request) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(walletService.withdraw(userId, request));
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(walletService.getBalance(userId));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            Authentication authentication,
            Pageable pageable) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(walletService.getTransactionHistory(userId, pageable));
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        return user.getId();
    }
}
