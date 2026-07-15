package com.airton.avoidminer.block.entity;

public final class MagnetiteFurnaceRules {
    public static final int RAW_ORE_BLOCK_ENERGY_MULTIPLIER = 8;

    private MagnetiteFurnaceRules() {
    }

    public static float energyMultiplier(int upgradeTier) {
        return switch (upgradeTier) {
            case 1 -> 0.8F;
            case 2 -> 0.7F;
            case 3 -> 0.6F;
            default -> 1.0F;
        };
    }

    public static float speedMultiplier(int upgradeTier) {
        return switch (upgradeTier) {
            case 1 -> 1.5F;
            case 2 -> 1.7F;
            case 3 -> 2.0F;
            default -> 1.0F;
        };
    }

    public static int effectiveTicks(int baseTicks, int speedUpgradeTier) {
        return Math.max(1, (int) (baseTicks / speedMultiplier(speedUpgradeTier)));
    }

    public static int energyPerItem(int baseTicks, float baseEnergyPerTick,
                                    int energyUpgradeTier, boolean rawOreBlock) {
        int recipeMultiplier = rawOreBlock ? RAW_ORE_BLOCK_ENERGY_MULTIPLIER : 1;
        return Math.max(1, Math.round(baseTicks * baseEnergyPerTick
                * energyMultiplier(energyUpgradeTier) * recipeMultiplier));
    }
}
