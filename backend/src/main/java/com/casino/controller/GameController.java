package com.casino.controller;

import com.casino.dto.*;
import com.casino.entity.Game;
import com.casino.entity.User;
import com.casino.exception.BadRequestException;
import com.casino.repository.UserRepository;
import com.casino.service.GameService;
import com.casino.util.IpAddressUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
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
            @Valid @RequestBody GameLaunchRequest request,
            HttpServletRequest httpRequest) {
        // Extract client IP for security tracking
        String ipAddress = IpAddressUtil.getClientIpAddress(httpRequest);
        request.setIpAddress(ipAddress);

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
    @RateLimiter(name = "betOperations", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<BigDecimal> placeBet(
            Authentication authentication,
            @Valid @RequestBody GameBetRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromAuth(authentication);
        // Extract client IP for security validation
        String ipAddress = IpAddressUtil.getClientIpAddress(httpRequest);
        request.setIpAddress(ipAddress);
        return ResponseEntity.ok(gameService.placeBet(userId, request));
    }

    @PostMapping("/win")
    @RateLimiter(name = "betOperations", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<BigDecimal> processWin(
            Authentication authentication,
            @Valid @RequestBody GameWinRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromAuth(authentication);
        // Extract client IP for security validation
        String ipAddress = IpAddressUtil.getClientIpAddress(httpRequest);
        request.setIpAddress(ipAddress);
        return ResponseEntity.ok(gameService.processWin(userId, request));
    }

    // Rate limit fallback method
    private ResponseEntity<BigDecimal> rateLimitFallback(Exception e) {
        throw new BadRequestException("Too many requests. Please slow down.");
    }

    @PostMapping("/rollback/{roundId}")
    public ResponseEntity<Void> rollbackBet(@PathVariable String roundId) {
        gameService.rollbackBet(roundId);
        return ResponseEntity.ok().build();
    }
}
