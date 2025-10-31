package com.casino.controller;

import com.casino.dto.TransactionResponse;
import com.casino.entity.Transaction;
import com.casino.repository.TransactionRepository;
import com.casino.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/transactions")
@RequiredArgsConstructor
public class AdminTransactionController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<AdminTransactionResponse>> getAllTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Transaction> transactions;

        if (type != null) {
            Transaction.TransactionType transactionType = Transaction.TransactionType.valueOf(type);
            if (search != null && !search.isEmpty()) {
                transactions = transactionRepository.findByTypeAndSearch(transactionType, search, pageable);
            } else {
                transactions = transactionRepository.findByType(transactionType, pageable);
            }
        } else {
            transactions = transactionRepository.findAll(pageable);
        }

        Page<AdminTransactionResponse> response = transactions.map(this::toAdminTransactionResponse);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{transactionId}/status")
    public ResponseEntity<AdminTransactionResponse> updateTransactionStatus(
            @PathVariable Long transactionId,
            @RequestBody Map<String, String> request) {

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        String statusStr = request.get("status");
        String notes = request.get("notes");

        Transaction.TransactionStatus newStatus = Transaction.TransactionStatus.valueOf(statusStr);
        transaction.setStatus(newStatus);

        if (notes != null && !notes.isEmpty()) {
            transaction.setDescription(
                (transaction.getDescription() != null ? transaction.getDescription() + " | " : "") + notes
            );
        }

        // If approving a withdrawal, update user balance
        if (newStatus == Transaction.TransactionStatus.COMPLETED &&
            transaction.getType() == Transaction.TransactionType.WITHDRAW) {
            // Balance was already deducted when withdrawal was created as PENDING
            // No need to deduct again
        }

        // If rejecting a withdrawal, refund the amount
        if (newStatus == Transaction.TransactionStatus.REJECTED &&
            transaction.getType() == Transaction.TransactionType.WITHDRAW) {
            var user = transaction.getUser();
            user.setBalance(user.getBalance().add(transaction.getAmount()));
            userRepository.save(user);
        }

        transactionRepository.save(transaction);

        return ResponseEntity.ok(toAdminTransactionResponse(transaction));
    }

    private AdminTransactionResponse toAdminTransactionResponse(Transaction transaction) {
        AdminTransactionResponse response = new AdminTransactionResponse();
        response.setId(transaction.getId());
        response.setUserId(transaction.getUser().getId());
        response.setUserEmail(transaction.getUser().getEmail());
        response.setType(transaction.getType().name());
        response.setAmount(transaction.getAmount().doubleValue());
        response.setStatus(transaction.getStatus().name());
        response.setPaymentMethod(transaction.getPaymentMethod());
        response.setCreatedAt(transaction.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        if (transaction.getUpdatedAt() != null) {
            response.setProcessedAt(transaction.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        response.setNotes(transaction.getDescription());
        return response;
    }

    @Data
    public static class AdminTransactionResponse {
        private Long id;
        private Long userId;
        private String userEmail;
        private String type;
        private Double amount;
        private String status;
        private String paymentMethod;
        private String createdAt;
        private String processedAt;
        private String notes;
    }
}
