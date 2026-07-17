package com.airton.avoidminer.item;

public final class ResonantMiningRules {
    public static final int RESONANT_PICKAXE_DURABILITY = 3_000;
    public static final int NATIVE_FORWARD_RANGE = 4;
    public static final int SIDE_RADIUS = 2;
    public static final int CHARGE_TICKS = 20;
    public static final int COOLDOWN_TICKS = 40;
    public static final int BLOCKS_PER_DURABILITY = 2;
    public static final int LAVA_STABILIZATION_TICKS = 40;
    public static final int LAVA_RECHECK_INTERVAL = 5;

    private ResonantMiningRules() {
    }

    public static int enchantmentRange(int level) {
        return switch (Math.clamp(level, 0, 3)) {
            case 1 -> 4;
            case 2 -> 8;
            case 3 -> 16;
            default -> 0;
        };
    }

    public static int forwardRange(boolean resonantPickaxe, int enchantmentLevel) {
        return (resonantPickaxe ? NATIVE_FORWARD_RANGE : 0) + enchantmentRange(enchantmentLevel);
    }
}
