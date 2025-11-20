package com.casino.controller;

import com.casino.dto.SpinRequest;
import com.casino.dto.SpinResponse;
import com.casino.entity.GameSession;
import com.casino.entity.User;
import com.casino.exception.BadRequestException;
import com.casino.repository.GameSessionRepository;
import com.casino.repository.UserRepository;
import com.casino.service.SlotSpinService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:8081"})
public class SlotSpinController {

    private final SlotSpinService slotSpinService;
    private final UserRepository userRepository;
    private final GameSessionRepository gameSessionRepository;

    @PostMapping("/games/spin")
    @RateLimiter(name = "slotSpin", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<SpinResponse> spin(
            Authentication authentication,
            @RequestBody SpinRequest request) {
        Long userId = getUserIdFromAuth(authentication);
        SpinResponse response = slotSpinService.processSpin(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/game-sessions/{sessionToken}/spin")
    @RateLimiter(name = "slotSpin", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<SpinResponse> spinByPath(
            Authentication authentication,
            @PathVariable String sessionToken,
            @RequestBody SpinRequest request) {
        // SECURITY: Validate authentication first
        Long userId = getUserIdFromAuth(authentication);

        GameSession session = gameSessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new BadRequestException("Invalid session token"));

        // SECURITY: Validate session belongs to authenticated user
        if (!session.getUser().getId().equals(userId)) {
            throw new BadRequestException("Invalid session");
        }

        User user = session.getUser();

        // Populate missing fields from session
        request.setSessionToken(sessionToken);
        request.setGameId(session.getGame().getId());

        // Generate roundId if not provided
        if (request.getRoundId() == null || request.getRoundId().isEmpty()) {
            request.setRoundId(java.util.UUID.randomUUID().toString());
        }

        SpinResponse response = slotSpinService.processSpin(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    // Rate limit fallback method
    private ResponseEntity<SpinResponse> rateLimitFallback(Exception e) {
        throw new BadRequestException("Too many spin requests. Please slow down.");
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
}
