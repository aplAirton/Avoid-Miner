package com.airton.avoidminer.enchantment;

public final class SonicProtectionRules {
    private SonicProtectionRules() {
    }

    public static float reduceDamage(float damage, int totalLevels) {
        float cappedProtection = Math.clamp(totalLevels, 0, 20);
        return damage * (1.0F - cappedProtection / 25.0F);
    }
}
