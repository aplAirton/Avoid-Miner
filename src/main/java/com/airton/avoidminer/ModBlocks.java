package com.airton.avoidminer;

import com.airton.avoidminer.block.AvoidMinerBlock;
import com.airton.avoidminer.block.LootrBlock;
import com.airton.avoidminer.block.ProcessorBlock;
import com.airton.avoidminer.block.entity.AvoidMinerBlockEntity;
import com.airton.avoidminer.block.entity.ProcessorBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
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

    public static final DeferredBlock<Block> AVOID_LOOTR = BLOCKS.registerBlock("avoid_lootr",
            LootrBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(4.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHERITE_BLOCK));
}
