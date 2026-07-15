package com.airton.avoidminer.item;



public final class GlassSwordRules {
    public static final float BASE_DAMAGE = 16.0F;
    public static final int CHARGE_TICKS = 20;
    public static final int WAVE_RANGE = (int) HypersonicCannonRules.TIER_1.range();
    public static final int SPECIAL_COOLDOWN_TICKS = 10;

    private GlassSwordRules() {
    }

    public static boolean isCharged(int heldTicks) {
        return heldTicks >= CHARGE_TICKS;
    }

    public static int getChargeTicks(int enchantLevel) {
        return switch (enchantLevel) {
            case 1 -> (int) Math.ceil(CHARGE_TICKS * 0.8);
            case 2 -> (int) Math.ceil(CHARGE_TICKS * 0.7);
            case 3 -> (int) Math.ceil(CHARGE_TICKS * 0.5);
            default -> CHARGE_TICKS;
        };
    }
}
