package com.casino.repository;

import com.casino.entity.SupportTicket;
import com.casino.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByUser(User user);
    List<SupportTicket> findByStatus(SupportTicket.TicketStatus status);
    List<SupportTicket> findByCategory(SupportTicket.TicketCategory category);
    List<SupportTicket> findByPriority(SupportTicket.TicketPriority priority);
}
