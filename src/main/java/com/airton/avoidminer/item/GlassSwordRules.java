package com.airton.avoidminer.item;

import com.airton.avoidminer.AvoidMiner;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public final class GlassSwordRules {
    public static final float BASE_DAMAGE = 16.0F;
    public static final int CHARGE_TICKS = 20;
    public static final int WAVE_RANGE = (int) HypersonicCannonRules.TIER_1.range();
    public static final int SPECIAL_COOLDOWN_TICKS = 10;

    public static final ResourceKey<Enchantment> SONIC_CHARGE = ResourceKey.create(
            Registries.ENCHANTMENT, Identifier.parse("avoidminer:sonic_charge"));

    private GlassSwordRules() {
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
