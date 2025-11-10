package com.casino.repository;

import com.casino.entity.PokerHand;
import com.casino.entity.PokerTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PokerHandRepository extends JpaRepository<PokerHand, Long> {
    Optional<PokerHand> findByHandId(String handId);
    List<PokerHand> findByTableOrderByCreatedAtDesc(PokerTable table);
    Optional<PokerHand> findTopByTableOrderByCreatedAtDesc(PokerTable table);
}
