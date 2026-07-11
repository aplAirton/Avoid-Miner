package com.airton.avoidminer;

import com.airton.avoidminer.item.GuideBookItem;
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
            props -> new Item(props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.mining_upgrade_tier_1"));
                }
            });
    public static final DeferredItem<Item> MINING_UPGRADE_TIER_2 = ITEMS.registerItem("mining_upgrade_tier_2",
            props -> new Item(props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.mining_upgrade_tier_2"));
                }
            });
    public static final DeferredItem<Item> MINING_UPGRADE_TIER_3 = ITEMS.registerItem("mining_upgrade_tier_3",
            props -> new Item(props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.mining_upgrade_tier_3"));
                }
            });

    public static final DeferredItem<Item> ENERGY_UPGRADE_TIER_1 = ITEMS.registerItem("energy_upgrade_tier_1",
            props -> new Item(props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.energy_upgrade_tier_1"));
                }
            });
    public static final DeferredItem<Item> ENERGY_UPGRADE_TIER_2 = ITEMS.registerItem("energy_upgrade_tier_2",
            props -> new Item(props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.energy_upgrade_tier_2"));
                }
            });
    public static final DeferredItem<Item> ENERGY_UPGRADE_TIER_3 = ITEMS.registerItem("energy_upgrade_tier_3",
            props -> new Item(props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.energy_upgrade_tier_3"));
                }
            });

    public static final DeferredItem<Item> SPEED_UPGRADE_TIER_1 = ITEMS.registerItem("speed_upgrade_tier_1",
            props -> new Item(props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.speed_upgrade_tier_1"));
                }
            });
    public static final DeferredItem<Item> SPEED_UPGRADE_TIER_2 = ITEMS.registerItem("speed_upgrade_tier_2",
            props -> new Item(props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.speed_upgrade_tier_2"));
                }
            });
    public static final DeferredItem<Item> SPEED_UPGRADE_TIER_3 = ITEMS.registerItem("speed_upgrade_tier_3",
            props -> new Item(props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.speed_upgrade_tier_3"));
                }
            });

    public static final DeferredItem<Item> BOTANY_UPGRADE = ITEMS.registerItem("botany_upgrade",
            props -> new Item(props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.botany_upgrade.mode"));
                    builder.accept(Component.translatable("tooltip.avoidminer.botany_upgrade.output"));
                    builder.accept(Component.translatable("tooltip.avoidminer.world_slot"));
                }
            });
    public static final DeferredItem<Item> NETHER_UPGRADE = ITEMS.registerItem("nether_upgrade",
            props -> new Item(props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.nether_upgrade.mode"));
                    builder.accept(Component.translatable("tooltip.avoidminer.nether_upgrade.output"));
                    builder.accept(Component.translatable("tooltip.avoidminer.world_slot"));
                }
            });
    public static final DeferredItem<Item> FORTUNE_UPGRADE = ITEMS.registerItem("fortune_upgrade",
            props -> new Item(props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.fortune_upgrade.applies"));
                    builder.accept(Component.translatable("tooltip.avoidminer.fortune_upgrade.output"));
                    builder.accept(Component.translatable("tooltip.avoidminer.enchant_slot"));
                }
            });
    public static final DeferredItem<Item> SILK_UPGRADE = ITEMS.registerItem("silk_upgrade",
            props -> new Item(props.stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
                    builder.accept(Component.translatable("tooltip.avoidminer.silk_upgrade.applies"));
                    builder.accept(Component.translatable("tooltip.avoidminer.silk_upgrade.output"));
                    builder.accept(Component.translatable("tooltip.avoidminer.enchant_slot"));
                }
            });

    public static final DeferredItem<Item> BLANK_UPGRADE_PATTERN = ITEMS.registerSimpleItem("blank_upgrade_pattern");
    public static final DeferredItem<Item> GUIDE_BOOK = ITEMS.registerItem("guide_book",
            props -> new GuideBookItem(props.stacksTo(1)));
}
