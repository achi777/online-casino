package com.casino.service;

import com.casino.dto.*;
import com.casino.entity.*;
import com.casino.exception.BadRequestException;
import com.casino.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GameSessionRepository gameSessionRepository;
    private final GameRoundRepository gameRoundRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<GameResponse> getAllGames(Pageable pageable) {
        return gameRepository.findByStatus(Game.GameStatus.ACTIVE, pageable)
                .map(GameResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<GameResponse> getGamesByCategory(Game.GameCategory category, Pageable pageable) {
        return gameRepository.findByCategory(category, pageable)
                .map(GameResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<GameResponse> getFeaturedGames() {
        return gameRepository.findByFeaturedTrueAndStatus(Game.GameStatus.ACTIVE)
                .stream()
                .map(GameResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public GameLaunchResponse launchGame(Long userId, GameLaunchRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BadRequestException("User account is not active");
        }

        // Check self-exclusion
        if (user.getSelfExclusionUntil() != null &&
            user.getSelfExclusionUntil().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Account is under self-exclusion");
        }

        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new BadRequestException("Game not found"));

        if (game.getStatus() != Game.GameStatus.ACTIVE) {
            throw new BadRequestException("Game is not available");
        }

        // Create game session
        GameSession session = new GameSession();
        session.setUser(user);
        session.setGame(game);
        session.setSessionToken(UUID.randomUUID().toString());
        session.setStartedAt(LocalDateTime.now());
        session = gameSessionRepository.save(session);

        auditService.logUserAction(userId, "GAME_LAUNCHED", "GameSession", session.getId(),
                null, "Game: " + game.getName());

        // Generate launch URL based on integration type
        String launchUrl = generateLaunchUrl(game, session.getSessionToken(), request.getDemoMode());
        String integrationType = game.getProvider().getIntegrationType().name();

        return new GameLaunchResponse(session.getSessionToken(), launchUrl, integrationType);
    }

    @Transactional
    public BigDecimal placeBet(GameBetRequest request) {
        GameSession session = gameSessionRepository.findBySessionToken(request.getSessionToken())
                .orElseThrow(() -> new BadRequestException("Invalid session"));

        User user = session.getUser();

        if (user.getBalance().compareTo(request.getBetAmount()) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        // Check if round already exists
        if (gameRoundRepository.findByRoundId(request.getRoundId()).isPresent()) {
            throw new BadRequestException("Round already exists");
        }

        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(request.getBetAmount());

        // Create game round
        GameRound round = new GameRound();
        round.setSession(session);
        round.setRoundId(request.getRoundId());
        round.setBetAmount(request.getBetAmount());
        round.setWinAmount(BigDecimal.ZERO);
        round.setBalanceBefore(balanceBefore);
        round.setBalanceAfter(balanceAfter);
        round.setStatus(GameRound.RoundStatus.PENDING);
        gameRoundRepository.save(round);

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setType(Transaction.TransactionType.BET);
        transaction.setAmount(request.getBetAmount());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setDescription("Bet - " + session.getGame().getName());
        transactionRepository.save(transaction);

        // Update user balance
        user.setBalance(balanceAfter);
        userRepository.save(user);

        // Update session stats
        session.setTotalBet(session.getTotalBet().add(request.getBetAmount()));
        session.setRoundsPlayed(session.getRoundsPlayed() + 1);
        gameSessionRepository.save(session);

        return balanceAfter;
    }

    @Transactional
    public BigDecimal processWin(GameWinRequest request) {
        GameSession session = gameSessionRepository.findBySessionToken(request.getSessionToken())
                .orElseThrow(() -> new BadRequestException("Invalid session"));

        GameRound round = gameRoundRepository.findByRoundId(request.getRoundId())
                .orElseThrow(() -> new BadRequestException("Round not found"));

        if (round.getStatus() == GameRound.RoundStatus.COMPLETED) {
            throw new BadRequestException("Round already completed");
        }

        User user = session.getUser();
        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(request.getWinAmount());

        // Update round
        round.setWinAmount(request.getWinAmount());
        round.setBalanceAfter(balanceAfter);
        round.setStatus(GameRound.RoundStatus.COMPLETED);
        gameRoundRepository.save(round);

        // Create transaction if there's a win
        if (request.getWinAmount().compareTo(BigDecimal.ZERO) > 0) {
            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setTransactionId(UUID.randomUUID().toString());
            transaction.setType(Transaction.TransactionType.WIN);
            transaction.setAmount(request.getWinAmount());
            transaction.setBalanceBefore(balanceBefore);
            transaction.setBalanceAfter(balanceAfter);
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setDescription("Win - " + session.getGame().getName());
            transactionRepository.save(transaction);

            // Update user balance
            user.setBalance(balanceAfter);
            userRepository.save(user);
        }

        // Update session stats
        session.setTotalWin(session.getTotalWin().add(request.getWinAmount()));
        gameSessionRepository.save(session);

        return balanceAfter;
    }

    @Transactional
    public void rollbackBet(String roundId) {
        GameRound round = gameRoundRepository.findByRoundId(roundId)
                .orElseThrow(() -> new BadRequestException("Round not found"));

        if (round.getStatus() == GameRound.RoundStatus.ROLLED_BACK) {
            throw new BadRequestException("Round already rolled back");
        }

        GameSession session = round.getSession();
        User user = session.getUser();

        // Refund bet amount
        user.setBalance(user.getBalance().add(round.getBetAmount()));
        userRepository.save(user);

        // Update round status
        round.setStatus(GameRound.RoundStatus.ROLLED_BACK);
        gameRoundRepository.save(round);

        // Create refund transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setType(Transaction.TransactionType.REFUND);
        transaction.setAmount(round.getBetAmount());
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setDescription("Rollback - " + session.getGame().getName());
        transactionRepository.save(transaction);
    }

    private String generateLaunchUrl(Game game, String sessionToken, Boolean demoMode) {
        // This would be customized based on the provider's integration requirements
        if (game.getProvider().getIntegrationType() == GameProvider.IntegrationType.IFRAME) {
            return game.getIframeUrl() + "?session=" + sessionToken + "&demo=" + demoMode;
        } else {
            return game.getProvider().getApiUrl() + "/launch?game=" + game.getGameCode() +
                   "&session=" + sessionToken + "&demo=" + demoMode;
        }
    }
}
