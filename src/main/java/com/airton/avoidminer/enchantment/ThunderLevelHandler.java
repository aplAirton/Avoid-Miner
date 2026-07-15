package com.airton.avoidminer.enchantment;

import com.airton.avoidminer.AvoidMiner;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEnchantItemEvent;

@EventBusSubscriber(modid = AvoidMiner.MODID)
public final class ThunderLevelHandler {
    private static final ResourceKey<Enchantment> THUNDER = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(AvoidMiner.MODID, "thor_thunder")
    );

    private ThunderLevelHandler() {
    }

    @SubscribeEvent
    public static void onPlayerEnchantItem(PlayerEnchantItemEvent event) {
        event.getEnchantments().stream()
                .filter(instance -> instance.enchantment().is(THUNDER) && instance.level() == 1)
                .findFirst()
                .ifPresent(instance -> EnchantmentHelper.updateEnchantments(
                        event.getEnchantedItem(),
                        enchantments -> enchantments.set(instance.enchantment(), 2)
                ));
    }
}
