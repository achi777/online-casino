package com.casino.controller;

import com.casino.entity.VIPTier;
import com.casino.repository.VIPTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vip")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_SUPPORT')")
public class AdminVIPController {

    private final VIPTierRepository vipTierRepository;

    @GetMapping("/tiers")
    public ResponseEntity<List<VIPTier>> getAllTiers() {
        return ResponseEntity.ok(vipTierRepository.findAllByOrderByLevelAsc());
    }

    @GetMapping("/tiers/{id}")
    public ResponseEntity<VIPTier> getTierById(@PathVariable Long id) {
        return vipTierRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/tiers")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN')")
    public ResponseEntity<VIPTier> createTier(@RequestBody VIPTier tier) {
        VIPTier saved = vipTierRepository.save(tier);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/tiers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN')")
    public ResponseEntity<VIPTier> updateTier(@PathVariable Long id, @RequestBody VIPTier tier) {
        return vipTierRepository.findById(id)
                .map(existing -> {
                    tier.setId(id);
                    return ResponseEntity.ok(vipTierRepository.save(tier));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
