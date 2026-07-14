package com.airton.avoidminer.block;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.block.entity.CreativeBatteryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CreativeBatteryBlock extends Block implements EntityBlock {
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public CreativeBatteryBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreativeBatteryBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CreativeBatteryBlockEntity battery) {
                battery.openMenu((ServerPlayer) player);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return type == ModBlockEntities.CREATIVE_BATTERY.get()
                ? (l, p, s, be) -> CreativeBatteryBlockEntity.tick(l, p, s, (CreativeBatteryBlockEntity) be)
                : null;
    }
}
