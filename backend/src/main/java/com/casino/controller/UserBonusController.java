package com.casino.controller;

import com.casino.entity.Bonus;
import com.casino.entity.User;
import com.casino.entity.UserBonus;
import com.casino.service.BonusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/bonuses")
@RequiredArgsConstructor
@Slf4j
public class UserBonusController {

    private final BonusService bonusService;

    @GetMapping("/available")
    public ResponseEntity<List<Bonus>> getAvailableBonuses() {
        return ResponseEntity.ok(bonusService.getAllActiveBonuses());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UserBonus>> getMyBonuses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(bonusService.getUserBonuses(user.getId()));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UserBonus>> getActiveBonuses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(bonusService.getActiveUserBonuses(user.getId()));
    }

    @PostMapping("/{bonusId}/claim")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> claimBonus(@PathVariable Long bonusId, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            UserBonus userBonus = bonusService.claimBonus(user.getId(), bonusId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userBonus", userBonus);
            response.put("message", "Bonus claimed successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to claim bonus: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
