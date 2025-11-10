package com.casino.controller;

import com.casino.entity.GameSession;
import com.casino.entity.User;
import com.casino.repository.GameSessionRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserBalanceController {

    private final GameSessionRepository gameSessionRepository;

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(@RequestParam String sessionToken) {
        GameSession session = gameSessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new RuntimeException("Invalid session token"));

        User user = session.getUser();

        BalanceResponse response = new BalanceResponse();
        response.setBalance(user.getBalance().doubleValue());
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/game-sessions/{sessionToken}/balance")
    public ResponseEntity<BalanceResponse> getBalanceByPath(@PathVariable String sessionToken) {
        GameSession session = gameSessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new RuntimeException("Invalid session token"));

        User user = session.getUser();

        BalanceResponse response = new BalanceResponse();
        response.setBalance(user.getBalance().doubleValue());
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/game-info")
    public ResponseEntity<GameInfoResponse> getGameInfo(@RequestParam String sessionToken) {
        GameSession session = gameSessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new RuntimeException("Invalid session token"));

        GameInfoResponse response = new GameInfoResponse();
        response.setGameId(session.getGame().getId());
        response.setGameName(session.getGame().getName());
        response.setGameCode(session.getGame().getGameCode());

        return ResponseEntity.ok(response);
    }

    @Data
    public static class BalanceResponse {
        private Double balance;
        private Long userId;
        private String email;
    }

    @Data
    public static class GameInfoResponse {
        private Long gameId;
        private String gameName;
        private String gameCode;
    }
}
