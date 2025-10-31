package com.casino.controller;

import com.casino.dto.*;
import com.casino.entity.Game;
import com.casino.entity.User;
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
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(gameService.launchGame(userId, request));
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        return user.getId();
    }

    @PostMapping("/bet")
    public ResponseEntity<BigDecimal> placeBet(@Valid @RequestBody GameBetRequest request) {
        return ResponseEntity.ok(gameService.placeBet(request));
    }

    @PostMapping("/win")
    public ResponseEntity<BigDecimal> processWin(@Valid @RequestBody GameWinRequest request) {
        return ResponseEntity.ok(gameService.processWin(request));
    }

    @PostMapping("/rollback/{roundId}")
    public ResponseEntity<Void> rollbackBet(@PathVariable String roundId) {
        gameService.rollbackBet(roundId);
        return ResponseEntity.ok().build();
    }
}
