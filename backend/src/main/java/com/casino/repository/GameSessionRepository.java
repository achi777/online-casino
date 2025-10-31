package com.casino.repository;

import com.casino.entity.GameSession;
import com.casino.entity.GameSession.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    Optional<GameSession> findBySessionToken(String sessionToken);
    Page<GameSession> findByUserId(Long userId, Pageable pageable);
    Page<GameSession> findByUserIdAndStatus(Long userId, SessionStatus status, Pageable pageable);

    @Query("SELECT SUM(gs.totalBet) FROM GameSession gs WHERE gs.createdAt >= :startDate AND gs.createdAt < :endDate")
    Optional<BigDecimal> sumTotalBetByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(gs.totalWin) FROM GameSession gs WHERE gs.createdAt >= :startDate AND gs.createdAt < :endDate")
    Optional<BigDecimal> sumTotalWinByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
