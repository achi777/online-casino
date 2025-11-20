package com.casino.constants;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Constants used throughout the game-related services.
 *
 * <p>This class contains all magic numbers and configuration values
 * used in game logic, session management, and validation rules.</p>
 *
 * @author Casino Platform
 * @version 1.0
 * @since 2025-11-19
 */
public final class GameConstants {

    private GameConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Session Management Constants
     */
    public static final class Session {
        private Session() {}

        /**
         * Duration after which a game session expires (2 hours).
         */
        public static final Duration EXPIRATION_DURATION = Duration.ofHours(2);

        /**
         * Session expiration in hours (for backward compatibility).
         */
        public static final int EXPIRATION_HOURS = 2;
    }

    /**
     * Game Validation Constants
     */
    public static final class Validation {
        private Validation() {}

        /**
         * Maximum win multiplier allowed per bet (1000x).
         * Example: If bet is 10₾, max win is 10,000₾.
         */
        public static final BigDecimal MAX_WIN_MULTIPLIER = new BigDecimal("1000");

        /**
         * Minimum bet amount in GEL.
         */
        public static final BigDecimal MIN_BET_AMOUNT = new BigDecimal("0.01");

        /**
         * Maximum bet amount in GEL (configurable per game).
         */
        public static final BigDecimal MAX_BET_AMOUNT = new BigDecimal("1000");
    }

    /**
     * Rate Limiting Constants
     */
    public static final class RateLimit {
        private RateLimit() {}

        /**
         * Maximum bet operations per second.
         */
        public static final int BET_OPERATIONS_PER_SECOND = 20;

        /**
         * Maximum slot spin operations per second.
         */
        public static final int SLOT_SPIN_PER_SECOND = 5;

        /**
         * Maximum general game operations per second.
         */
        public static final int GAME_OPERATIONS_PER_SECOND = 10;
    }
}
