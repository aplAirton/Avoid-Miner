package com.airton.avoidminer.enchantment;

public final class EnderCooldownRules {
    private EnderCooldownRules() {}

    public static int cooldownTicks(int level) {
        return switch (level) {
            case 3 -> 20;
            case 2 -> 30;
            default -> 60;
        };
    }
}
