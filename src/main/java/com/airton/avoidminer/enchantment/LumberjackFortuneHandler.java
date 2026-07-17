package com.airton.avoidminer.enchantment;

import com.airton.avoidminer.AvoidMiner;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

@EventBusSubscriber(modid = AvoidMiner.MODID)
public final class LumberjackFortuneHandler {
    public static final ResourceKey<Enchantment> LUMBERJACK_FORTUNE = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(AvoidMiner.MODID, "lumberjack_fortune"));

    private LumberjackFortuneHandler() {}

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        ItemStack tool = event.getTool();
        if (!event.getState().is(BlockTags.LOGS) || !tool.is(ItemTags.AXES)) return;

        int level = event.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(LUMBERJACK_FORTUNE)
                .map(holder -> tool.getEnchantments().getLevel(holder))
                .orElse(0);
        if (level <= 0) return;

        int fortuneRoll = event.getLevel().getRandom().nextInt(level + 2) - 1;
        int multiplier = Math.max(0, fortuneRoll) + 1;
        if (multiplier <= 1) return;

        event.getDrops().forEach(drop -> {
            ItemStack stack = drop.getItem();
            if (stack.is(ItemTags.LOGS)) {
                stack.grow(stack.getCount() * (multiplier - 1));
            }
        });
    }
}
