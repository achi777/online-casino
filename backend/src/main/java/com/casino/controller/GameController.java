package com.casino.controller;

import com.casino.dto.*;
import com.casino.entity.Game;
import com.casino.entity.User;
import com.casino.exception.BadRequestException;
import com.casino.repository.UserRepository;
import com.casino.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/user/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<GameResponse>> getAllGames(Pageable pageable) {
        return ResponseEntity.ok(gameService.getAllGames(pageable));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<GameResponse>> getGamesByCategory(
            @PathVariable Game.GameCategory category,
            Pageable pageable) {
        return ResponseEntity.ok(gameService.getGamesByCategory(category, pageable));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<GameResponse>> getFeaturedGames() {
        return ResponseEntity.ok(gameService.getFeaturedGames());
    }

    @PostMapping("/launch")
    public ResponseEntity<GameLaunchResponse> launchGame(
            Authentication authentication,
            @Valid @RequestBody GameLaunchRequest request) {
        // Demo mode doesn't require authentication
        if (Boolean.TRUE.equals(request.getDemoMode())) {
            return ResponseEntity.ok(gameService.launchGameDemo(request));
        }

        // Real play requires authentication
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(gameService.launchGame(userId, request));
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null) {
            throw new BadRequestException("Authentication required");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found: " + email));
        return user.getId();
    }

    @PostMapping("/bet")
    public ResponseEntity<BigDecimal> placeBet(
            Authentication authentication,
            @Valid @RequestBody GameBetRequest request) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(gameService.placeBet(userId, request));
    }

    @PostMapping("/win")
    public ResponseEntity<BigDecimal> processWin(
            Authentication authentication,
            @Valid @RequestBody GameWinRequest request) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(gameService.processWin(userId, request));
    }

    @PostMapping("/rollback/{roundId}")
    public ResponseEntity<Void> rollbackBet(@PathVariable String roundId) {
        gameService.rollbackBet(roundId);
        return ResponseEntity.ok().build();
    }
}
