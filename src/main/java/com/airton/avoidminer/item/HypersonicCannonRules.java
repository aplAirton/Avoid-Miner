package com.airton.avoidminer.item;

public final class HypersonicCannonRules {
    public static final int COOLDOWN_TICKS = 20 * 10;
    public static final int CHARGE_TICKS = 34;
    public static final double RANGE = 15.0;
    public static final float DAMAGE = 10.0F;
    public static final double HORIZONTAL_KNOCKBACK = 2.5;
    public static final double VERTICAL_KNOCKBACK = 0.5;

    private HypersonicCannonRules() {
    }

    public static int particleCount(double distance) {
        return (int) Math.floor(distance) + 6;
    }

    public static double adjustedKnockback(double strength, double resistance) {
        return strength * (1.0 - resistance);
    }
}
