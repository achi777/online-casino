package com.casino.constants;

import java.math.BigDecimal;

/**
 * Constants for VIP and loyalty program calculations.
 *
 * <p>This class defines point calculation rates and tier progression rules
 * for the VIP loyalty program.</p>
 *
 * @author Casino Platform
 * @version 1.0
 * @since 2025-11-19
 */
public final class VIPConstants {

    private VIPConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Points Calculation Rates
     */
    public static final class PointsRate {
        private PointsRate() {}

        /**
         * VIP points earned per 1 GEL wagered (1 point per 1 GEL).
         */
        public static final BigDecimal POINTS_PER_GEL_WAGERED = new BigDecimal("1");

        /**
         * VIP points earned per 1 GEL deposited (0.1 points per 1 GEL).
         */
        public static final BigDecimal POINTS_PER_GEL_DEPOSIT = new BigDecimal("0.1");
    }

    /**
     * VIP Tier Thresholds
     */
    public static final class TierThreshold {
        private TierThreshold() {}

        /**
         * Points required for Bronze tier (entry level).
         */
        public static final int BRONZE = 0;

        /**
         * Points required for Silver tier.
         */
        public static final int SILVER = 1000;

        /**
         * Points required for Gold tier.
         */
        public static final int GOLD = 5000;

        /**
         * Points required for Platinum tier.
         */
        public static final int PLATINUM = 20000;

        /**
         * Points required for Diamond tier (highest).
         */
        public static final int DIAMOND = 50000;
    }
}
