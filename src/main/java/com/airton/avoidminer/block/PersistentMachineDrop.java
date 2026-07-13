package com.airton.avoidminer.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

final class PersistentMachineDrop {
    private PersistentMachineDrop() {
    }

    static void drop(Level level, Player player, BlockPos pos, BlockState state,
                     Block block, @Nullable BlockEntity blockEntity, ItemStack tool) {
        player.awardStat(Stats.BLOCK_MINED.get(block));
        player.causeFoodExhaustion(0.005F);

        if (!(level instanceof ServerLevel serverLevel)) return;

        ItemStack dropped = new ItemStack(block);
        if (blockEntity != null) {
            dropped.set(
                    DataComponents.BLOCK_ENTITY_DATA,
                    TypedEntityData.of(blockEntity.getType(), blockEntity.saveCustomOnly(serverLevel.registryAccess()))
            );
        }

        Block.popResource(serverLevel, pos, dropped);
        state.spawnAfterBreak(serverLevel, pos, tool, true);
    }
}
