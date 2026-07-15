package com.airton.avoidminer.enchantment;

public final class ThorThunderRules {
    public static final int CHARGE_TICKS = 10;
    public static final int COOLDOWN_TICKS = 40;
    public static final float DAMAGE = 10.0F;
    public static final double RANGE = 32.0;
    public static final double CHAIN_RADIUS = 8.0;
    public static final double ELECTRIC_CORE_DROP_CHANCE = 0.15;

    private ThorThunderRules() {
    }

    public static int additionalTargets(int level) {
        return switch (level) {
            case 2 -> 2;
            case 3 -> 4;
            default -> 0;
        };
    }

    public static int effectiveLevel(int enchantmentLevel) {
        return Math.clamp(enchantmentLevel, 1, 3);
    }
}
