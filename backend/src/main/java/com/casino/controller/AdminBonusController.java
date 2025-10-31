package com.casino.controller;

import com.casino.entity.Bonus;
import com.casino.repository.BonusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bonuses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_CONTENT')")
public class AdminBonusController {

    private final BonusRepository bonusRepository;

    @GetMapping
    public ResponseEntity<List<Bonus>> getAllBonuses() {
        return ResponseEntity.ok(bonusRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bonus> getBonusById(@PathVariable Long id) {
        return bonusRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_CONTENT')")
    public ResponseEntity<Bonus> createBonus(@RequestBody Bonus bonus) {
        Bonus saved = bonusRepository.save(bonus);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_CONTENT')")
    public ResponseEntity<Bonus> updateBonus(@PathVariable Long id, @RequestBody Bonus bonus) {
        return bonusRepository.findById(id)
                .map(existing -> {
                    bonus.setId(id);
                    return ResponseEntity.ok(bonusRepository.save(bonus));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN')")
    public ResponseEntity<Void> deleteBonus(@PathVariable Long id) {
        bonusRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
