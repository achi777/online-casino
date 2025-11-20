package com.casino.constants;

import java.time.Duration;

/**
 * Security-related constants for authentication and authorization.
 *
 * <p>This class contains constants for JWT token management, password policies,
 * and session security settings.</p>
 *
 * @author Casino Platform
 * @version 1.0
 * @since 2025-11-19
 */
public final class SecurityConstants {

    private SecurityConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * JWT Token Configuration
     */
    public static final class JWT {
        private JWT() {}

        /**
         * Access token expiration time (24 hours in milliseconds).
         */
        public static final long ACCESS_TOKEN_EXPIRATION_MS = 86400000L; // 24 hours

        /**
         * Refresh token expiration time (7 days in milliseconds).
         */
        public static final long REFRESH_TOKEN_EXPIRATION_MS = 604800000L; // 7 days

        /**
         * Access token expiration as Duration (for readability).
         */
        public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(24);

        /**
         * Refresh token expiration as Duration (for readability).
         */
        public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(7);

        /**
         * JWT token type claim value for access tokens.
         */
        public static final String TOKEN_TYPE_ACCESS = "ACCESS";

        /**
         * JWT token type claim value for refresh tokens.
         */
        public static final String TOKEN_TYPE_REFRESH = "REFRESH";
    }

    /**
     * Password Policy
     */
    public static final class Password {
        private Password() {}

        /**
         * BCrypt password encoding strength (cost factor).
         * Higher values are more secure but slower. Range: 4-31.
         */
        public static final int BCRYPT_STRENGTH = 12;

        /**
         * Minimum password length.
         */
        public static final int MIN_LENGTH = 8;

        /**
         * Maximum password length.
         */
        public static final int MAX_LENGTH = 128;
    }

    /**
     * Session Security
     */
    public static final class Session {
        private Session() {}

        /**
         * Maximum concurrent sessions per user.
         */
        public static final int MAX_CONCURRENT_SESSIONS = 5;

        /**
         * Session inactivity timeout (30 minutes).
         */
        public static final Duration INACTIVITY_TIMEOUT = Duration.ofMinutes(30);
    }

    /**
     * IP Address Validation
     */
    public static final class IPAddress {
        private IPAddress() {}

        /**
         * Maximum number of failed login attempts before temporary block.
         */
        public static final int MAX_FAILED_ATTEMPTS = 5;

        /**
         * Duration of temporary block after max failed attempts (15 minutes).
         */
        public static final Duration BLOCK_DURATION = Duration.ofMinutes(15);
    }
}
