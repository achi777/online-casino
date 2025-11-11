package com.casino.controller;

import com.casino.entity.Game;
import com.casino.entity.GameSession;
import com.casino.entity.User;
import com.casino.exception.BadRequestException;
import com.casino.repository.GameRepository;
import com.casino.repository.GameSessionRepository;
import com.casino.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/arcade")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:8081", "http://localhost:8888"})
public class ArcadeGameController {

    private final UserRepository userRepository;
    private final GameSessionRepository gameSessionRepository;
    private final GameRepository gameRepository;

    @PostMapping("/start")
    @Transactional
    public ResponseEntity<ArcadeStartResponse> startGame(
            Authentication authentication,
            @RequestBody ArcadeStartRequest request) {

        User user = getUserFromAuth(authentication);

        // Validate bet amount
        if (request.getBetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Bet amount must be positive");
        }

        if (user.getBalance().compareTo(request.getBetAmount()) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        // Find game by code
        Game game = gameRepository.findByGameCode(request.getGameCode())
                .orElseThrow(() -> new BadRequestException("Game not found: " + request.getGameCode()));

        // Deduct bet from balance
        user.setBalance(user.getBalance().subtract(request.getBetAmount()));
        userRepository.save(user);

        // Create game session
        GameSession session = new GameSession();
        session.setSessionToken(UUID.randomUUID().toString());
        session.setUser(user);
        session.setGame(game);
        session.setStartedAt(LocalDateTime.now());
        session.setStatus(GameSession.SessionStatus.ACTIVE);
        session.setTotalBet(request.getBetAmount());
        gameSessionRepository.save(session);

        ArcadeStartResponse response = new ArcadeStartResponse();
        response.setSessionToken(session.getSessionToken());
        response.setNewBalance(user.getBalance());
        response.setBetAmount(request.getBetAmount());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cashout")
    @Transactional
    public ResponseEntity<ArcadeCashoutResponse> cashOut(
            Authentication authentication,
            @RequestBody ArcadeCashoutRequest request) {

        User user = getUserFromAuth(authentication);

        // Find active session
        GameSession session = gameSessionRepository.findBySessionToken(request.getSessionToken())
                .orElseThrow(() -> new BadRequestException("Invalid session token"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Session does not belong to user");
        }

        if (session.getStatus() != GameSession.SessionStatus.ACTIVE) {
            throw new BadRequestException("Session is not active");
        }

        // Validate win amount
        BigDecimal winAmount = request.getWinAmount();
        if (winAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Win amount cannot be negative");
        }

        // Add win to balance
        user.setBalance(user.getBalance().add(winAmount));
        userRepository.save(user);

        // Close session
        session.setStatus(GameSession.SessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());
        session.setTotalWin(winAmount);

        // Store game data if provided
        if (request.getGameData() != null) {
            // You can add a JSON field to GameSession to store game-specific data
            // For now, we'll just save the session
        }

        gameSessionRepository.save(session);

        ArcadeCashoutResponse response = new ArcadeCashoutResponse();
        response.setNewBalance(user.getBalance());
        response.setWinAmount(winAmount);
        response.setTotalProfit(winAmount.subtract(session.getTotalBet()));

        return ResponseEntity.ok(response);
    }

    private User getUserFromAuth(Authentication authentication) {
        if (authentication == null) {
            throw new BadRequestException("Authentication required");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found: " + email));
    }

    @Data
    public static class ArcadeStartRequest {
        private BigDecimal betAmount;
        private String gameCode;
    }

    @Data
    public static class ArcadeStartResponse {
        private String sessionToken;
        private BigDecimal newBalance;
        private BigDecimal betAmount;
    }

    @Data
    public static class ArcadeCashoutRequest {
        private String sessionToken;
        private BigDecimal winAmount;
        private Map<String, Object> gameData;
    }

    @Data
    public static class ArcadeCashoutResponse {
        private BigDecimal newBalance;
        private BigDecimal winAmount;
        private BigDecimal totalProfit;
    }
}
