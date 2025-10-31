package com.casino.controller;

import com.casino.entity.SupportTicket;
import com.casino.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/support")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_SUPPORT')")
public class AdminSupportController {

    private final SupportTicketRepository supportTicketRepository;

    @GetMapping("/tickets")
    public ResponseEntity<List<SupportTicket>> getAllTickets() {
        return ResponseEntity.ok(supportTicketRepository.findAll());
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<SupportTicket> getTicketById(@PathVariable Long id) {
        return supportTicketRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tickets/status/{status}")
    public ResponseEntity<List<SupportTicket>> getTicketsByStatus(@PathVariable SupportTicket.TicketStatus status) {
        return ResponseEntity.ok(supportTicketRepository.findByStatus(status));
    }

    @PutMapping("/tickets/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_SUPPORT')")
    public ResponseEntity<SupportTicket> updateTicketStatus(
            @PathVariable Long id,
            @RequestParam SupportTicket.TicketStatus status) {
        return supportTicketRepository.findById(id)
                .map(ticket -> {
                    ticket.setStatus(status);
                    if (status == SupportTicket.TicketStatus.RESOLVED || status == SupportTicket.TicketStatus.CLOSED) {
                        ticket.setResolvedAt(LocalDateTime.now());
                    }
                    return ResponseEntity.ok(supportTicketRepository.save(ticket));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/tickets/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_SUPPORT')")
    public ResponseEntity<SupportTicket> resolveTicket(
            @PathVariable Long id,
            @RequestBody String resolution) {
        return supportTicketRepository.findById(id)
                .map(ticket -> {
                    ticket.setStatus(SupportTicket.TicketStatus.RESOLVED);
                    ticket.setResolution(resolution);
                    ticket.setResolvedAt(LocalDateTime.now());
                    return ResponseEntity.ok(supportTicketRepository.save(ticket));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
