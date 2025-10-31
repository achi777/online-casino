package com.casino.repository;

import com.casino.entity.Transaction;
import com.casino.entity.Transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);
    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.status = 'COMPLETED'")
    BigDecimal sumByUserIdAndType(Long userId, TransactionType type);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.status = 'COMPLETED' AND t.createdAt >= :from")
    BigDecimal sumByUserIdAndTypeAndDateAfter(Long userId, TransactionType type, LocalDateTime from);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.status = 'COMPLETED' AND t.createdAt >= :startDate AND t.createdAt < :endDate")
    Optional<BigDecimal> sumAmountByTypeAndCreatedAtBetween(String type, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.type = :type AND t.status = :status")
    long countByTypeAndStatus(String type, Transaction.TransactionStatus status);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.status = :status AND t.createdAt BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> sumByTypeAndStatusAndCreatedAtBetween(Transaction.TransactionType type, Transaction.TransactionStatus status, LocalDateTime startDate, LocalDateTime endDate);

    long countByTypeAndStatus(Transaction.TransactionType type, Transaction.TransactionStatus status);

    Page<Transaction> findByType(Transaction.TransactionType type, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.type = :type AND (t.user.email LIKE %:search% OR CAST(t.amount AS string) LIKE %:search%)")
    Page<Transaction> findByTypeAndSearch(Transaction.TransactionType type, String search, Pageable pageable);
}
