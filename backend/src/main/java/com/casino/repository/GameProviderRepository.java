package com.casino.repository;

import com.casino.entity.GameProvider;
import com.casino.entity.GameProvider.ProviderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameProviderRepository extends JpaRepository<GameProvider, Long> {
    Optional<GameProvider> findByCode(String code);
    List<GameProvider> findByStatus(ProviderStatus status);
}
