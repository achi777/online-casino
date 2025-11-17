package com.casino.controller;

import com.casino.entity.User;
import com.casino.entity.VIPPointsTransaction;
import com.casino.entity.VIPTier;
import com.casino.repository.UserRepository;
import com.casino.service.VIPService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/vip")
@RequiredArgsConstructor
public class UserVIPController {

    private final VIPService vipService;
    private final UserRepository userRepository;

    @GetMapping("/tiers")
    public ResponseEntity<List<VIPTier>> getAllTiers() {
        return ResponseEntity.ok(vipService.getAllTiers());
    }

    @GetMapping("/my-status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<VIPStatusResponse> getMyVIPStatus(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        VIPStatusResponse response = new VIPStatusResponse();
        response.setCurrentPoints(user.getVipPoints());
        response.setTotalWagered(user.getTotalWagered());
        response.setLifetimeDeposits(user.getLifetimeDeposits());

        if (user.getVipTier() != null) {
            response.setCurrentTier(user.getVipTier());
        }

        // Find next tier
        List<VIPTier> allTiers = vipService.getAllTiers();
        VIPTier nextTier = null;
        int pointsToNextTier = 0;

        for (VIPTier tier : allTiers) {
            if (user.getVipTier() == null || tier.getLevel() > user.getVipTier().getLevel()) {
                if (user.getVipPoints() < tier.getMinPoints()) {
                    nextTier = tier;
                    pointsToNextTier = tier.getMinPoints() - user.getVipPoints();
                    break;
                }
            }
        }

        response.setNextTier(nextTier);
        response.setPointsToNextTier(pointsToNextTier);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<VIPPointsTransaction>> getMyPointsHistory(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(vipService.getUserPointsHistory(user.getId()));
    }

    @GetMapping("/my-history/paginated")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<VIPPointsTransaction>> getMyPointsHistoryPaginated(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(vipService.getUserPointsHistory(user.getId(), PageRequest.of(page, size)));
    }

    @GetMapping("/benefits")
    public ResponseEntity<Map<String, Object>> getVIPBenefits() {
        List<VIPTier> tiers = vipService.getAllTiers();

        Map<String, Object> response = new HashMap<>();
        response.put("tiers", tiers);
        response.put("howToEarn", Map.of(
            "wagering", "1 ლარი wagering = 1 VIP ქულა",
            "deposits", "1 ლარი deposit = 0.1 VIP ქულა"
        ));

        return ResponseEntity.ok(response);
    }

    @Data
    public static class VIPStatusResponse {
        private VIPTier currentTier;
        private Integer currentPoints;
        private VIPTier nextTier;
        private Integer pointsToNextTier;
        private java.math.BigDecimal totalWagered;
        private java.math.BigDecimal lifetimeDeposits;
    }
}
