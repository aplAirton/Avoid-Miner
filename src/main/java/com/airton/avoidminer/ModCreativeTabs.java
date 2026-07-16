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
                        output.accept(ModItems.MAGNETITE_FURNACE_TIER_1.get());
                        output.accept(ModItems.MAGNETITE_FURNACE_TIER_2.get());
                        output.accept(ModItems.MAGNETITE_FURNACE_TIER_3.get());
                        output.accept(ModItems.AVOID_MINER_TIER_1.get());
                        output.accept(ModItems.AVOID_MINER_TIER_2.get());
                        output.accept(ModItems.AVOID_MINER_TIER_3.get());
                        output.accept(ModItems.MAGNETITE_ORE.get());
                        output.accept(ModItems.RAW_MAGNETITE_BLOCK.get());
                        output.accept(ModItems.MAGNETITE_BLOCK.get());
                        output.accept(ModItems.RAW_MAGNETITE.get());
                        output.accept(ModItems.MAGNETITE_INGOT.get());
                        output.accept(ModItems.MAGNETITE_NUGGET.get());
                        output.accept(ModItems.MAGNETITE_POWDER.get());
                        output.accept(ModItems.HYPERSONIC_CANNON.get());
                        output.accept(ModItems.HYPERSONIC_CANNON_TIER_2.get());
                        output.accept(ModItems.HYPERSONIC_CANNON_TIER_3.get());
                        output.accept(ModItems.HYPERSONIC_CANNON_CREATIVE.get());
                        output.accept(ModItems.WARDEN_HEART.get());
                        output.accept(ModItems.TEMPORAL_FRAGMENT.get());
                        output.accept(ModItems.TEMPORAL_CORE.get());
                        output.accept(ModItems.TEMPORAL_CLOCK.get());
                        output.accept(ModItems.TEMPORAL_CLOCK_CREATIVE.get());
                        output.accept(ModItems.TEMPORAL_SLOW_CLOCK.get());
                        output.accept(ModItems.TEMPORAL_SLOW_CLOCK_CREATIVE.get());
                        output.accept(ModItems.HYPERSONIC_SMITHING_TEMPLATE.get());
                        output.accept(ModItems.GLASS_SWORD.get());
                        output.accept(ModItems.RESONANT_PICKAXE.get());
                        output.accept(ModItems.RESONANT_RETALIATION_SHIELD.get());
                        output.accept(ModItems.SEISMIC_PROPULSION_BOOTS.get());
                        output.accept(ModItems.NIGHT_VISION_HELMET.get());
                        output.accept(ModItems.THOR_HAMMER.get());
                        output.accept(ModItems.ELECTRIC_CORE.get());
                        output.accept(ModItems.MINING_CORE.get());
                        output.accept(ModItems.PROCESSING_CORE.get());
                        output.accept(ModItems.TRIAL_SPAWNER_CORE.get());
                        output.accept(ModItems.VAULT_CORE.get());
                        output.accept(ModItems.OMINOUS_VAULT_CORE.get());
                        output.accept(ModItems.CONFIGURED_TRIAL_SPAWNER.get());
                        output.accept(ModItems.CONFIGURED_VAULT.get());
                        output.accept(ModItems.CONFIGURED_OMINOUS_VAULT.get());
                        for (var card : ModItems.MOB_CARDS) {
                            output.accept(card.get());
                        }
                        output.accept(ModItems.RARITY_UPGRADE.get());
                        output.accept(ModItems.LOOT_UPGRADE.get());
                        output.accept(ModBlocks.AVOID_LOOTR.get());
                        output.accept(ModItems.BATTERY.get());
                        output.accept(ModItems.CREATIVE_BATTERY.get());
                        output.accept(ModItems.ENERGY_LINK.get());
                        output.accept(ModItems.MAGNETITE_BARREL.get());
                        output.accept(ModItems.XP_STORAGE_CORE.get());
                        output.accept(ModItems.XP_VAULT.get());
                        output.accept(ModItems.ITEM_ABSORB_UPGRADE.get());
                        output.accept(ModItems.XP_ABSORB_UPGRADE.get());
                        output.accept(ModItems.ITEM_STACKING_UPGRADE.get());
                        output.accept(ModItems.CAPACITY_UPGRADE_TIER_1.get());
                        output.accept(ModItems.CAPACITY_UPGRADE_TIER_2.get());
                        output.accept(ModItems.CAPACITY_UPGRADE_TIER_3.get());
                        output.accept(ModItems.RANGE_UPGRADE_TIER_1.get());
                        output.accept(ModItems.RANGE_UPGRADE_TIER_2.get());
                        output.accept(ModItems.RANGE_UPGRADE_TIER_3.get());
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
                        output.accept(ModItems.END_UPGRADE.get());
                        output.accept(ModItems.DEEP_DARK_UPGRADE.get());
                        output.accept(ModItems.OCEAN_UPGRADE.get());
                        output.accept(ModItems.FORTUNE_UPGRADE.get());
                        output.accept(ModItems.SILK_UPGRADE.get());
                        output.accept(ModItems.BLANK_UPGRADE_PATTERN.get());
                        output.accept(ModItems.BLANK_CARD.get());
                        output.accept(ModItems.GUIDE_BOOK.get());
                    })
                    .build());
}
