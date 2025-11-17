package com.casino.repository;

import com.casino.entity.UserBonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBonusRepository extends JpaRepository<UserBonus, Long> {
    List<UserBonus> findByUserId(Long userId);
    List<UserBonus> findByUserIdAndStatus(Long userId, UserBonus.UserBonusStatus status);
    Optional<UserBonus> findByUserIdAndBonusId(Long userId, Long bonusId);
    boolean existsByUserIdAndBonusId(Long userId, Long bonusId);
}
