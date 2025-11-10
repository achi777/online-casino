package com.casino.repository;

import com.casino.entity.PokerTable;
import com.casino.entity.PokerTable.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PokerTableRepository extends JpaRepository<PokerTable, Long> {
    Optional<PokerTable> findByTableCode(String tableCode);
    List<PokerTable> findByStatus(TableStatus status);
    List<PokerTable> findByStatusIn(List<TableStatus> statuses);
}
