package com.airton.avoidminer.menu;

import com.airton.avoidminer.ModMenuTypes;
import com.airton.avoidminer.block.entity.MinerBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class MinerMenu extends AbstractContainerMenu {
    private final ContainerData data;

    public static final int SLOT_SIZE = 18;
    public static final int RANGE_X = 79;
    public static final int RANGE_Y = 22;
    public static final int FILTER_X = 8;
    public static final int FILTER_Y = 22;
    public static final int UPGRADE_X = 43;
    public static final int UPGRADE_Y = 22;
    public static final int FILTER_GRID_X = 8;
    public static final int FILTER_GRID_Y = 90;
    public static final int FILTER_GRID_COLS = 9;
    public static final int FILTER_GRID_ROWS = 2;
    public static final int PLAYER_X = 8;
    public static final int PLAYER_Y = 134;
    public static final int HOTBAR_Y = 192;
    public static final int OVERLAY_BUTTON = 0;
    public static final int FILTER_CLEAR_BUTTON = 1;
    public static final int RESET_BUTTON = 2;
    public static final int FILTER_REMOVE_BASE = 100;
    public static final int FILTER_MAX_DISPLAY = 18;

    private final Runnable toggleOverlay;
    private final MinerBlockEntity blockEntity;
    private final ResourceHandler<ItemResource> handler;

    public MinerMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new ItemStacksResourceHandler(MinerBlockEntity.TOTAL_SLOTS), new SimpleContainerData(16), () -> {}, null);
    }

    public MinerMenu(int id, Inventory playerInventory, MinerBlockEntity be) {
        this(id, playerInventory, be.getItemHandler(), be.getContainerData(), be::toggleOverlay, be);
    }

    public MinerMenu(int id, Inventory playerInventory, ResourceHandler<ItemResource> handler, ContainerData data, Runnable toggleOverlay, MinerBlockEntity be) {
        super(ModMenuTypes.MINER.get(), id);
        this.data = data;
        this.toggleOverlay = toggleOverlay;
        this.blockEntity = be;
        this.handler = handler;
        addDataSlots(data);

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                MinerBlockEntity.RANGE_SLOT, RANGE_X, RANGE_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
            @Override public int getMaxStackSize() { return 1; }
        });

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                MinerBlockEntity.FILTER_SLOT, FILTER_X, FILTER_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
            @Override public int getMaxStackSize() { return 1; }
        });

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                MinerBlockEntity.UPGRADE_SLOT, UPGRADE_X, UPGRADE_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
            @Override public int getMaxStackSize() { return 1; }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        PLAYER_X + col * SLOT_SIZE, PLAYER_Y + row * SLOT_SIZE));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col,
                    PLAYER_X + col * SLOT_SIZE, HOTBAR_Y));
        }
    }

    public ContainerData getData() { return data; }
    public int getTotalMined() { return data.get(MinerBlockEntity.DATA_MINED); }
    public int getTotalBlocks() { return data.get(MinerBlockEntity.DATA_TOTAL_BLOCKS); }
    public boolean isOverlayEnabled() { return data.get(MinerBlockEntity.DATA_OVERLAY) == 1; }
    public int getStatus() { return data.get(MinerBlockEntity.DATA_STATUS); }
    public int getProgressPercent() {
        int total = getTotalBlocks();
        if (total <= 0) return 0;
        long pct = (long) getTotalMined() * 100 / total;
        return (int) Math.min(100, Math.max(0, pct));
    }
    public boolean isOperating() { return getStatus() == MinerBlockEntity.STATUS_RUNNING; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == OVERLAY_BUTTON) {
            toggleOverlay.run();
            return true;
        }
        if (id == FILTER_CLEAR_BUTTON) {
            if (blockEntity != null) blockEntity.clearFilter();
            return true;
        }
        if (id == RESET_BUTTON) {
            if (blockEntity != null) blockEntity.resetMining();
            return true;
        }
        if (id >= FILTER_REMOVE_BASE && id < FILTER_REMOVE_BASE + FILTER_MAX_DISPLAY) {
            if (blockEntity != null) blockEntity.removeFilterEntry(id - FILTER_REMOVE_BASE);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private void noopSet(int index, ItemResource resource, int amount) {
    }
}
