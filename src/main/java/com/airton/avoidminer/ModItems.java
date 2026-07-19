package com.airton.avoidminer;

import com.airton.avoidminer.item.GuideBookItem;
import com.airton.avoidminer.item.GlassSwordItem;
import com.airton.avoidminer.item.HypersonicCannonCreativeItem;
import com.airton.avoidminer.item.HypersonicCannonItem;
import com.airton.avoidminer.item.HypersonicSmithingTemplateItem;
import com.airton.avoidminer.item.TemporalClockCreativeItem;
import com.airton.avoidminer.item.MachineUpgradeItem;
import com.airton.avoidminer.item.NightVisionHelmetItem;
import com.airton.avoidminer.item.ResonantRetaliationShieldItem;
import com.airton.avoidminer.item.ResonantPickaxeItem;
import com.airton.avoidminer.item.ResonantScannerItem;
import com.airton.avoidminer.item.SeismicPropulsionBootsItem;
import com.airton.avoidminer.item.TemporalClockItem;
import com.airton.avoidminer.item.TemporalCoreItem;
import com.airton.avoidminer.item.TemporalFragmentItem;
import com.airton.avoidminer.item.TemporalSlowClockCreativeItem;
import com.airton.avoidminer.item.TemporalSlowClockItem;
import com.airton.avoidminer.item.ThorHammerItem;
import com.airton.avoidminer.item.WardenHeartItem;
import com.airton.avoidminer.item.ScrollOfRenewalItem;
import com.airton.avoidminer.item.HeroMedallionItem;
import com.airton.avoidminer.item.ScrollOfForgetfulnessItem;
import com.airton.avoidminer.item.RangeCardItem;
import com.airton.avoidminer.item.VillagerLassoItem;
import com.airton.avoidminer.item.ZombiePoisonItem;
import com.airton.avoidminer.item.ConfiguredTrialSpawnerItem;
import com.airton.avoidminer.item.ConfiguredVaultItem;
import com.airton.avoidminer.lootr.MobCardItem;
import com.airton.avoidminer.lootr.MobCardType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
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
                    builder.accept(Component.translatable("tooltip.avoidminer.processor_t1.desc").withStyle(ChatFormatting.GRAY));
                    builder.accept(Component.translatable("tooltip.avoidminer.processor_t1.upgrades").withStyle(ChatFormatting.GRAY));
                }
            });
    public static final DeferredItem<BlockItem> AVOID_PROCESSOR_TIER_2 = ITEMS.registerItem("avoid_processor_tier_2",
            props -> new BlockItem(ModBlocks.AVOID_PROCESSOR_TIER_2.get(), props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.processor_t2.desc").withStyle(ChatFormatting.GRAY));
                    builder.accept(Component.translatable("tooltip.avoidminer.processor_t2.upgrades").withStyle(ChatFormatting.GRAY));
                }
            });
    public static final DeferredItem<BlockItem> AVOID_PROCESSOR_TIER_3 = ITEMS.registerItem("avoid_processor_tier_3",
            props -> new BlockItem(ModBlocks.AVOID_PROCESSOR_TIER_3.get(), props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.processor_t3.desc").withStyle(ChatFormatting.GRAY));
                    builder.accept(Component.translatable("tooltip.avoidminer.processor_t3.upgrades").withStyle(ChatFormatting.GRAY));
                }
            });

    public static final DeferredItem<BlockItem> MAGNETITE_FURNACE_TIER_1 = ITEMS.registerItem(
            "magnetite_furnace_tier_1",
            props -> magnetiteFurnaceItem(ModBlocks.MAGNETITE_FURNACE_TIER_1.get(), props, 1));
    public static final DeferredItem<BlockItem> MAGNETITE_FURNACE_TIER_2 = ITEMS.registerItem(
            "magnetite_furnace_tier_2",
            props -> magnetiteFurnaceItem(ModBlocks.MAGNETITE_FURNACE_TIER_2.get(), props, 2));
    public static final DeferredItem<BlockItem> MAGNETITE_FURNACE_TIER_3 = ITEMS.registerItem(
            "magnetite_furnace_tier_3",
            props -> magnetiteFurnaceItem(ModBlocks.MAGNETITE_FURNACE_TIER_3.get(), props, 3));

    private static BlockItem magnetiteFurnaceItem(net.minecraft.world.level.block.Block block,
                                                   Item.Properties properties, int tier) {
        return new BlockItem(block, properties.stacksTo(1)) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                        Consumer<Component> builder, TooltipFlag flag) {
                builder.accept(Component.translatable("tooltip.avoidminer.magnetite_furnace_t" + tier + ".desc")
                        .withStyle(ChatFormatting.GRAY));
                builder.accept(Component.translatable("tooltip.avoidminer.magnetite_furnace.upgrades")
                        .withStyle(ChatFormatting.GRAY));
                var data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
                if (data != null) {
                    int energy = data.copyTagWithoutId().getIntOr("Energy", 0);
                    builder.accept(Component.translatable("tooltip.avoidminer.machine.stored_energy", energy)
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        };
    }

    public static final DeferredItem<BlockItem> AVOID_MINER_TIER_1 = ITEMS.registerItem("avoid_miner_tier_1",
            props -> new BlockItem(ModBlocks.AVOID_MINER_TIER_1.get(), props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.machine_t1.efficiency").withStyle(ChatFormatting.GRAY));
                    builder.accept(Component.translatable("tooltip.avoidminer.machine_t1.slots").withStyle(ChatFormatting.GRAY));
                    builder.accept(Component.translatable("tooltip.avoidminer.machine.modes").withStyle(ChatFormatting.GRAY));
                }
            });
    public static final DeferredItem<BlockItem> AVOID_MINER_TIER_2 = ITEMS.registerItem("avoid_miner_tier_2",
            props -> new BlockItem(ModBlocks.AVOID_MINER_TIER_2.get(), props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.machine_t2.efficiency").withStyle(ChatFormatting.GRAY));
                    builder.accept(Component.translatable("tooltip.avoidminer.machine_t2.slots").withStyle(ChatFormatting.GRAY));
                    builder.accept(Component.translatable("tooltip.avoidminer.machine.modes").withStyle(ChatFormatting.GRAY));
                }
            });
    public static final DeferredItem<BlockItem> AVOID_MINER_TIER_3 = ITEMS.registerItem("avoid_miner_tier_3",
            props -> new BlockItem(ModBlocks.AVOID_MINER_TIER_3.get(), props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.machine_t3.efficiency").withStyle(ChatFormatting.GRAY));
                    builder.accept(Component.translatable("tooltip.avoidminer.machine_t3.slots").withStyle(ChatFormatting.GRAY));
                    builder.accept(Component.translatable("tooltip.avoidminer.machine.modes").withStyle(ChatFormatting.GRAY));
                }
            });

    public static final DeferredItem<Item> MINING_CORE = ITEMS.registerItem("mining_core",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.mining_core").withStyle(ChatFormatting.GRAY));
                }
            });

    public static final DeferredItem<Item> UPGRADE_TIER_2 = ITEMS.registerItem("upgrade_tier_2",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.upgrade_tier_2.obtain").withStyle(ChatFormatting.GRAY));
                }
            });
    public static final DeferredItem<Item> UPGRADE_TIER_3 = ITEMS.registerItem("upgrade_tier_3",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.upgrade_tier_3.obtain").withStyle(ChatFormatting.GRAY));
                }
            });

    public static final DeferredItem<Item> MINING_UPGRADE_TIER_1 = ITEMS.registerItem("mining_upgrade_tier_1",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.mining_upgrade_tier_1"));
    public static final DeferredItem<Item> MINING_UPGRADE_TIER_2 = ITEMS.registerItem("mining_upgrade_tier_2",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.mining_upgrade_tier_2"));
    public static final DeferredItem<Item> MINING_UPGRADE_TIER_3 = ITEMS.registerItem("mining_upgrade_tier_3",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.mining_upgrade_tier_3"));

    public static final DeferredItem<Item> RANGE_CARD = ITEMS.registerItem("range_card",
            RangeCardItem::new);

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
    public static final DeferredItem<Item> END_UPGRADE = ITEMS.registerItem("end_upgrade",
            props -> new MachineUpgradeItem(props,
                    "tooltip.avoidminer.end_upgrade.mode",
                    "tooltip.avoidminer.end_upgrade.output",
                    "tooltip.avoidminer.world_slot"));
    public static final DeferredItem<Item> DEEP_DARK_UPGRADE = ITEMS.registerItem("deep_dark_upgrade",
            props -> new MachineUpgradeItem(props,
                    "tooltip.avoidminer.deep_dark_upgrade.mode",
                    "tooltip.avoidminer.deep_dark_upgrade.output",
                    "tooltip.avoidminer.world_slot"));
    public static final DeferredItem<Item> OCEAN_UPGRADE = ITEMS.registerItem("ocean_upgrade",
            props -> new MachineUpgradeItem(props,
                    "tooltip.avoidminer.ocean_upgrade.mode",
                    "tooltip.avoidminer.ocean_upgrade.output",
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
    public static final DeferredItem<BlockItem> MAGNETITE_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.MAGNETITE_BLOCK);
    public static final DeferredItem<BlockItem> RESONANT_CRYSTAL_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.RESONANT_CRYSTAL_BLOCK);
    public static final DeferredItem<BlockItem> BUDDING_RESONANT_CRYSTAL = ITEMS.registerSimpleBlockItem(ModBlocks.BUDDING_RESONANT_CRYSTAL);
    public static final DeferredItem<BlockItem> RESONANT_CRYSTAL_CLUSTER = ITEMS.registerSimpleBlockItem(ModBlocks.RESONANT_CRYSTAL_CLUSTER);
    public static final DeferredItem<BlockItem> LARGE_RESONANT_CRYSTAL_BUD = ITEMS.registerSimpleBlockItem(ModBlocks.LARGE_RESONANT_CRYSTAL_BUD);
    public static final DeferredItem<BlockItem> MEDIUM_RESONANT_CRYSTAL_BUD = ITEMS.registerSimpleBlockItem(ModBlocks.MEDIUM_RESONANT_CRYSTAL_BUD);
    public static final DeferredItem<BlockItem> SMALL_RESONANT_CRYSTAL_BUD = ITEMS.registerSimpleBlockItem(ModBlocks.SMALL_RESONANT_CRYSTAL_BUD);
    public static final DeferredItem<Item> RESONANT_CRYSTAL = ITEMS.registerSimpleItem("resonant_crystal");
    public static final DeferredItem<ResonantScannerItem> RESONANT_SCANNER = ITEMS.registerItem(
            "resonant_scanner", props -> new ResonantScannerItem(props, 1));
    public static final DeferredItem<ResonantScannerItem> RESONANT_SCANNER_TIER_2 = ITEMS.registerItem(
            "resonant_scanner_tier_2", props -> new ResonantScannerItem(props, 2));
    public static final DeferredItem<ResonantScannerItem> RESONANT_SCANNER_TIER_3 = ITEMS.registerItem(
            "resonant_scanner_tier_3", props -> new ResonantScannerItem(props, 3));
    public static final DeferredItem<BlockItem> RESONANT_REPAIR_STATION = ITEMS.registerItem(
            "resonant_repair_station",
            props -> new BlockItem(ModBlocks.RESONANT_REPAIR_STATION.get(), props.stacksTo(1)));
    public static final DeferredItem<Item> RAW_MAGNETITE = ITEMS.registerSimpleItem("raw_magnetite");
    public static final DeferredItem<Item> MAGNETITE_INGOT = ITEMS.registerSimpleItem("magnetite_ingot");
    public static final DeferredItem<Item> MAGNETITE_NUGGET = ITEMS.registerSimpleItem("magnetite_nugget");
    public static final DeferredItem<Item> MAGNETITE_POWDER = ITEMS.registerSimpleItem("magnetite_powder");
    public static final DeferredItem<Item> IRON_POWDER = ITEMS.registerSimpleItem("iron_powder");
    public static final DeferredItem<Item> GOLD_POWDER = ITEMS.registerSimpleItem("gold_powder");
    public static final DeferredItem<Item> COPPER_POWDER = ITEMS.registerSimpleItem("copper_powder");
    public static final DeferredItem<HypersonicCannonItem> HYPERSONIC_CANNON = ITEMS.registerItem("hypersonic_cannon",
            HypersonicCannonItem::new);
    public static final DeferredItem<HypersonicCannonItem> HYPERSONIC_CANNON_TIER_2 = ITEMS.registerItem("hypersonic_cannon_tier_2",
            props -> new HypersonicCannonItem(props, 2));
    public static final DeferredItem<HypersonicCannonItem> HYPERSONIC_CANNON_TIER_3 = ITEMS.registerItem("hypersonic_cannon_tier_3",
            props -> new HypersonicCannonItem(props, 3));
    public static final DeferredItem<Item> WARDEN_HEART = ITEMS.registerItem("warden_heart", WardenHeartItem::new);
    public static final DeferredItem<Item> TEMPORAL_CLOCK = ITEMS.registerItem("temporal_clock", TemporalClockItem::new);
    public static final DeferredItem<TemporalClockCreativeItem> TEMPORAL_CLOCK_CREATIVE = ITEMS.registerItem(
            "temporal_clock_creative", TemporalClockCreativeItem::new);
    public static final DeferredItem<TemporalSlowClockItem> TEMPORAL_SLOW_CLOCK = ITEMS.registerItem(
            "temporal_slow_clock", TemporalSlowClockItem::new);
    public static final DeferredItem<TemporalSlowClockCreativeItem> TEMPORAL_SLOW_CLOCK_CREATIVE = ITEMS.registerItem(
            "temporal_slow_clock_creative", TemporalSlowClockCreativeItem::new);
    public static final DeferredItem<HypersonicCannonCreativeItem> HYPERSONIC_CANNON_CREATIVE = ITEMS.registerItem(
            "hypersonic_cannon_creative", HypersonicCannonCreativeItem::new);
    public static final DeferredItem<Item> TEMPORAL_FRAGMENT = ITEMS.registerItem(
            "temporal_fragment", TemporalFragmentItem::new);
    public static final DeferredItem<Item> TEMPORAL_CORE = ITEMS.registerItem(
            "temporal_core", TemporalCoreItem::new);
    public static final DeferredItem<Item> HYPERSONIC_SMITHING_TEMPLATE = ITEMS.registerItem(
            "hypersonic_smithing_template", HypersonicSmithingTemplateItem::new);
    public static final DeferredItem<Item> GLASS_SWORD = ITEMS.registerItem("glass_sword", GlassSwordItem::new);
    public static final DeferredItem<ResonantPickaxeItem> RESONANT_PICKAXE = ITEMS.registerItem(
            "resonant_pickaxe", ResonantPickaxeItem::new);
    public static final DeferredItem<ResonantRetaliationShieldItem> RESONANT_RETALIATION_SHIELD = ITEMS.registerItem(
            "resonant_retaliation_shield", ResonantRetaliationShieldItem::new);
    public static final DeferredItem<SeismicPropulsionBootsItem> SEISMIC_PROPULSION_BOOTS = ITEMS.registerItem(
            "seismic_propulsion_boots", SeismicPropulsionBootsItem::new);
    public static final DeferredItem<NightVisionHelmetItem> NIGHT_VISION_HELMET = ITEMS.registerItem(
            "night_vision_helmet", NightVisionHelmetItem::new);
    public static final DeferredItem<ThorHammerItem> THOR_HAMMER = ITEMS.registerItem(
            "thor_hammer", ThorHammerItem::new);
    public static final DeferredItem<Item> ELECTRIC_CORE = ITEMS.registerItem("electric_core",
            props -> new Item(props.stacksTo(1).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                            Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.electric_core.obtain")
                            .withStyle(ChatFormatting.GRAY));
                }
            });

    public static final DeferredItem<Item> PROCESSING_CORE = ITEMS.registerItem("processing_core",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.processing_core").withStyle(ChatFormatting.GRAY));
                }
            });

    public static final DeferredItem<Item> TRIAL_SPAWNER_CORE = ITEMS.registerItem("trial_spawner_core",
            props -> new Item(props.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> VAULT_CORE = ITEMS.registerItem("vault_core",
            props -> new Item(props.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> OMINOUS_VAULT_CORE = ITEMS.registerItem("ominous_vault_core",
            props -> new Item(props.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<ConfiguredTrialSpawnerItem> CONFIGURED_TRIAL_SPAWNER = ITEMS.registerItem(
            "configured_trial_spawner", ConfiguredTrialSpawnerItem::new);
    public static final DeferredItem<ConfiguredVaultItem> CONFIGURED_VAULT = ITEMS.registerItem(
            "configured_vault", props -> new ConfiguredVaultItem(props, false));
    public static final DeferredItem<ConfiguredVaultItem> CONFIGURED_OMINOUS_VAULT = ITEMS.registerItem(
            "configured_ominous_vault", props -> new ConfiguredVaultItem(props, true));

    public static final DeferredItem<Item> BLANK_UPGRADE_PATTERN = ITEMS.registerSimpleItem("blank_upgrade_pattern");

    public static final DeferredItem<Item> BLANK_CARD = ITEMS.registerSimpleItem("blank_card");

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
    public static final DeferredItem<MobCardItem> BREEZE_CARD = ITEMS.registerItem("breeze_card",
            props -> new MobCardItem(MobCardType.BREEZE, props.stacksTo(1)));
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
    public static final DeferredItem<MobCardItem> WARDEN_CARD = ITEMS.registerItem("warden_card",
            props -> new MobCardItem(MobCardType.WARDEN, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> WITHER_CARD = ITEMS.registerItem("wither_card",
            props -> new MobCardItem(MobCardType.WITHER, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> BOGGED_CARD = ITEMS.registerItem("bogged_card",
            props -> new MobCardItem(MobCardType.BOGGED, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> DROWNED_CARD = ITEMS.registerItem("drowned_card",
            props -> new MobCardItem(MobCardType.DROWNED, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> HOGLIN_CARD = ITEMS.registerItem("hoglin_card",
            props -> new MobCardItem(MobCardType.HOGLIN, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> PHANTOM_CARD = ITEMS.registerItem("phantom_card",
            props -> new MobCardItem(MobCardType.PHANTOM, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> PILLAGER_CARD = ITEMS.registerItem("pillager_card",
            props -> new MobCardItem(MobCardType.PILLAGER, props.stacksTo(1)));
    public static final DeferredItem<MobCardItem> STRAY_CARD = ITEMS.registerItem("stray_card",
            props -> new MobCardItem(MobCardType.STRAY, props.stacksTo(1)));

    // Referência ordenada usada pela HUD, JEI e máquina.
    public static final DeferredItem<?>[] MOB_CARDS = {
            SKELETON_CARD, WITHER_SKELETON_CARD, CREEPER_CARD, ZOMBIE_CARD,
            SPIDER_CARD, WITCH_CARD, PIGLIN_CARD, PIGLIN_BRUTE_CARD,
            ENDERMAN_CARD, VILLAGER_CARD, BLAZE_CARD, PIG_CARD, COW_CARD,
            CHICKEN_CARD, SHEEP_CARD, BREEZE_CARD, GUARDIAN_CARD, ELDER_GUARDIAN_CARD,
            SLIME_CARD, VINDICATOR_CARD, EVOKER_CARD, GHAST_CARD, MAGMA_CUBE_CARD,
            SHULKER_CARD, ENDER_DRAGON_CARD, WARDEN_CARD, WITHER_CARD,
            BOGGED_CARD, DROWNED_CARD, HOGLIN_CARD, PHANTOM_CARD, PILLAGER_CARD, STRAY_CARD
    };

    public static final DeferredItem<Item> SCROLL_OF_RENEWAL = ITEMS.registerItem("scroll_of_renewal",
            props -> new ScrollOfRenewalItem(props.stacksTo(16)));
    public static final DeferredItem<Item> HERO_MEDALLION = ITEMS.registerItem("hero_medallion",
            props -> new HeroMedallionItem(props.stacksTo(1)));
    public static final DeferredItem<Item> SCROLL_OF_FORGETFULNESS = ITEMS.registerItem("scroll_of_forgetfulness",
            props -> new ScrollOfForgetfulnessItem(props.stacksTo(16)));

    public static final DeferredItem<Item> ZOMBIE_POISON = ITEMS.registerItem("zombie_poison",
            props -> new ZombiePoisonItem(props.stacksTo(16)));
    public static final DeferredItem<Item> VILLAGER_LASSO = ITEMS.registerItem("villager_lasso",
            props -> new VillagerLassoItem(props.durability(4).stacksTo(1)));
    public static final DeferredItem<BlockItem> MINER = ITEMS.registerItem("miner",
            props -> new BlockItem(ModBlocks.MINER.get(), props.stacksTo(1)));

    public static final DeferredItem<BlockItem> VILLAGER_HUB = ITEMS.registerItem("villager_hub",
            props -> new BlockItem(ModBlocks.VILLAGER_HUB.get(), props.stacksTo(1)));

    public static final DeferredItem<Item> GUIDE_BOOK = ITEMS.registerItem("guide_book",
            props -> new GuideBookItem(props.stacksTo(1)));

    public static final DeferredItem<BlockItem> AVOID_LOOTR = ITEMS.registerItem("avoid_lootr",
            props -> new BlockItem(ModBlocks.AVOID_LOOTR.get(), props.stacksTo(1)));

    // Bateria Criativa (energia infinita)
    public static final DeferredItem<BlockItem> CREATIVE_BATTERY = ITEMS.registerItem("creative_battery",
            props -> new BlockItem(ModBlocks.CREATIVE_BATTERY.get(), props.stacksTo(1)) {
                @Override
                public boolean isFoil(ItemStack stack) { return true; }
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                            Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.creative_battery.desc")
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                    builder.accept(Component.translatable("tooltip.avoidminer.creative_battery.usage")
                            .withStyle(ChatFormatting.GRAY));
                    builder.accept(Component.translatable("tooltip.avoidminer.creative_battery.stored")
                            .withStyle(ChatFormatting.RED));
                }
            });

    // Bateria + Link de Energia
    public static final DeferredItem<BlockItem> BATTERY = ITEMS.registerItem("battery",
            props -> new BlockItem(ModBlocks.BATTERY.get(), props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.battery.desc").withStyle(ChatFormatting.GRAY));
                    builder.accept(Component.translatable("tooltip.avoidminer.battery.usage").withStyle(ChatFormatting.GRAY));
                    var data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
                    if (data != null) {
                        var tag = data.copyTagWithoutId();
                        int energy = tag.getIntOr("Energy", 0);
                        long capacity = Math.min(
                                Integer.MAX_VALUE,
                                4_000_000L + tag.getLongOr("InstalledCapacityBonus", 0L)
                        );
                        int installed = tag.getIntOr("InstalledCapacityUpgrades", 0);
                        builder.accept(Component.translatable("tooltip.avoidminer.battery.stored", energy, capacity)
                                .withStyle(ChatFormatting.RED));
                        builder.accept(Component.translatable("tooltip.avoidminer.battery.installed", installed)
                                .withStyle(ChatFormatting.DARK_RED));
                    }
                }
            });

    // Barril de Magnetita: aspira itens/XP num raio de 8 blocos
    public static final DeferredItem<BlockItem> MAGNETITE_BARREL = ITEMS.registerItem("magnetite_barrel",
            props -> new BlockItem(ModBlocks.MAGNETITE_BARREL.get(), props) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.magnetite_barrel.desc"));
                    builder.accept(Component.translatable("tooltip.avoidminer.magnetite_barrel.slots"));
                    int storedXp = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                            net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getIntOr("StoredXp", 0);
                    if (storedXp > 0) {
                        builder.accept(Component.translatable("tooltip.avoidminer.magnetite_barrel.stored_xp", storedXp));
                    }
                }
            });

    public static final DeferredItem<Item> XP_STORAGE_CORE = ITEMS.registerItem("xp_storage_core",
            props -> new Item(props.rarity(Rarity.RARE)));

    public static final DeferredItem<BlockItem> XP_VAULT = ITEMS.registerItem("xp_vault",
            props -> new BlockItem(ModBlocks.XP_VAULT.get(), props.stacksTo(1).rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                            Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.xp_vault.desc")
                            .withStyle(ChatFormatting.GRAY));
                    var data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
                    if (data != null) {
                        int levels = data.copyTagWithoutId().getIntOr("StoredLevels", 0);
                        if (levels > 0) {
                            builder.accept(Component.translatable("tooltip.avoidminer.xp_vault.stored", levels)
                                    .withStyle(ChatFormatting.GREEN));
                        }
                    }
                }
            });

    public static final DeferredItem<Item> ITEM_ABSORB_UPGRADE = ITEMS.registerItem("item_absorb_upgrade",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.item_absorb_upgrade"));

    public static final DeferredItem<Item> XP_ABSORB_UPGRADE = ITEMS.registerItem("xp_absorb_upgrade",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.xp_absorb_upgrade"));

    public static final DeferredItem<Item> ITEM_STACKING_UPGRADE = ITEMS.registerItem("item_stacking_upgrade",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.item_stacking_upgrade"));

    public static final DeferredItem<Item> ENERGY_LINK = ITEMS.registerItem("energy_link",
            com.airton.avoidminer.item.EnergyLinkItem::new);

    // Upgrades específicos da Bateria
    public static final DeferredItem<Item> CAPACITY_UPGRADE_TIER_1 = ITEMS.registerItem("capacity_upgrade_tier_1",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.capacity_upgrade_tier_1"));
    public static final DeferredItem<Item> CAPACITY_UPGRADE_TIER_2 = ITEMS.registerItem("capacity_upgrade_tier_2",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.capacity_upgrade_tier_2"));
    public static final DeferredItem<Item> CAPACITY_UPGRADE_TIER_3 = ITEMS.registerItem("capacity_upgrade_tier_3",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.capacity_upgrade_tier_3"));
    public static final DeferredItem<Item> RANGE_UPGRADE_TIER_1 = ITEMS.registerItem("range_upgrade_tier_1",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.range_upgrade_tier_1"));
    public static final DeferredItem<Item> RANGE_UPGRADE_TIER_2 = ITEMS.registerItem("range_upgrade_tier_2",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.range_upgrade_tier_2"));
    public static final DeferredItem<Item> RANGE_UPGRADE_TIER_3 = ITEMS.registerItem("range_upgrade_tier_3",
            props -> new MachineUpgradeItem(props, "tooltip.avoidminer.range_upgrade_tier_3"));

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
