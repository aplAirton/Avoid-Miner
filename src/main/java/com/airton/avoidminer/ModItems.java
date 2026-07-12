package com.airton.avoidminer;

import com.airton.avoidminer.item.GuideBookItem;
import com.airton.avoidminer.item.MachineUpgradeItem;
import com.airton.avoidminer.lootr.MobCardItem;
import com.airton.avoidminer.lootr.MobCardType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AvoidMiner.MODID);

    public static final DeferredItem<BlockItem> AVOID_PROCESSOR_TIER_1 = ITEMS.registerItem("avoid_processor_tier_1",
            props -> new BlockItem(ModBlocks.AVOID_PROCESSOR_TIER_1.get(), props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.processor_t1.desc"));
                    builder.accept(Component.translatable("tooltip.avoidminer.processor_t1.upgrades"));
                }
            });
    public static final DeferredItem<BlockItem> AVOID_PROCESSOR_TIER_2 = ITEMS.registerItem("avoid_processor_tier_2",
            props -> new BlockItem(ModBlocks.AVOID_PROCESSOR_TIER_2.get(), props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.processor_t2.desc"));
                    builder.accept(Component.translatable("tooltip.avoidminer.processor_t2.upgrades"));
                }
            });
    public static final DeferredItem<BlockItem> AVOID_PROCESSOR_TIER_3 = ITEMS.registerItem("avoid_processor_tier_3",
            props -> new BlockItem(ModBlocks.AVOID_PROCESSOR_TIER_3.get(), props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.processor_t3.desc"));
                    builder.accept(Component.translatable("tooltip.avoidminer.processor_t3.upgrades"));
                }
            });

    public static final DeferredItem<BlockItem> AVOID_MINER_TIER_1 = ITEMS.registerItem("avoid_miner_tier_1",
            props -> new BlockItem(ModBlocks.AVOID_MINER_TIER_1.get(), props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.machine_t1.efficiency"));
                    builder.accept(Component.translatable("tooltip.avoidminer.machine_t1.slots"));
                    builder.accept(Component.translatable("tooltip.avoidminer.machine.modes"));
                }
            });
    public static final DeferredItem<BlockItem> AVOID_MINER_TIER_2 = ITEMS.registerItem("avoid_miner_tier_2",
            props -> new BlockItem(ModBlocks.AVOID_MINER_TIER_2.get(), props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.machine_t2.efficiency"));
                    builder.accept(Component.translatable("tooltip.avoidminer.machine_t2.slots"));
                    builder.accept(Component.translatable("tooltip.avoidminer.machine.modes"));
                }
            });
    public static final DeferredItem<BlockItem> AVOID_MINER_TIER_3 = ITEMS.registerItem("avoid_miner_tier_3",
            props -> new BlockItem(ModBlocks.AVOID_MINER_TIER_3.get(), props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.machine_t3.efficiency"));
                    builder.accept(Component.translatable("tooltip.avoidminer.machine_t3.slots"));
                    builder.accept(Component.translatable("tooltip.avoidminer.machine.modes"));
                }
            });

    public static final DeferredItem<Item> MINING_CORE = ITEMS.registerItem("mining_core",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.mining_core"));
                }
            });

    public static final DeferredItem<Item> UPGRADE_TIER_2 = ITEMS.registerItem("upgrade_tier_2",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.upgrade_tier_2.obtain"));
                }
            });
    public static final DeferredItem<Item> UPGRADE_TIER_3 = ITEMS.registerItem("upgrade_tier_3",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.upgrade_tier_3.obtain"));
                }
            });

    public static final DeferredItem<Item> MINING_UPGRADE_TIER_1 = ITEMS.registerItem("mining_upgrade_tier_1",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.mining_upgrade_tier_1"));
    public static final DeferredItem<Item> MINING_UPGRADE_TIER_2 = ITEMS.registerItem("mining_upgrade_tier_2",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.mining_upgrade_tier_2"));
    public static final DeferredItem<Item> MINING_UPGRADE_TIER_3 = ITEMS.registerItem("mining_upgrade_tier_3",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.mining_upgrade_tier_3"));

    public static final DeferredItem<Item> ENERGY_UPGRADE_TIER_1 = ITEMS.registerItem("energy_upgrade_tier_1",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.energy_upgrade_tier_1"));
    public static final DeferredItem<Item> ENERGY_UPGRADE_TIER_2 = ITEMS.registerItem("energy_upgrade_tier_2",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.energy_upgrade_tier_2"));
    public static final DeferredItem<Item> ENERGY_UPGRADE_TIER_3 = ITEMS.registerItem("energy_upgrade_tier_3",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.energy_upgrade_tier_3"));

    public static final DeferredItem<Item> SPEED_UPGRADE_TIER_1 = ITEMS.registerItem("speed_upgrade_tier_1",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.speed_upgrade_tier_1"));
    public static final DeferredItem<Item> SPEED_UPGRADE_TIER_2 = ITEMS.registerItem("speed_upgrade_tier_2",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.speed_upgrade_tier_2"));
    public static final DeferredItem<Item> SPEED_UPGRADE_TIER_3 = ITEMS.registerItem("speed_upgrade_tier_3",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.speed_upgrade_tier_3"));

    public static final DeferredItem<Item> BOTANY_UPGRADE = ITEMS.registerItem("botany_upgrade",
            props -> new MachineUpgradeItem(props,
                    "tooltip.avoidminer.botany_upgrade.mode",
                    "tooltip.avoidminer.botany_upgrade.output",
                    "tooltip.avoidminer.world_slot"));
    public static final DeferredItem<Item> NETHER_UPGRADE = ITEMS.registerItem("nether_upgrade",
            props -> new MachineUpgradeItem(props,
                    "tooltip.avoidminer.nether_upgrade.mode",
                    "tooltip.avoidminer.nether_upgrade.output",
                    "tooltip.avoidminer.world_slot"));
    public static final DeferredItem<Item> FORTUNE_UPGRADE = ITEMS.registerItem("fortune_upgrade",
            props -> new MachineUpgradeItem(props,
                    "tooltip.avoidminer.fortune_upgrade.applies",
                    "tooltip.avoidminer.fortune_upgrade.output",
                    "tooltip.avoidminer.enchant_slot"));
    public static final DeferredItem<Item> SILK_UPGRADE = ITEMS.registerItem("silk_upgrade",
            props -> new MachineUpgradeItem(props,
                    "tooltip.avoidminer.silk_upgrade.applies",
                    "tooltip.avoidminer.silk_upgrade.output",
                    "tooltip.avoidminer.enchant_slot"));

    public static final DeferredItem<BlockItem> MAGNETITE_ORE = ITEMS.registerSimpleBlockItem(ModBlocks.MAGNETITE_ORE);
    public static final DeferredItem<BlockItem> RAW_MAGNETITE_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.RAW_MAGNETITE_BLOCK);
    public static final DeferredItem<Item> RAW_MAGNETITE = ITEMS.registerSimpleItem("raw_magnetite");
    public static final DeferredItem<Item> MAGNETITE_INGOT = ITEMS.registerSimpleItem("magnetite_ingot");
    public static final DeferredItem<Item> MAGNETITE_NUGGET = ITEMS.registerSimpleItem("magnetite_nugget");

    public static final DeferredItem<Item> PROCESSING_CORE = ITEMS.registerItem("processing_core",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.processing_core"));
                }
            });

    public static final DeferredItem<Item> BLANK_UPGRADE_PATTERN = ITEMS.registerSimpleItem("blank_upgrade_pattern");

    // Cartões de mob - um item por MobCardType. Cada cartão rastreia kills via
    // CustomData (Kills, 0..requiredKills). Quando completo, ganha foil (encantado).
    public static final DeferredItem<MobCardItem> SKELETON_CARD = ITEMS.registerItem("skeleton_card",
            props -> new MobCardItem(MobCardType.SKELETON, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> WITHER_SKELETON_CARD = ITEMS.registerItem("wither_skeleton_card",
            props -> new MobCardItem(MobCardType.WITHER_SKELETON, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> CREEPER_CARD = ITEMS.registerItem("creeper_card",
            props -> new MobCardItem(MobCardType.CREEPER, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> ZOMBIE_CARD = ITEMS.registerItem("zombie_card",
            props -> new MobCardItem(MobCardType.ZOMBIE, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> SPIDER_CARD = ITEMS.registerItem("spider_card",
            props -> new MobCardItem(MobCardType.SPIDER, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> WITCH_CARD = ITEMS.registerItem("witch_card",
            props -> new MobCardItem(MobCardType.WITCH, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> PIGLIN_CARD = ITEMS.registerItem("piglin_card",
            props -> new MobCardItem(MobCardType.PIGLIN, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> PIGLIN_BRUTE_CARD = ITEMS.registerItem("piglin_brute_card",
            props -> new MobCardItem(MobCardType.PIGLIN_BRUTE, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> ENDERMAN_CARD = ITEMS.registerItem("enderman_card",
            props -> new MobCardItem(MobCardType.ENDERMAN, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> VILLAGER_CARD = ITEMS.registerItem("villager_card",
            props -> new MobCardItem(MobCardType.VILLAGER, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> BLAZE_CARD = ITEMS.registerItem("blaze_card",
            props -> new MobCardItem(MobCardType.BLAZE, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> PIG_CARD = ITEMS.registerItem("pig_card",
            props -> new MobCardItem(MobCardType.PIG, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> COW_CARD = ITEMS.registerItem("cow_card",
            props -> new MobCardItem(MobCardType.COW, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> CHICKEN_CARD = ITEMS.registerItem("chicken_card",
            props -> new MobCardItem(MobCardType.CHICKEN, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> SHEEP_CARD = ITEMS.registerItem("sheep_card",
            props -> new MobCardItem(MobCardType.SHEEP, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> VEX_CARD = ITEMS.registerItem("vex_card",
            props -> new MobCardItem(MobCardType.VEX, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> GUARDIAN_CARD = ITEMS.registerItem("guardian_card",
            props -> new MobCardItem(MobCardType.GUARDIAN, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> ELDER_GUARDIAN_CARD = ITEMS.registerItem("elder_guardian_card",
            props -> new MobCardItem(MobCardType.ELDER_GUARDIAN, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> SLIME_CARD = ITEMS.registerItem("slime_card",
            props -> new MobCardItem(MobCardType.SLIME, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> VINDICATOR_CARD = ITEMS.registerItem("vindicator_card",
            props -> new MobCardItem(MobCardType.VINDICATOR, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> EVOKER_CARD = ITEMS.registerItem("evoker_card",
            props -> new MobCardItem(MobCardType.EVOKER, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> GHAST_CARD = ITEMS.registerItem("ghast_card",
            props -> new MobCardItem(MobCardType.GHAST, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> MAGMA_CUBE_CARD = ITEMS.registerItem("magma_cube_card",
            props -> new MobCardItem(MobCardType.MAGMA_CUBE, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> SHULKER_CARD = ITEMS.registerItem("shulker_card",
            props -> new MobCardItem(MobCardType.SHULKER, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> ENDER_DRAGON_CARD = ITEMS.registerItem("ender_dragon_card",
            props -> new MobCardItem(MobCardType.ENDER_DRAGON, props.stacksTo(1)));

    // Referência ordenada usada pela HUD, JEI e máquina.
    public static final DeferredItem<?>[] MOB_CARDS = {
            SKELETON_CARD, WITHER_SKELETON_CARD, CREEPER_CARD, ZOMBIE_CARD,
            SPIDER_CARD, WITCH_CARD, PIGLIN_CARD, PIGLIN_BRUTE_CARD,
            ENDERMAN_CARD, VILLAGER_CARD, BLAZE_CARD, PIG_CARD, COW_CARD,
            CHICKEN_CARD, SHEEP_CARD, VEX_CARD, GUARDIAN_CARD, ELDER_GUARDIAN_CARD,
            SLIME_CARD, VINDICATOR_CARD, EVOKER_CARD, GHAST_CARD, MAGMA_CUBE_CARD,
            SHULKER_CARD, ENDER_DRAGON_CARD
    };

    public static final DeferredItem<Item> GUIDE_BOOK = ITEMS.registerItem("guide_book",
            props -> new GuideBookItem(props.stacksTo(1)));

    public static final DeferredItem<BlockItem> AVOID_LOOTR = ITEMS.registerSimpleBlockItem(ModBlocks.AVOID_LOOTR);

    // Upgrades específicos da Lootr
    public static final DeferredItem<Item> RARITY_UPGRADE = ITEMS.registerItem("rarity_upgrade",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.rarity_upgrade"));

    public static final DeferredItem<Item> LOOT_UPGRADE = ITEMS.registerItem("loot_upgrade",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.loot_upgrade"));

    /** Retorna o nível de saque (1/2/3) de um LOOT_UPGRADE, 0 se não for. */
    public static int lootUpgradeLevel(ItemStack stack) {
        if (stack.is(LOOT_UPGRADE.get())) return 3;
        return 0;
    }
}
