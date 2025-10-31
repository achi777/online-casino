package com.casino.controller;

import com.casino.dto.*;
import com.casino.entity.Game;
import com.casino.entity.GameProvider;
import com.casino.service.AdminGameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/games")
@RequiredArgsConstructor
public class AdminGameController {

    private final AdminGameService adminGameService;

    @PostMapping
    public ResponseEntity<GameResponse> createGame(
            Authentication authentication,
            @Valid @RequestBody CreateGameRequest request) {
        Long adminId = 1L; // Placeholder
        return ResponseEntity.ok(adminGameService.createGame(adminId, request));
    }

    @GetMapping
    public ResponseEntity<Page<GameResponse>> getAllGames(Pageable pageable) {
        return ResponseEntity.ok(adminGameService.getAllGames(pageable));
    }

    @PutMapping("/{gameId}/status")
    public ResponseEntity<GameResponse> updateGameStatus(
            Authentication authentication,
            @PathVariable Long gameId,
            @RequestBody Map<String, String> request) {
        Long adminId = 1L; // Placeholder
        Game.GameStatus status = Game.GameStatus.valueOf(request.get("status"));
        return ResponseEntity.ok(adminGameService.updateGameStatus(adminId, gameId, status));
    }

    @PutMapping("/{gameId}")
    public ResponseEntity<GameResponse> updateGame(
            Authentication authentication,
            @PathVariable Long gameId,
            @RequestBody UpdateGameRequest request) {
        Long adminId = 1L; // Placeholder
        return ResponseEntity.ok(adminGameService.updateGame(adminId, gameId, request));
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> deleteGame(
            Authentication authentication,
            @PathVariable Long gameId) {
        Long adminId = 1L; // Placeholder
        adminGameService.deleteGame(adminId, gameId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/providers")
    public ResponseEntity<ProviderResponse> createProvider(
            Authentication authentication,
            @Valid @RequestBody CreateProviderRequest request) {
        Long adminId = 1L; // Placeholder
        return ResponseEntity.ok(adminGameService.createProvider(adminId, request));
    }

    @GetMapping("/providers")
    public ResponseEntity<List<ProviderResponse>> getAllProviders() {
        return ResponseEntity.ok(adminGameService.getAllProviders());
    }

    @PutMapping("/providers/{providerId}/status")
    public ResponseEntity<ProviderResponse> updateProviderStatus(
            Authentication authentication,
            @PathVariable Long providerId,
            @RequestBody Map<String, String> request) {
        Long adminId = 1L; // Placeholder
        GameProvider.ProviderStatus status = GameProvider.ProviderStatus.valueOf(request.get("status"));
        return ResponseEntity.ok(adminGameService.updateProviderStatus(adminId, providerId, status));
    }
}
