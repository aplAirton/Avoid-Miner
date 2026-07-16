package com.airton.avoidminer.block;

import com.airton.avoidminer.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public final class BuddingResonantCrystalBlock extends AmethystBlock {
    private static final Direction[] DIRECTIONS = Direction.values();

    public BuddingResonantCrystalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) return;

        Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos growthPos = pos.relative(direction);
        BlockState current = level.getBlockState(growthPos);
        Block next = null;

        if (BuddingAmethystBlock.canClusterGrowAtState(current)) {
            next = ModBlocks.SMALL_RESONANT_CRYSTAL_BUD.get();
        } else if (current.is(ModBlocks.SMALL_RESONANT_CRYSTAL_BUD.get())
                && current.getValue(AmethystClusterBlock.FACING) == direction) {
            next = ModBlocks.MEDIUM_RESONANT_CRYSTAL_BUD.get();
        } else if (current.is(ModBlocks.MEDIUM_RESONANT_CRYSTAL_BUD.get())
                && current.getValue(AmethystClusterBlock.FACING) == direction) {
            next = ModBlocks.LARGE_RESONANT_CRYSTAL_BUD.get();
        } else if (current.is(ModBlocks.LARGE_RESONANT_CRYSTAL_BUD.get())
                && current.getValue(AmethystClusterBlock.FACING) == direction) {
            next = ModBlocks.RESONANT_CRYSTAL_CLUSTER.get();
        }

        if (next != null) {
            BlockState grown = next.defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, direction)
                    .setValue(AmethystClusterBlock.WATERLOGGED,
                            current.getFluidState().is(Fluids.WATER));
            level.setBlockAndUpdate(growthPos, grown);
        }
    }
}
