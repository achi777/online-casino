package com.casino.constants;

import java.math.BigDecimal;

/**
 * Constants for wallet and financial transaction operations.
 *
 * <p>This class defines limits, thresholds, and validation rules
 * for deposits, withdrawals, and balance management.</p>
 *
 * @author Casino Platform
 * @version 1.0
 * @since 2025-11-19
 */
public final class WalletConstants {

    private WalletConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Transaction Limits
     */
    public static final class Limits {
        private Limits() {}

        /**
         * Minimum deposit amount in GEL.
         */
        public static final BigDecimal MIN_DEPOSIT = new BigDecimal("10");

        /**
         * Maximum deposit amount in GEL (single transaction).
         */
        public static final BigDecimal MAX_DEPOSIT = new BigDecimal("10000");

        /**
         * Minimum withdrawal amount in GEL.
         */
        public static final BigDecimal MIN_WITHDRAWAL = new BigDecimal("10");

        /**
         * Maximum withdrawal amount in GEL (single transaction).
         */
        public static final BigDecimal MAX_WITHDRAWAL = new BigDecimal("50000");

        /**
         * Default daily deposit limit for new users (in GEL).
         */
        public static final BigDecimal DEFAULT_DAILY_DEPOSIT_LIMIT = new BigDecimal("5000");

        /**
         * Default weekly deposit limit for new users (in GEL).
         */
        public static final BigDecimal DEFAULT_WEEKLY_DEPOSIT_LIMIT = new BigDecimal("20000");

        /**
         * Default monthly deposit limit for new users (in GEL).
         */
        public static final BigDecimal DEFAULT_MONTHLY_DEPOSIT_LIMIT = new BigDecimal("50000");
    }

    /**
     * Transaction Processing
     */
    public static final class Processing {
        private Processing() {}

        /**
         * Number of decimal places for currency amounts (2 for GEL).
         */
        public static final int CURRENCY_SCALE = 2;

        /**
         * Minimum balance to allow withdrawal (prevents dust).
         */
        public static final BigDecimal MIN_BALANCE_FOR_WITHDRAWAL = new BigDecimal("0.01");
    }

    /**
     * KYC Requirements
     */
    public static final class KYC {
        private KYC() {}

        /**
         * Maximum withdrawal amount without KYC verification (in GEL).
         */
        public static final BigDecimal MAX_WITHDRAWAL_WITHOUT_KYC = new BigDecimal("0");

        /**
         * Cumulative deposit threshold that triggers mandatory KYC (in GEL).
         */
        public static final BigDecimal KYC_TRIGGER_AMOUNT = new BigDecimal("10000");
    }
}
