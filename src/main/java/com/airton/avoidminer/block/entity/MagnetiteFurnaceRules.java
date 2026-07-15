package com.airton.avoidminer.block.entity;

public final class MagnetiteFurnaceRules {
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

    public static int energyPerItem(int baseTicks, int energyUpgradeTier) {
        return Math.max(1, Math.round(baseTicks * energyMultiplier(energyUpgradeTier)));
    }
}
