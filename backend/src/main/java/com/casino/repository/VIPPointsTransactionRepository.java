package com.casino.repository;

import com.casino.entity.VIPPointsTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VIPPointsTransactionRepository extends JpaRepository<VIPPointsTransaction, Long> {
    List<VIPPointsTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<VIPPointsTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<VIPPointsTransaction> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, LocalDateTime start, LocalDateTime end);
    List<VIPPointsTransaction> findByTypeOrderByCreatedAtDesc(VIPPointsTransaction.TransactionType type);
}
