package com.airton.avoidminer.block;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.block.entity.BatteryBlockEntity;
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

/**
 * Bloco da Bateria — armazena energia transformada para as demais máquinas
 * consumirem via Link de Energia. Acende (LIT) enquanto tiver carga.
 */
public class BatteryBlock extends Block implements EntityBlock {
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public BatteryBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BatteryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return type == ModBlockEntities.BATTERY.get()
                ? (level1, pos, state1, be) -> BatteryBlockEntity.tick(level1, pos, state1, (BatteryBlockEntity) be)
                : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BatteryBlockEntity battery) {
                battery.openMenu((ServerPlayer) player);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void dropInventory(Level level, BlockPos pos, BlockEntity blockEntity) {
        if (blockEntity instanceof BatteryBlockEntity battery) {
            ResourceHandler<ItemResource> handler = battery.getItemHandler();
            for (int i = 0; i < handler.size(); i++) {
                ItemResource resource = handler.getResource(i);
                int amount = handler.getAmountAsInt(i);
                if (!resource.isEmpty() && amount > 0) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), resource.toStack(amount));
                    if (handler instanceof ItemStacksResourceHandler isrh) {
                        isrh.set(i, ItemResource.EMPTY, 0);
                    }
                }
            }
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack stack, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide()) dropInventory(level, pos, level.getBlockEntity(pos));
        return super.onDestroyedByPlayer(state, level, pos, player, stack, willHarvest, fluid);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (!level.isClientSide()) dropInventory(level, pos, blockEntity);
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }
}
