package com.casino.repository;

import com.casino.entity.Game;
import com.casino.entity.Game.GameCategory;
import com.casino.entity.Game.GameStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByGameCode(String gameCode);
    Page<Game> findByStatus(GameStatus status, Pageable pageable);
    Page<Game> findByCategory(GameCategory category, Pageable pageable);
    Page<Game> findByProviderId(Long providerId, Pageable pageable);
    List<Game> findByFeaturedTrueAndStatus(GameStatus status);
    long countByStatus(GameStatus status);
}
