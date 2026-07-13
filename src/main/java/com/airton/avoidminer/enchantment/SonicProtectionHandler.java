package com.airton.avoidminer.enchantment;

import com.airton.avoidminer.AvoidMiner;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = AvoidMiner.MODID)
public final class SonicProtectionHandler {
    private static final TagKey<DamageType> SONIC_BOOM = TagKey.create(
            Registries.DAMAGE_TYPE, Identifier.parse("avoidminer:is_sonic_boom"));
    private static final ResourceKey<Enchantment> SONIC_PROTECTION = ResourceKey.create(
            Registries.ENCHANTMENT, Identifier.parse("avoidminer:sonic_protection"));

    private SonicProtectionHandler() {
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!event.getSource().is(SONIC_BOOM)
                || !event.getSource().is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
            return;
        }

        int[] totalLevels = {0};
        EnchantmentHelper.runIterationOnEquipment(event.getEntity(), (enchantment, level, item) -> {
            if (enchantment.is(SONIC_PROTECTION)) {
                totalLevels[0] += level;
            }
        });
        if (totalLevels[0] > 0) {
            event.setNewDamage(SonicProtectionRules.reduceDamage(event.getNewDamage(), totalLevels[0]));
        }
    }
}
