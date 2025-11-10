package com.casino.repository;

import com.casino.entity.PokerTable;
import com.casino.entity.PokerTableSeat;
import com.casino.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PokerTableSeatRepository extends JpaRepository<PokerTableSeat, Long> {
    List<PokerTableSeat> findByTable(PokerTable table);
    List<PokerTableSeat> findByTableOrderBySeatNumber(PokerTable table);
    Optional<PokerTableSeat> findByTableAndSeatNumber(PokerTable table, Integer seatNumber);
    Optional<PokerTableSeat> findByTableAndUser(PokerTable table, User user);
    List<PokerTableSeat> findByUser(User user);
}
