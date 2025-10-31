package com.casino.repository;

import com.casino.entity.VIPTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VIPTierRepository extends JpaRepository<VIPTier, Long> {
    List<VIPTier> findAllByOrderByLevelAsc();
    Optional<VIPTier> findByLevel(Integer level);
    Optional<VIPTier> findByName(String name);
}
