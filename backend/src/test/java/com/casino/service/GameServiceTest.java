package com.casino.service;

import com.casino.constants.GameConstants;
import com.casino.dto.GameBetRequest;
import com.casino.dto.GameLaunchRequest;
import com.casino.dto.GameLaunchResponse;
import com.casino.dto.GameWinRequest;
import com.casino.entity.*;
import com.casino.exception.BadRequestException;
import com.casino.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GameService}.
 *
 * <p>These tests verify:</p>
 * <ul>
 *   <li>Game session creation and validation</li>
 *   <li>Bet processing with security checks</li>
 *   <li>Win processing with fraud detection</li>
 *   <li>IP address validation</li>
 *   <li>Session expiration handling</li>
 * </ul>
 *
 * @author Casino Platform
 * @version 1.0
 * @since 2025-11-19
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GameService Tests")
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private GameRoundRepository gameRoundRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private GameService gameService;
    private AuditService auditService;
    private VIPService vipService;

    private User testUser;
    private Game testGame;
    private GameProvider testProvider;
    private GameSession testSession;

    @BeforeEach
    void setUp() {
        // Create stub implementations to avoid Java 23 + Mockito compatibility issues
        auditService = new AuditService(null) {
            @Override
            public void logUserAction(Long userId, String action, String entityType, Long entityId, String oldValue, String newValue) {
                // Stub implementation - do nothing
            }
        };

        vipService = new VIPService(null, null, null) {
            @Override
            public void addPointsForWagering(Long userId, java.math.BigDecimal wageringAmount, com.casino.entity.GameSession gameSession) {
                // Stub implementation - do nothing
            }
        };

        // Manually instantiate GameService with all dependencies
        gameService = new GameService(
            gameRepository,
            gameSessionRepository,
            gameRoundRepository,
            userRepository,
            transactionRepository,
            auditService,
            vipService
        );
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@casino.ge");
        testUser.setBalance(new BigDecimal("1000.00"));
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setKycStatus(User.KYCStatus.VERIFIED);
        testUser.setSelfExclusionUntil(null);
        testUser.setVipPoints(100);
        testUser.setTotalWagered(BigDecimal.ZERO);

        // Setup test provider
        testProvider = new GameProvider();
        testProvider.setId(1L);
        testProvider.setName("Test Provider");
        testProvider.setIntegrationType(GameProvider.IntegrationType.IFRAME);

        // Setup test game
        testGame = new Game();
        testGame.setId(1L);
        testGame.setName("Test Slot");
        testGame.setGameCode("TEST_SLOT_001");
        testGame.setCategory(Game.GameCategory.SLOTS);
        testGame.setStatus(Game.GameStatus.ACTIVE);
        testGame.setProvider(testProvider);
        testGame.setIframeUrl("http://localhost:8888/slots/test-slot/index.html");

        // Setup test session
        testSession = new GameSession();
        testSession.setId(1L);
        testSession.setUser(testUser);
        testSession.setGame(testGame);
        testSession.setSessionToken(UUID.randomUUID().toString());
        testSession.setStartedAt(LocalDateTime.now());
        testSession.setExpiresAt(LocalDateTime.now().plusHours(GameConstants.Session.EXPIRATION_HOURS));
        testSession.setIpAddress("192.168.1.1");
        testSession.setStatus(GameSession.SessionStatus.ACTIVE);
        testSession.setTotalBet(BigDecimal.ZERO);
        testSession.setTotalWin(BigDecimal.ZERO);
        testSession.setRoundsPlayed(0);
    }

    // ===== Game Launch Tests =====

    @Test
    @DisplayName("Should successfully launch game for active user")
    void shouldLaunchGameSuccessfully() {
        // Given
        GameLaunchRequest request = new GameLaunchRequest();
        request.setGameId(1L);
        request.setDemoMode(false);
        request.setIpAddress("192.168.1.1");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);

        // When
        GameLaunchResponse response = gameService.launchGame(1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSessionToken()).isNotNull();
        assertThat(response.getLaunchUrl()).contains("test-slot");
        verify(gameSessionRepository).save(any(GameSession.class));
    }

    @Test
    @DisplayName("Should reject launch for inactive user")
    void shouldRejectLaunchForInactiveUser() {
        // Given
        testUser.setStatus(User.UserStatus.SUSPENDED);
        GameLaunchRequest request = new GameLaunchRequest();
        request.setGameId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> gameService.launchGame(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not active");
    }

    @Test
    @DisplayName("Should reject launch for self-excluded user")
    void shouldRejectLaunchForSelfExcludedUser() {
        // Given
        testUser.setSelfExclusionUntil(LocalDateTime.now().plusDays(30));
        GameLaunchRequest request = new GameLaunchRequest();
        request.setGameId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> gameService.launchGame(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("self-exclusion");
    }

    // ===== Bet Processing Tests =====

    @Test
    @DisplayName("Should successfully process bet")
    void shouldProcessBetSuccessfully() {
        // Given
        GameBetRequest request = new GameBetRequest();
        request.setSessionToken(testSession.getSessionToken());
        request.setRoundId("ROUND_001");
        request.setBetAmount(new BigDecimal("10.00"));
        request.setIpAddress("192.168.1.1");

        when(gameSessionRepository.findBySessionToken(testSession.getSessionToken()))
                .thenReturn(Optional.of(testSession));
        when(gameRoundRepository.findByRoundId("ROUND_001")).thenReturn(Optional.empty());
        when(gameRoundRepository.save(any(GameRound.class))).thenReturn(new GameRound());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);

        // When
        BigDecimal balanceAfter = gameService.placeBet(1L, request);

        // Then
        assertThat(balanceAfter).isEqualByComparingTo("990.00");
        verify(gameRoundRepository).save(any(GameRound.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should reject bet for insufficient balance")
    void shouldRejectBetForInsufficientBalance() {
        // Given
        testUser.setBalance(new BigDecimal("5.00"));
        GameBetRequest request = new GameBetRequest();
        request.setSessionToken(testSession.getSessionToken());
        request.setRoundId("ROUND_001");
        request.setBetAmount(new BigDecimal("10.00"));
        request.setIpAddress("192.168.1.1");

        when(gameSessionRepository.findBySessionToken(testSession.getSessionToken()))
                .thenReturn(Optional.of(testSession));

        // When & Then
        assertThatThrownBy(() -> gameService.placeBet(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    @DisplayName("Should reject bet for expired session")
    void shouldRejectBetForExpiredSession() {
        // Given
        testSession.setExpiresAt(LocalDateTime.now().minusHours(1));
        GameBetRequest request = new GameBetRequest();
        request.setSessionToken(testSession.getSessionToken());
        request.setRoundId("ROUND_001");
        request.setBetAmount(new BigDecimal("10.00"));
        request.setIpAddress("192.168.1.1");

        when(gameSessionRepository.findBySessionToken(testSession.getSessionToken()))
                .thenReturn(Optional.of(testSession));

        // When & Then
        assertThatThrownBy(() -> gameService.placeBet(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("Should reject bet for IP mismatch")
    void shouldRejectBetForIpMismatch() {
        // Given
        GameBetRequest request = new GameBetRequest();
        request.setSessionToken(testSession.getSessionToken());
        request.setRoundId("ROUND_001");
        request.setBetAmount(new BigDecimal("10.00"));
        request.setIpAddress("192.168.1.99"); // Different IP

        when(gameSessionRepository.findBySessionToken(testSession.getSessionToken()))
                .thenReturn(Optional.of(testSession));

        // When & Then
        assertThatThrownBy(() -> gameService.placeBet(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("security validation failed");
    }

    @Test
    @DisplayName("Should reject bet for wrong user")
    void shouldRejectBetForWrongUser() {
        // Given
        GameBetRequest request = new GameBetRequest();
        request.setSessionToken(testSession.getSessionToken());
        request.setRoundId("ROUND_001");
        request.setBetAmount(new BigDecimal("10.00"));
        request.setIpAddress("192.168.1.1");

        when(gameSessionRepository.findBySessionToken(testSession.getSessionToken()))
                .thenReturn(Optional.of(testSession));

        // When & Then (user ID 999 instead of 1)
        assertThatThrownBy(() -> gameService.placeBet(999L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid session");
    }

    // ===== Win Processing Tests =====

    @Test
    @DisplayName("Should successfully process win")
    void shouldProcessWinSuccessfully() {
        // Given
        GameRound round = new GameRound();
        round.setId(1L);
        round.setSession(testSession);
        round.setRoundId("ROUND_001");
        round.setBetAmount(new BigDecimal("10.00"));
        round.setWinAmount(BigDecimal.ZERO);
        round.setStatus(GameRound.RoundStatus.PENDING);

        GameWinRequest request = new GameWinRequest();
        request.setSessionToken(testSession.getSessionToken());
        request.setRoundId("ROUND_001");
        request.setWinAmount(new BigDecimal("50.00")); // 5x win
        request.setIpAddress("192.168.1.1");

        when(gameSessionRepository.findBySessionToken(testSession.getSessionToken()))
                .thenReturn(Optional.of(testSession));
        when(gameRoundRepository.findByRoundId("ROUND_001")).thenReturn(Optional.of(round));
        when(gameRoundRepository.save(any(GameRound.class))).thenReturn(round);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);

        // When
        BigDecimal balanceAfter = gameService.processWin(1L, request);

        // Then
        assertThat(balanceAfter).isEqualByComparingTo("1050.00");
        verify(gameRoundRepository).save(any(GameRound.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should reject excessive win amount (fraud detection)")
    void shouldRejectExcessiveWinAmount() {
        // Given
        GameRound round = new GameRound();
        round.setId(1L);
        round.setSession(testSession);
        round.setRoundId("ROUND_001");
        round.setBetAmount(new BigDecimal("10.00"));
        round.setStatus(GameRound.RoundStatus.PENDING);

        GameWinRequest request = new GameWinRequest();
        request.setSessionToken(testSession.getSessionToken());
        request.setRoundId("ROUND_001");
        request.setWinAmount(new BigDecimal("20000.00")); // 2000x - exceeds max 1000x
        request.setIpAddress("192.168.1.1");

        when(gameSessionRepository.findBySessionToken(testSession.getSessionToken()))
                .thenReturn(Optional.of(testSession));
        when(gameRoundRepository.findByRoundId("ROUND_001")).thenReturn(Optional.of(round));

        // When & Then
        assertThatThrownBy(() -> gameService.processWin(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid win amount");
    }

    @Test
    @DisplayName("Should accept maximum allowed win (1000x)")
    void shouldAcceptMaximumAllowedWin() {
        // Given
        GameRound round = new GameRound();
        round.setId(1L);
        round.setSession(testSession);
        round.setRoundId("ROUND_001");
        round.setBetAmount(new BigDecimal("10.00"));
        round.setStatus(GameRound.RoundStatus.PENDING);

        GameWinRequest request = new GameWinRequest();
        request.setSessionToken(testSession.getSessionToken());
        request.setRoundId("ROUND_001");
        request.setWinAmount(new BigDecimal("10000.00")); // Exactly 1000x
        request.setIpAddress("192.168.1.1");

        when(gameSessionRepository.findBySessionToken(testSession.getSessionToken()))
                .thenReturn(Optional.of(testSession));
        when(gameRoundRepository.findByRoundId("ROUND_001")).thenReturn(Optional.of(round));
        when(gameRoundRepository.save(any(GameRound.class))).thenReturn(round);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(testSession);

        // When
        BigDecimal balanceAfter = gameService.processWin(1L, request);

        // Then
        assertThat(balanceAfter).isEqualByComparingTo("11000.00");
    }

    @Test
    @DisplayName("Should reject win for already completed round")
    void shouldRejectWinForCompletedRound() {
        // Given
        GameRound round = new GameRound();
        round.setId(1L);
        round.setSession(testSession);
        round.setRoundId("ROUND_001");
        round.setBetAmount(new BigDecimal("10.00"));
        round.setWinAmount(new BigDecimal("50.00"));
        round.setStatus(GameRound.RoundStatus.COMPLETED); // Already completed

        GameWinRequest request = new GameWinRequest();
        request.setSessionToken(testSession.getSessionToken());
        request.setRoundId("ROUND_001");
        request.setWinAmount(new BigDecimal("100.00"));
        request.setIpAddress("192.168.1.1");

        when(gameSessionRepository.findBySessionToken(testSession.getSessionToken()))
                .thenReturn(Optional.of(testSession));
        when(gameRoundRepository.findByRoundId("ROUND_001")).thenReturn(Optional.of(round));

        // When & Then
        assertThatThrownBy(() -> gameService.processWin(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already completed");
    }
}
