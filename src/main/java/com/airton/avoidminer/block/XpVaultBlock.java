package com.airton.avoidminer.block;

import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.block.entity.XpVaultBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;

public final class XpVaultBlock extends Block implements EntityBlock {
    public XpVaultBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new XpVaultBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof XpVaultBlockEntity vault) {
            vault.openMenu(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
                                       ItemStack tool, boolean willHarvest, FluidState fluid) {
        if (level instanceof ServerLevel serverLevel && !player.hasInfiniteMaterials()) {
            ItemStack dropped = new ItemStack(ModItems.XP_VAULT.get());
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof XpVaultBlockEntity) {
                dropped.set(
                        DataComponents.BLOCK_ENTITY_DATA,
                        TypedEntityData.of(blockEntity.getType(),
                                blockEntity.saveCustomOnly(serverLevel.registryAccess()))
                );
            }
            Block.popResource(serverLevel, pos, dropped);
        }
        return super.onDestroyedByPlayer(state, level, pos, player, tool, willHarvest, fluid);
    }
}
