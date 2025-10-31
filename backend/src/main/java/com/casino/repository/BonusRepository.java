package com.casino.repository;

import com.casino.entity.Bonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonusRepository extends JpaRepository<Bonus, Long> {
    List<Bonus> findByStatus(Bonus.BonusStatus status);
    List<Bonus> findByType(Bonus.BonusType type);
}
