package com.casino.controller;

import com.casino.entity.Bonus;
import com.casino.entity.UserBonus;
import com.casino.service.BonusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bonuses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_FINANCE', 'ADMIN_CONTENT')")
public class AdminBonusController {

    private final BonusService bonusService;

    @GetMapping
    public ResponseEntity<List<Bonus>> getAllBonuses() {
        return ResponseEntity.ok(bonusService.getAllBonuses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bonus> getBonusById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bonusService.getBonusById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Bonus> createBonus(@RequestBody Bonus bonus) {
        Bonus saved = bonusService.createBonus(bonus);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bonus> updateBonus(@PathVariable Long id, @RequestBody Bonus bonus) {
        try {
            Bonus updated = bonusService.updateBonus(id, bonus);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_FINANCE')")
    public ResponseEntity<Void> deleteBonus(@PathVariable Long id) {
        bonusService.deleteBonus(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserBonus>> getUserBonuses(@PathVariable Long userId) {
        return ResponseEntity.ok(bonusService.getUserBonuses(userId));
    }
}
