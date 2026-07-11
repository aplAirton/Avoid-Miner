package com.airton.avoidminer.block;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.block.entity.AvoidMinerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.Nullable;

public class AvoidMinerBlock extends Block implements EntityBlock {
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    private final AvoidMinerBlockEntity.Tier tier;

    public AvoidMinerBlock(AvoidMinerBlockEntity.Tier tier, BlockBehaviour.Properties properties) {
        super(properties);
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    public AvoidMinerBlockEntity.Tier getTier() {
        return tier;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AvoidMinerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.AVOID_MINER.get(), AvoidMinerBlockEntity::tick);
    }

    @Nullable
    private static <E extends BlockEntity, T extends BlockEntity> BlockEntityTicker<T> createTickerHelper(
            BlockEntityType<T> type, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return type == expectedType ? (BlockEntityTicker<T>) ticker : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AvoidMinerBlockEntity miner) {
                miner.openMenu((ServerPlayer) player);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void dropInventory(Level level, BlockPos pos, BlockEntity blockEntity) {
        if (blockEntity instanceof AvoidMinerBlockEntity miner) {
            ResourceHandler<ItemResource> handler = miner.getItemHandler();
            boolean dropped = false;
            for (int i = 0; i < handler.size(); i++) {
                ItemResource resource = handler.getResource(i);
                int amount = handler.getAmountAsInt(i);
                if (!resource.isEmpty() && amount > 0) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), resource.toStack(amount));
                    dropped = true;
                }
            }
            if (dropped) {
                for (int i = 0; i < handler.size(); i++) {
                    if (!handler.getResource(i).isEmpty()) {
                        if (handler instanceof ItemStacksResourceHandler isrh) {
                            isrh.set(i, ItemResource.EMPTY, 0);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack stack, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide()) {
            dropInventory(level, pos, level.getBlockEntity(pos));
        }
        return super.onDestroyedByPlayer(state, level, pos, player, stack, willHarvest, fluid);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (!level.isClientSide()) {
            dropInventory(level, pos, blockEntity);
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }
}
