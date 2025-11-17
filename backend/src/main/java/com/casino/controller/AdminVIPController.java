package com.casino.controller;

import com.casino.entity.User;
import com.casino.entity.VIPPointsTransaction;
import com.casino.entity.VIPTier;
import com.casino.service.VIPService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/vip")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_FINANCE', 'ADMIN_SUPPORT')")
public class AdminVIPController {

    private final VIPService vipService;

    // VIP Tiers Management
    @GetMapping("/tiers")
    public ResponseEntity<List<VIPTier>> getAllTiers() {
        return ResponseEntity.ok(vipService.getAllTiers());
    }

    @GetMapping("/tiers/{id}")
    public ResponseEntity<VIPTier> getTierById(@PathVariable Long id) {
        return vipService.getTierById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/tiers")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_FINANCE')")
    public ResponseEntity<VIPTier> createTier(@RequestBody VIPTier tier) {
        VIPTier saved = vipService.createTier(tier);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/tiers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_FINANCE')")
    public ResponseEntity<VIPTier> updateTier(@PathVariable Long id, @RequestBody VIPTier tier) {
        try {
            VIPTier updated = vipService.updateTier(id, tier);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/tiers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER')")
    public ResponseEntity<Void> deleteTier(@PathVariable Long id) {
        vipService.deleteTier(id);
        return ResponseEntity.ok().build();
    }

    // VIP Users Management
    @GetMapping("/users")
    public ResponseEntity<List<User>> getVIPUsers() {
        return ResponseEntity.ok(vipService.getVIPUsers());
    }

    @GetMapping("/users/tier/{tierId}")
    public ResponseEntity<List<User>> getUsersByTier(@PathVariable Long tierId) {
        return ResponseEntity.ok(vipService.getUsersByTier(tierId));
    }

    // VIP Points Management
    @PostMapping("/users/{userId}/points")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_FINANCE')")
    public ResponseEntity<Map<String, Object>> adjustUserPoints(
            @PathVariable Long userId,
            @RequestBody AdjustPointsRequest request) {
        try {
            vipService.addManualPoints(userId, request.getPoints(), request.getDescription());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Points adjusted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/users/{userId}/points/history")
    public ResponseEntity<List<VIPPointsTransaction>> getUserPointsHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(vipService.getUserPointsHistory(userId));
    }

    @GetMapping("/users/{userId}/points/history/paginated")
    public ResponseEntity<Page<VIPPointsTransaction>> getUserPointsHistoryPaginated(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(vipService.getUserPointsHistory(userId, PageRequest.of(page, size)));
    }

    @Data
    public static class AdjustPointsRequest {
        private int points;
        private String description;
    }
}
