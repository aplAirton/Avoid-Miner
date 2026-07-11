package com.airton.avoidminer;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AvoidMiner.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> AVOID_MINER_TAB = TABS.register("avoid_miner_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.avoidminer"))
                    .icon(() -> new ItemStack(ModItems.AVOID_MINER_TIER_3.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.AVOID_PROCESSOR_TIER_1.get());
                        output.accept(ModItems.AVOID_PROCESSOR_TIER_2.get());
                        output.accept(ModItems.AVOID_PROCESSOR_TIER_3.get());
                        output.accept(ModItems.AVOID_MINER_TIER_1.get());
                        output.accept(ModItems.AVOID_MINER_TIER_2.get());
                        output.accept(ModItems.AVOID_MINER_TIER_3.get());
                        output.accept(ModItems.MINING_CORE.get());
                        output.accept(ModItems.UPGRADE_TIER_2.get());
                        output.accept(ModItems.UPGRADE_TIER_3.get());
                        output.accept(ModItems.MINING_UPGRADE_TIER_1.get());
                        output.accept(ModItems.MINING_UPGRADE_TIER_2.get());
                        output.accept(ModItems.MINING_UPGRADE_TIER_3.get());
                        output.accept(ModItems.ENERGY_UPGRADE_TIER_1.get());
                        output.accept(ModItems.ENERGY_UPGRADE_TIER_2.get());
                        output.accept(ModItems.ENERGY_UPGRADE_TIER_3.get());
                        output.accept(ModItems.SPEED_UPGRADE_TIER_1.get());
                        output.accept(ModItems.SPEED_UPGRADE_TIER_2.get());
                        output.accept(ModItems.SPEED_UPGRADE_TIER_3.get());
                        output.accept(ModItems.BOTANY_UPGRADE.get());
                        output.accept(ModItems.NETHER_UPGRADE.get());
                        output.accept(ModItems.FORTUNE_UPGRADE.get());
                        output.accept(ModItems.SILK_UPGRADE.get());
                        output.accept(ModItems.BLANK_UPGRADE_PATTERN.get());
                        output.accept(ModItems.GUIDE_BOOK.get());
                    })
                    .build());
}
