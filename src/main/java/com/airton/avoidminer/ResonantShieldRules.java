package com.airton.avoidminer;

public final class ResonantShieldRules {
    public static final int MAX_CHARGE = 5;
    public static final int DOUBLE_USE_WINDOW_TICKS = 8;
    public static final double BLAST_RANGE = 13.0;
    public static final double BLAST_HALF_ANGLE_DEGREES = 32.0;
    public static final int STUN_TICKS = 50;
    public static final float BLAST_DAMAGE = 6.0F;

    private ResonantShieldRules() {
    }

    public static int addCharge(int current) {
        return Math.min(MAX_CHARGE, Math.max(0, current) + 1);
    }

    public static boolean isReady(int charge) {
        return charge >= MAX_CHARGE;
    }

    public static boolean isDoubleUse(long previousTick, long currentTick) {
        long elapsed = currentTick - previousTick;
        return previousTick >= 0 && elapsed >= 1 && elapsed <= DOUBLE_USE_WINDOW_TICKS;
    }
}
