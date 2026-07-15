package com.airton.avoidminer.block;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.block.entity.MagnetiteBarrelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Bloco do Barril de Magnetita. Ao quebrar, derruba apenas os itens
 * guardados; o XP acumulado permanece como dado inerente do item do barril
 * (CUSTOM_DATA "StoredXp") e é restaurado quando o bloco volta ao mundo.
 * Registrado com noLootTable — o drop do próprio barril é feito aqui.
 */
public class MagnetiteBarrelBlock extends Block implements EntityBlock {
    private static final String STORED_XP_KEY = "StoredXp";

    public MagnetiteBarrelBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MagnetiteBarrelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return type == ModBlockEntities.MAGNETITE_BARREL.get()
                ? (level1, pos, state1, be) -> MagnetiteBarrelBlockEntity.tick(level1, pos, state1, (MagnetiteBarrelBlockEntity) be)
                : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MagnetiteBarrelBlockEntity barrel) {
                barrel.openMenu((ServerPlayer) player);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) return;
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        int storedXp = data.copyTag().getIntOr(STORED_XP_KEY, 0);
        if (storedXp > 0 && level.getBlockEntity(pos) instanceof MagnetiteBarrelBlockEntity barrel) {
            barrel.setStoredXp(storedXp);
        }
    }

    private void dropContents(Level level, BlockPos pos, @Nullable BlockEntity blockEntity, boolean dropBarrelItem) {
        if (!(blockEntity instanceof MagnetiteBarrelBlockEntity barrel)) return;

        ResourceHandler<ItemResource> handler = barrel.getItemHandler();
        for (int i = 0; i < handler.size(); i++) {
            ItemResource resource = handler.getResource(i);
            int amount = handler.getAmountAsInt(i);
            while (!resource.isEmpty() && amount > 0) {
                int drop = Math.min(amount, resource.toStack(1).getMaxStackSize());
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), resource.toStack(drop));
                amount -= drop;
            }
            if (!handler.getResource(i).isEmpty() && handler instanceof ItemStacksResourceHandler isrh) {
                isrh.set(i, ItemResource.EMPTY, 0);
            }
        }

        if (dropBarrelItem) {
            ItemStack barrelStack = new ItemStack(ModItems.MAGNETITE_BARREL.get());
            int storedXp = barrel.getStoredXp();
            if (storedXp > 0) {
                CustomData data = barrelStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                barrelStack.set(DataComponents.CUSTOM_DATA, data.update(tag -> tag.putInt(STORED_XP_KEY, storedXp)));
            }
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), barrelStack);
            barrel.setStoredXp(0);
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack stack, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide()) {
            boolean dropBarrel = willHarvest && !player.hasInfiniteMaterials();
            dropContents(level, pos, level.getBlockEntity(pos), dropBarrel);
        }
        return super.onDestroyedByPlayer(state, level, pos, player, stack, willHarvest, fluid);
    }
}
