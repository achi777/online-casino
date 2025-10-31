package com.casino.service;

import com.casino.dto.SpinRequest;
import com.casino.dto.SpinResponse;
import com.casino.entity.Game;
import com.casino.entity.GameRound;
import com.casino.entity.GameSession;
import com.casino.entity.User;
import com.casino.exception.BadRequestException;
import com.casino.repository.GameRepository;
import com.casino.repository.GameRoundRepository;
import com.casino.repository.GameSessionRepository;
import com.casino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotSpinService {

    private final GameSessionRepository gameSessionRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final GameRoundRepository gameRoundRepository;

    // Symbol definitions for 3-reel slot
    private static final List<String> SYMBOLS_3REEL = List.of("🍒", "🍋", "🍊", "🍇", "🍉", "⭐", "💎");

    // Symbol multipliers for 3-reel slot
    private static final Map<String, Integer> MULTIPLIERS_3REEL = Map.of(
            "💎", 10,
            "⭐", 8,
            "🍉", 5,
            "🍇", 4,
            "🍊", 3,
            "🍋", 2,
            "🍒", 2
    );

    // Symbol definitions for 5-reel slot
    private static final List<String> SYMBOLS_5REEL = List.of("💎", "⭐", "🍀", "🔔", "💰", "🎰", "7️⃣", "🎲", "🏆");

    // Symbol multipliers for 5-reel slot (5 of a kind)
    private static final Map<String, Integer> MULTIPLIERS_5REEL = Map.of(
            "💎", 100,
            "7️⃣", 75,
            "🏆", 60,
            "⭐", 50,
            "🎰", 40,
            "💰", 30,
            "🔔", 25,
            "🍀", 20,
            "🎲", 15
    );

    @Transactional
    public SpinResponse processSpin(Long userId, SpinRequest request) {
        // Validate session
        GameSession session = gameSessionRepository.findBySessionToken(request.getSessionToken())
                .orElseThrow(() -> new BadRequestException("Invalid session token"));

        // SECURITY: Validate session belongs to authenticated user
        if (!session.getUser().getId().equals(userId)) {
            log.error("Session hijack attempt: userId={}, sessionUserId={}", userId, session.getUser().getId());
            throw new BadRequestException("Invalid session");
        }

        // Validate session is not expired
        if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            session.setStatus(GameSession.SessionStatus.EXPIRED);
            gameSessionRepository.save(session);
            throw new BadRequestException("Session expired");
        }

        User user = session.getUser();
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new BadRequestException("Game not found"));

        // Check balance
        if (user.getBalance().compareTo(request.getBetAmount()) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        // Check if round already exists
        if (gameRoundRepository.findByRoundId(request.getRoundId()).isPresent()) {
            throw new BadRequestException("Round already exists");
        }

        // Deduct bet from balance
        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(request.getBetAmount());
        user.setBalance(balanceAfter);
        userRepository.save(user);

        // Generate spin results based on RTP
        SpinResponse spinResponse = generateSpinResult(game, request.getBetAmount(), user);

        // Create game round
        GameRound round = new GameRound();
        round.setSession(session);
        round.setRoundId(request.getRoundId());
        round.setBetAmount(request.getBetAmount());
        round.setWinAmount(spinResponse.getWinAmount());
        round.setBalanceBefore(balanceBefore);
        round.setBalanceAfter(spinResponse.getNewBalance());
        round.setStatus(GameRound.RoundStatus.COMPLETED);
        gameRoundRepository.save(round);

        // Update session stats
        session.setTotalBet(session.getTotalBet().add(request.getBetAmount()));
        session.setTotalWin(session.getTotalWin().add(spinResponse.getWinAmount()));
        session.setRoundsPlayed(session.getRoundsPlayed() + 1);
        gameSessionRepository.save(session);

        return spinResponse;
    }

    private SpinResponse generateSpinResult(Game game, BigDecimal betAmount, User user) {
        SpinResponse response = new SpinResponse();

        // Get RTP from game configuration
        double rtp = game.getRtp().doubleValue();

        // Determine if this spin should be a win based on RTP
        Random random = new Random();
        double randomValue = random.nextDouble() * 100;

        // Adjust win probability based on RTP
        // RTP of 96% means player should win back 96% of bets over time
        boolean shouldWin = randomValue < rtp;

        // Generate results based on game type
        String gameCode = game.getGameCode();
        List<String> results;
        BigDecimal winAmount = BigDecimal.ZERO;

        if (gameCode.contains("FRUIT_SLOT") || gameCode.contains("3")) {
            // 3-reel slot
            results = generate3ReelResult(shouldWin, rtp);
            winAmount = calculate3ReelWin(results, betAmount);
        } else {
            // 5-reel slot
            results = generate5ReelResult(shouldWin, rtp);
            winAmount = calculate5ReelWin(results, betAmount);
        }

        // Apply winAmount to user balance
        if (winAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal newBalance = user.getBalance().add(winAmount);
            user.setBalance(newBalance);
            userRepository.save(user);

            response.setWin(true);
            response.setWinAmount(winAmount);
            response.setNewBalance(newBalance);
            response.setMessage("You won " + winAmount + " ₾!");
        } else {
            response.setWin(false);
            response.setWinAmount(BigDecimal.ZERO);
            response.setNewBalance(user.getBalance());
            response.setMessage("Try again!");
        }

        response.setResults(results);
        return response;
    }

    private List<String> generate3ReelResult(boolean shouldWin, double rtp) {
        List<String> results = new ArrayList<>();
        Random random = new Random();

        if (shouldWin) {
            // Generate winning combination
            // Higher value symbols are rarer
            String winningSymbol;
            double symbolRoll = random.nextDouble() * 100;

            if (symbolRoll < 5) { // 5% - Diamond (highest)
                winningSymbol = "💎";
            } else if (symbolRoll < 15) { // 10% - Star
                winningSymbol = "⭐";
            } else if (symbolRoll < 30) { // 15% - Watermelon
                winningSymbol = "🍉";
            } else if (symbolRoll < 50) { // 20% - Grapes
                winningSymbol = "🍇";
            } else if (symbolRoll < 70) { // 20% - Orange
                winningSymbol = "🍊";
            } else if (symbolRoll < 85) { // 15% - Lemon
                winningSymbol = "🍋";
            } else { // 15% - Cherry
                winningSymbol = "🍒";
            }

            results.add(winningSymbol);
            results.add(winningSymbol);
            results.add(winningSymbol);
        } else {
            // Generate losing combination (random, non-matching)
            for (int i = 0; i < 3; i++) {
                results.add(SYMBOLS_3REEL.get(random.nextInt(SYMBOLS_3REEL.size())));
            }
            // Ensure it's not accidentally a winning combination
            while (results.get(0).equals(results.get(1)) && results.get(1).equals(results.get(2))) {
                results.set(2, SYMBOLS_3REEL.get(random.nextInt(SYMBOLS_3REEL.size())));
            }
        }

        return results;
    }

    private BigDecimal calculate3ReelWin(List<String> results, BigDecimal betAmount) {
        if (results.get(0).equals(results.get(1)) && results.get(1).equals(results.get(2))) {
            int multiplier = MULTIPLIERS_3REEL.getOrDefault(results.get(0), 2);
            return betAmount.multiply(BigDecimal.valueOf(multiplier));
        }
        return BigDecimal.ZERO;
    }

    private List<String> generate5ReelResult(boolean shouldWin, double rtp) {
        List<String> results = new ArrayList<>();
        Random random = new Random();

        if (shouldWin) {
            // Determine win type based on probability
            double winTypeRoll = random.nextDouble() * 100;

            if (winTypeRoll < 5) { // 5% - 5 of a kind
                String winningSymbol = selectWeighted5ReelSymbol(random);
                for (int i = 0; i < 5; i++) {
                    results.add(winningSymbol);
                }
            } else if (winTypeRoll < 20) { // 15% - 4 of a kind
                String winningSymbol = selectWeighted5ReelSymbol(random);
                for (int i = 0; i < 4; i++) {
                    results.add(winningSymbol);
                }
                // Add one different symbol
                String different;
                do {
                    different = SYMBOLS_5REEL.get(random.nextInt(SYMBOLS_5REEL.size()));
                } while (different.equals(winningSymbol));
                results.add(random.nextInt(5), different);
            } else { // 80% - 3 of a kind
                String winningSymbol = selectWeighted5ReelSymbol(random);
                for (int i = 0; i < 3; i++) {
                    results.add(winningSymbol);
                }
                // Add two different symbols
                for (int i = 0; i < 2; i++) {
                    results.add(SYMBOLS_5REEL.get(random.nextInt(SYMBOLS_5REEL.size())));
                }
                // Shuffle to randomize positions
                Collections.shuffle(results);
            }
        } else {
            // Generate losing combination
            for (int i = 0; i < 5; i++) {
                results.add(SYMBOLS_5REEL.get(random.nextInt(SYMBOLS_5REEL.size())));
            }
            // Ensure it's not accidentally a winning combination
            while (countMaxOccurrences(results) >= 3) {
                results.set(random.nextInt(5), SYMBOLS_5REEL.get(random.nextInt(SYMBOLS_5REEL.size())));
            }
        }

        return results;
    }

    private String selectWeighted5ReelSymbol(Random random) {
        // Weight symbols - rarer symbols have lower probability
        double roll = random.nextDouble() * 100;

        if (roll < 3) return "💎";      // 3%
        else if (roll < 8) return "7️⃣";   // 5%
        else if (roll < 15) return "🏆"; // 7%
        else if (roll < 25) return "⭐"; // 10%
        else if (roll < 38) return "🎰"; // 13%
        else if (roll < 53) return "💰"; // 15%
        else if (roll < 70) return "🔔"; // 17%
        else if (roll < 85) return "🍀"; // 15%
        else return "🎲";                 // 15%
    }

    private BigDecimal calculate5ReelWin(List<String> results, BigDecimal betAmount) {
        // Count occurrences of each symbol
        Map<String, Integer> counts = new HashMap<>();
        for (String symbol : results) {
            counts.put(symbol, counts.getOrDefault(symbol, 0) + 1);
        }

        // Find maximum count
        int maxCount = counts.values().stream().max(Integer::compareTo).orElse(0);

        if (maxCount >= 3) {
            // Find which symbol has the max count
            String winningSymbol = counts.entrySet().stream()
                    .filter(e -> e.getValue() == maxCount)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse("");

            if (maxCount == 5) {
                // 5 of a kind - full multiplier
                int multiplier = MULTIPLIERS_5REEL.getOrDefault(winningSymbol, 50);
                return betAmount.multiply(BigDecimal.valueOf(multiplier));
            } else if (maxCount == 4) {
                // 4 of a kind - 40% of full multiplier
                int multiplier = MULTIPLIERS_5REEL.getOrDefault(winningSymbol, 50);
                return betAmount.multiply(BigDecimal.valueOf(multiplier * 0.4));
            } else if (maxCount == 3) {
                // 3 of a kind - 10% of full multiplier
                int multiplier = MULTIPLIERS_5REEL.getOrDefault(winningSymbol, 50);
                return betAmount.multiply(BigDecimal.valueOf(multiplier * 0.1));
            }
        }

        return BigDecimal.ZERO;
    }

    private int countMaxOccurrences(List<String> list) {
        Map<String, Integer> counts = new HashMap<>();
        for (String item : list) {
            counts.put(item, counts.getOrDefault(item, 0) + 1);
        }
        return counts.values().stream().max(Integer::compareTo).orElse(0);
    }
}
