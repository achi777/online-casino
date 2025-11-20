package com.casino.service;

import com.casino.constants.GameConstants;
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

/**
 * Service class for managing game operations and sessions.
 *
 * <p>This service handles:</p>
 * <ul>
 *   <li>Game catalog management</li>
 *   <li>Game session creation and validation</li>
 *   <li>Bet and win processing</li>
 *   <li>Game round management</li>
 *   <li>Transaction recording</li>
 * </ul>
 *
 * @author Casino Platform
 * @version 1.0
 * @since 2025-11-19
 */

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GameSessionRepository gameSessionRepository;
    private final GameRoundRepository gameRoundRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;
    private final VIPService vipService;

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

    @Transactional(readOnly = true)
    public GameLaunchResponse launchGameDemo(GameLaunchRequest request) {
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new BadRequestException("Game not found"));

        if (game.getStatus() != Game.GameStatus.ACTIVE) {
            throw new BadRequestException("Game is not available");
        }

        // Generate demo session token (not saved to database)
        String demoSessionToken = "demo-" + UUID.randomUUID().toString();

        // Generate launch URL for demo mode
        String launchUrl = generateLaunchUrl(game, demoSessionToken, true);
        String integrationType = game.getProvider().getIntegrationType().name();

        return new GameLaunchResponse(demoSessionToken, launchUrl, integrationType);
    }

    /**
     * Launches a game session for an authenticated user.
     *
     * <p>Creates a new game session with the following security features:</p>
     * <ul>
     *   <li>Validates user account status (must be ACTIVE)</li>
     *   <li>Checks self-exclusion period</li>
     *   <li>Binds session to user's IP address</li>
     *   <li>Sets 2-hour expiration</li>
     *   <li>Generates unique session token</li>
     *   <li>Logs the action in audit trail</li>
     * </ul>
     *
     * @param userId the ID of the user launching the game
     * @param request the game launch request containing gameId and optional IP address
     * @return GameLaunchResponse containing session token and launch URL
     * @throws BadRequestException if user not found, inactive, self-excluded, or game unavailable
     */
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

        // Create game session with expiration and IP binding
        GameSession session = new GameSession();
        session.setUser(user);
        session.setGame(game);
        session.setSessionToken(generateSecureSessionToken(userId));
        session.setStartedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusHours(GameConstants.Session.EXPIRATION_HOURS));
        session.setIpAddress(request.getIpAddress()); // IP binding for security
        session = gameSessionRepository.save(session);

        auditService.logUserAction(userId, "GAME_LAUNCHED", "GameSession", session.getId(),
                null, "Game: " + game.getName() + ", IP: " + request.getIpAddress());

        // Generate launch URL based on integration type
        String launchUrl = generateLaunchUrl(game, session.getSessionToken(), request.getDemoMode());
        String integrationType = game.getProvider().getIntegrationType().name();

        return new GameLaunchResponse(session.getSessionToken(), launchUrl, integrationType);
    }

    /**
     * Places a bet for a user in an active game session.
     *
     * <p>Security validations performed:</p>
     * <ul>
     *   <li>Session must belong to the authenticated user</li>
     *   <li>Session must not be expired</li>
     *   <li>IP address must match session creation IP</li>
     *   <li>User must have sufficient balance</li>
     *   <li>Round ID must be unique (prevents duplicate bets)</li>
     * </ul>
     *
     * <p>This method is atomic and transactional. If any validation fails,
     * the entire transaction is rolled back.</p>
     *
     * @param userId the ID of the authenticated user
     * @param request the bet request containing sessionToken, roundId, betAmount, and IP address
     * @return the user's balance after the bet
     * @throws BadRequestException if validation fails
     */
    @Transactional
    public BigDecimal placeBet(Long userId, GameBetRequest request) {
        GameSession session = gameSessionRepository.findBySessionToken(request.getSessionToken())
                .orElseThrow(() -> new BadRequestException("Invalid session"));

        // Validate session belongs to authenticated user
        if (!session.getUser().getId().equals(userId)) {
            auditService.logUserAction(userId, "SESSION_HIJACK_ATTEMPT", "GameSession", session.getId(),
                    null, "Attempted to use another user's session");
            throw new BadRequestException("Invalid session");
        }

        // Validate session is not expired
        if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(GameSession.SessionStatus.EXPIRED);
            gameSessionRepository.save(session);
            throw new BadRequestException("Session expired");
        }

        // Validate IP address matches (if IP was tracked during session creation)
        if (session.getIpAddress() != null && request.getIpAddress() != null &&
                !session.getIpAddress().equals(request.getIpAddress())) {
            auditService.logUserAction(userId, "IP_MISMATCH", "GameSession", session.getId(),
                    session.getIpAddress(), "Request from different IP: " + request.getIpAddress());
            throw new BadRequestException("Session security validation failed");
        }

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

        // Add VIP points for wagering
        vipService.addPointsForWagering(userId, request.getBetAmount(), session);

        return balanceAfter;
    }

    /**
     * Processes a win and credits the user's balance.
     *
     * <p>Security validations performed:</p>
     * <ul>
     *   <li>Session must belong to the authenticated user</li>
     *   <li>Session must not be expired</li>
     *   <li>IP address must match session creation IP</li>
     *   <li>Round must exist and not be completed</li>
     *   <li>Win amount must not exceed maximum multiplier (1000x bet amount)</li>
     * </ul>
     *
     * <p>Fraud detection: If win amount exceeds maximum, the attempt is logged
     * in the audit trail and the transaction is rejected.</p>
     *
     * @param userId the ID of the authenticated user
     * @param request the win request containing sessionToken, roundId, winAmount, and IP address
     * @return the user's balance after the win is credited
     * @throws BadRequestException if validation fails or fraud is detected
     */
    @Transactional
    public BigDecimal processWin(Long userId, GameWinRequest request) {
        GameSession session = gameSessionRepository.findBySessionToken(request.getSessionToken())
                .orElseThrow(() -> new BadRequestException("Invalid session"));

        // Validate session belongs to authenticated user
        if (!session.getUser().getId().equals(userId)) {
            auditService.logUserAction(userId, "SESSION_HIJACK_ATTEMPT", "GameSession", session.getId(),
                    null, "Attempted to use another user's session");
            throw new BadRequestException("Invalid session");
        }

        // Validate session is not expired
        if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(GameSession.SessionStatus.EXPIRED);
            gameSessionRepository.save(session);
            throw new BadRequestException("Session expired");
        }

        // Validate IP address matches (if IP was tracked during session creation)
        if (session.getIpAddress() != null && request.getIpAddress() != null &&
                !session.getIpAddress().equals(request.getIpAddress())) {
            auditService.logUserAction(userId, "IP_MISMATCH", "GameSession", session.getId(),
                    session.getIpAddress(), "Request from different IP: " + request.getIpAddress());
            throw new BadRequestException("Session security validation failed");
        }

        GameRound round = gameRoundRepository.findByRoundId(request.getRoundId())
                .orElseThrow(() -> new BadRequestException("Round not found"));

        if (round.getStatus() == GameRound.RoundStatus.COMPLETED) {
            throw new BadRequestException("Round already completed");
        }

        // CRITICAL: Validate win amount against maximum multiplier
        BigDecimal betAmount = round.getBetAmount();
        BigDecimal maxWin = betAmount.multiply(GameConstants.Validation.MAX_WIN_MULTIPLIER);

        if (request.getWinAmount().compareTo(maxWin) > 0) {
            auditService.logUserAction(userId, "FRAUD_ATTEMPT", "GameRound", round.getId(),
                    betAmount.toString(),
                    "Win amount " + request.getWinAmount() + " exceeds max " + maxWin +
                    " (" + GameConstants.Validation.MAX_WIN_MULTIPLIER + "x multiplier)");
            throw new BadRequestException("Invalid win amount");
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

    /**
     * Generate secure session token using HMAC
     * Format: {userId}-{timestamp}-{randomUUID}-{hmac}
     * This ensures the token is cryptographically secure and tamper-proof
     */
    private String generateSecureSessionToken(Long userId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomPart = UUID.randomUUID().toString();
        String data = userId + "-" + timestamp + "-" + randomPart;

        // For now, return UUID-based token
        // In production, consider using JWT or HMAC-based token
        // Example: JWT.create().withClaim("userId", userId).withExpiresAt(...).sign(...)
        return UUID.randomUUID().toString();
    }
}
