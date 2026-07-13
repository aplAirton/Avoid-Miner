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
}
