package com.casino.repository;

import com.casino.entity.GameRound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRoundRepository extends JpaRepository<GameRound, Long> {
    Optional<GameRound> findByRoundId(String roundId);
    Page<GameRound> findBySessionId(Long sessionId, Pageable pageable);
}
