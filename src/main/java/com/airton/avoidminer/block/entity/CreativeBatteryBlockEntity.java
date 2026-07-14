package com.airton.avoidminer.block.entity;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.block.CreativeBatteryBlock;
import com.airton.avoidminer.energy.EnergyReceiver;
import com.airton.avoidminer.menu.BatteryMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class CreativeBatteryBlockEntity extends BlockEntity {
    private static final int TRANSFER_PER_ADJACENT = 1000;

    private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(3) {
        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) {}
        @Override
        public boolean isValid(int slot, ItemResource resource) { return false; }
    };

    private final ContainerData data = new SimpleContainerData(BatteryBlockEntity.DATA_SIZE) {
        @Override
        public int get(int index) {
            return switch (index) {
                case BatteryBlockEntity.DATA_ENERGY_LO -> 0xFFFF;
                case BatteryBlockEntity.DATA_ENERGY_HI -> 0x7FFF;
                case BatteryBlockEntity.DATA_LINKS -> 0;
                case BatteryBlockEntity.DATA_CHARGING -> 1;
                case BatteryBlockEntity.DATA_CAPACITY_LO -> 0xFFFF;
                case BatteryBlockEntity.DATA_CAPACITY_HI -> 0x7FFF;
                case BatteryBlockEntity.DATA_RANGE_TIER -> 0;
                case BatteryBlockEntity.DATA_INSTALLED_CAPACITY -> 0;
                default -> 0;
            };
        }
    };

    private final ResourceHandler<ItemResource> sideHandler = new ResourceHandler<>() {
        @Override public int size() { return itemHandler.size(); }
        @Override public ItemResource getResource(int index) { return itemHandler.getResource(index); }
        @Override public long getAmountAsLong(int index) { return itemHandler.getAmountAsLong(index); }
        @Override public long getCapacityAsLong(int index, ItemResource resource) { return itemHandler.getCapacityAsLong(index, resource); }
        @Override public boolean isValid(int index, ItemResource resource) { return false; }
        @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) { return 0; }
        @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) { return 0; }
    };

    public CreativeBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_BATTERY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CreativeBatteryBlockEntity be) {
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor instanceof EnergyReceiver receiver) {
                receiver.receiveEnergy(TRANSFER_PER_ADJACENT, false);
            }
        }
    }

    public void openMenu(ServerPlayer player) {
        player.openMenu(getMenuProvider(), worldPosition);
    }

    public MenuProvider getMenuProvider() {
        return new SimpleMenuProvider(
                (id, inv, p) -> new BatteryMenu(id, inv, itemHandler, data),
                Component.translatable("container.avoidminer.battery")
        );
    }

    public ResourceHandler<ItemResource> getItemHandler() { return itemHandler; }

    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        return side == null ? itemHandler : sideHandler;
    }
}
