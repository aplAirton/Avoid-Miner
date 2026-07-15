package com.airton.avoidminer.enchantment;

public final class ResonanceEnchantmentRules {
    public static final int ECHO_DURATION_TICKS = 60;
    public static final double ECHO_RADIUS = 24.0;
    public static final int DISSONANCE_TICKS_PER_LEVEL = 50;
    public static final int DISSONANCE_MAX_LEVEL = 2;
    public static final int DISSONANCE_REDIRECT_INTERVAL_TICKS = 10;
    public static final int DISSONANCE_REDIRECT_RADIUS = 8;

    private ResonanceEnchantmentRules() {
    }

    public static int dissonanceDurationTicks(int level) {
        return Math.clamp(level, 1, DISSONANCE_MAX_LEVEL) * DISSONANCE_TICKS_PER_LEVEL;
    }
}
