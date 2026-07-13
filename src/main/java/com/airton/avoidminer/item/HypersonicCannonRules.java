package com.airton.avoidminer.item;

public final class HypersonicCannonRules {
    public static final CannonTier TIER_1 = new CannonTier(1, 200, 34, 15.0, 10.0F, 10.0F, 0.0, 1, 0, 0.0);
    public static final CannonTier TIER_2 = new CannonTier(2, 160, 30, 20.0, 14.0F, 7.0F, 7.0, 4, 0, 0.0);
    public static final CannonTier TIER_3 = new CannonTier(3, 120, 26, 26.0, 18.0F, 9.0F, 12.0, 8, 2, 8.0);

    private HypersonicCannonRules() {
    }

    public static CannonTier forTier(int tier) {
        return switch (tier) {
            case 2 -> TIER_2;
            case 3 -> TIER_3;
            default -> TIER_1;
        };
    }

    public static int particleCount(double distance) {
        return (int) Math.floor(distance) + 6;
    }

    public static double adjustedKnockback(double strength, double resistance) {
        return strength * (1.0 - resistance);
    }

    public static float damageAtDistance(CannonTier tier, double distance) {
        double progress = Math.clamp(distance / tier.range(), 0.0, 1.0);
        return (float) (tier.maxDamage() + (tier.minDamage() - tier.maxDamage()) * progress);
    }

    public record CannonTier(int tier, int cooldownTicks, int chargeTicks, double range,
                             float maxDamage, float minDamage, double coneDegrees,
                             int maxTargets, int ricochets, double ricochetRange) {
    }
}
