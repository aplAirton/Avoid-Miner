package com.airton.avoidminer.block;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.block.entity.MinerBlockEntity;
import com.airton.avoidminer.item.RangeCardItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MinerBlock extends Block implements EntityBlock {
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    public static final IntegerProperty TIER = IntegerProperty.create("tier", 1, 3);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final MinerBlockEntity.Tier tier;

    public MinerBlock(MinerBlockEntity.Tier tier, BlockBehaviour.Properties properties) {
        super(properties);
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(LIT, false).setValue(TIER, 1).setValue(FACING, Direction.NORTH));
    }

    public MinerBlockEntity.Tier getTier() {
        return tier;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, TIER, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MinerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.MINER.get(), MinerBlockEntity::tick);
    }

    @Nullable
    private static <E extends BlockEntity, T extends BlockEntity> BlockEntityTicker<T> createTickerHelper(
            BlockEntityType<T> type, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return type == expectedType ? (BlockEntityTicker<T>) ticker : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return openMenu(state, level, pos, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(ModItems.RANGE_CARD.get())) {
            if (!level.isClientSide()) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof MinerBlockEntity miner) {
                    if (RangeCardItem.hasCompleteData(stack)) {
                        int x1 = RangeCardItem.getX1(stack);
                        int z1 = RangeCardItem.getZ1(stack);
                        int x2 = RangeCardItem.getX2(stack);
                        int z2 = RangeCardItem.getZ2(stack);
                        int y1 = RangeCardItem.getY1(stack);
                        int y2 = RangeCardItem.getY2(stack);
                        var handler = miner.getItemHandler();
                        var existing = handler.getResource(MinerBlockEntity.RANGE_SLOT);
                        if (!existing.isEmpty()) {
                            player.sendSystemMessage(Component.translatable("msg.avoidminer.range_card.slot_occupied"));
                            return InteractionResult.SUCCESS;
                        }
                        try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                            int inserted = handler.insert(MinerBlockEntity.RANGE_SLOT,
                                    net.neoforged.neoforge.transfer.item.ItemResource.of(stack), 1, tx);
                            if (inserted > 0) {
                                tx.commit();
                                stack.shrink(1);
                                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ENDER_EYE_DEATH,
                                        net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 1.0f);
                                int depth = Math.abs(y2 - y1);
                                player.sendSystemMessage(Component.translatable("msg.avoidminer.range_card.applied",
                                        Math.min(x1, x2), Math.min(z1, z2), Math.max(x1, x2), Math.max(z1, z2), depth));
                            }
                        }
                    } else {
                        player.sendSystemMessage(Component.translatable("msg.avoidminer.range_card.incomplete"));
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (stack.is(ModItems.MINING_UPGRADE_TIER_2.get()) || stack.is(ModItems.MINING_UPGRADE_TIER_3.get())) {
            if (!level.isClientSide()) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof MinerBlockEntity miner) {
                    int targetOrdinal = stack.is(ModItems.MINING_UPGRADE_TIER_3.get()) ? 2 : 1;
                    if (miner.getTierOrdinal() >= targetOrdinal) {
                        player.sendSystemMessage(Component.translatable("msg.avoidminer.miner.already_tier"));
                    } else {
                        miner.upgradeTier(targetOrdinal);
                        stack.shrink(1);
                        player.sendSystemMessage(Component.translatable("msg.avoidminer.miner.upgraded"));
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return openMenu(state, level, pos, player);
    }

    private InteractionResult openMenu(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MinerBlockEntity miner) {
                miner.openMenu((ServerPlayer) player);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        PersistentMachineDrop.drop(level, player, pos, state, this, blockEntity, tool);
    }
}
