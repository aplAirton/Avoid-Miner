package com.airton.avoidminer;

import com.airton.avoidminer.block.AvoidMinerBlock;
import com.airton.avoidminer.block.BatteryBlock;
import com.airton.avoidminer.block.CreativeBatteryBlock;
import com.airton.avoidminer.block.LootrBlock;
import com.airton.avoidminer.block.MagnetiteFurnaceBlock;
import com.airton.avoidminer.block.ProcessorBlock;
import com.airton.avoidminer.block.XpVaultBlock;
import com.airton.avoidminer.block.BuddingResonantCrystalBlock;
import com.airton.avoidminer.block.ResonantRepairStationBlock;
import com.airton.avoidminer.block.entity.AvoidMinerBlockEntity;
import com.airton.avoidminer.block.entity.ProcessorBlockEntity;
import com.airton.avoidminer.block.entity.MagnetiteFurnaceBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AvoidMiner.MODID);

    public static final DeferredBlock<Block> AVOID_MINER_TIER_1 = BLOCKS.registerBlock("avoid_miner_tier_1",
            p -> new AvoidMinerBlock(AvoidMinerBlockEntity.Tier.TIER_1, p),
            () -> BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> AVOID_MINER_TIER_2 = BLOCKS.registerBlock("avoid_miner_tier_2",
            p -> new AvoidMinerBlock(AvoidMinerBlockEntity.Tier.TIER_2, p),
            () -> BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> AVOID_MINER_TIER_3 = BLOCKS.registerBlock("avoid_miner_tier_3",
            p -> new AvoidMinerBlock(AvoidMinerBlockEntity.Tier.TIER_3, p),
            () -> BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> AVOID_PROCESSOR_TIER_1 = BLOCKS.registerBlock("avoid_processor_tier_1",
            p -> new ProcessorBlock(ProcessorBlockEntity.Tier.TIER_1, p),
            () -> BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> AVOID_PROCESSOR_TIER_2 = BLOCKS.registerBlock("avoid_processor_tier_2",
            p -> new ProcessorBlock(ProcessorBlockEntity.Tier.TIER_2, p),
            () -> BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> AVOID_PROCESSOR_TIER_3 = BLOCKS.registerBlock("avoid_processor_tier_3",
            p -> new ProcessorBlock(ProcessorBlockEntity.Tier.TIER_3, p),
            () -> BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> MAGNETITE_FURNACE_TIER_1 = BLOCKS.registerBlock("magnetite_furnace_tier_1",
            p -> new MagnetiteFurnaceBlock(MagnetiteFurnaceBlockEntity.Tier.TIER_1, p),
            () -> BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHERITE_BLOCK));

    public static final DeferredBlock<Block> MAGNETITE_FURNACE_TIER_2 = BLOCKS.registerBlock("magnetite_furnace_tier_2",
            p -> new MagnetiteFurnaceBlock(MagnetiteFurnaceBlockEntity.Tier.TIER_2, p),
            () -> BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHERITE_BLOCK));

    public static final DeferredBlock<Block> MAGNETITE_FURNACE_TIER_3 = BLOCKS.registerBlock("magnetite_furnace_tier_3",
            p -> new MagnetiteFurnaceBlock(MagnetiteFurnaceBlockEntity.Tier.TIER_3, p),
            () -> BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHERITE_BLOCK));

    public static final DeferredBlock<Block> MAGNETITE_ORE = BLOCKS.registerBlock("magnetite_ore",
            Block::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHER_ORE));

    public static final DeferredBlock<Block> RAW_MAGNETITE_BLOCK = BLOCKS.registerBlock("raw_magnetite_block",
            Block::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(5.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHERITE_BLOCK));

    public static final DeferredBlock<Block> MAGNETITE_BLOCK = BLOCKS.registerBlock("magnetite_block",
            Block::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(5.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL));

    public static final DeferredBlock<AmethystBlock> RESONANT_CRYSTAL_BLOCK = BLOCKS.registerBlock(
            "resonant_crystal_block", AmethystBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN).strength(1.5F)
                    .sound(SoundType.AMETHYST).requiresCorrectToolForDrops());

    public static final DeferredBlock<BuddingResonantCrystalBlock> BUDDING_RESONANT_CRYSTAL = BLOCKS.registerBlock(
            "budding_resonant_crystal", BuddingResonantCrystalBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN).randomTicks().strength(1.5F)
                    .sound(SoundType.AMETHYST).requiresCorrectToolForDrops()
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<AmethystClusterBlock> RESONANT_CRYSTAL_CLUSTER = BLOCKS.registerBlock(
            "resonant_crystal_cluster", p -> new AmethystClusterBlock(7.0F, 3.0F, p),
            () -> BlockBehaviour.Properties.ofLegacyCopy(Blocks.AMETHYST_CLUSTER)
                    .mapColor(MapColor.COLOR_CYAN));
    public static final DeferredBlock<AmethystClusterBlock> LARGE_RESONANT_CRYSTAL_BUD = BLOCKS.registerBlock(
            "large_resonant_crystal_bud", p -> new AmethystClusterBlock(5.0F, 3.0F, p),
            () -> BlockBehaviour.Properties.ofLegacyCopy(Blocks.LARGE_AMETHYST_BUD)
                    .mapColor(MapColor.COLOR_CYAN));
    public static final DeferredBlock<AmethystClusterBlock> MEDIUM_RESONANT_CRYSTAL_BUD = BLOCKS.registerBlock(
            "medium_resonant_crystal_bud", p -> new AmethystClusterBlock(4.0F, 3.0F, p),
            () -> BlockBehaviour.Properties.ofLegacyCopy(Blocks.MEDIUM_AMETHYST_BUD)
                    .mapColor(MapColor.COLOR_CYAN));
    public static final DeferredBlock<AmethystClusterBlock> SMALL_RESONANT_CRYSTAL_BUD = BLOCKS.registerBlock(
            "small_resonant_crystal_bud", p -> new AmethystClusterBlock(3.0F, 4.0F, p),
            () -> BlockBehaviour.Properties.ofLegacyCopy(Blocks.SMALL_AMETHYST_BUD)
                    .mapColor(MapColor.COLOR_CYAN));

    public static final DeferredBlock<Block> AVOID_LOOTR = BLOCKS.registerBlock("avoid_lootr",
            LootrBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHERITE_BLOCK));

    public static final DeferredBlock<Block> BATTERY = BLOCKS.registerBlock("battery",
            BatteryBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.COPPER));

    public static final DeferredBlock<Block> MAGNETITE_BARREL = BLOCKS.registerBlock("magnetite_barrel",
            com.airton.avoidminer.block.MagnetiteBarrelBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .noLootTable()
                    .sound(SoundType.METAL));

    public static final DeferredBlock<Block> XP_VAULT = BLOCKS.registerBlock("xp_vault",
            XpVaultBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(4.0f, 8.0f)
                    .requiresCorrectToolForDrops()
                    .noLootTable()
                    .sound(SoundType.SCULK_CATALYST));

    public static final DeferredBlock<Block> RESONANT_REPAIR_STATION = BLOCKS.registerBlock(
            "resonant_repair_station", ResonantRepairStationBlock::new,
            () -> BlockBehaviour.Properties.of().strength(4.0F, 8.0F)
                    .requiresCorrectToolForDrops().noLootTable().sound(SoundType.AMETHYST));

    public static final DeferredBlock<Block> CREATIVE_BATTERY = BLOCKS.registerBlock("creative_battery",
            CreativeBatteryBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(-1.0f, 3600000.0f)
                    .noLootTable()
                    .sound(SoundType.COPPER));
}
