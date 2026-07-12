package com.airton.avoidminer.menu;

import com.airton.avoidminer.ModMenuTypes;
import com.airton.avoidminer.block.entity.BatteryBlockEntity;
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

public class BatteryMenu extends AbstractContainerMenu {
    private final ContainerData data;

    // Geometria compartilhada com a BatteryScreen (textura 288x206)
    public static final int TEXTURE_W = 288;
    public static final int TEXTURE_H = 206;
    public static final int MAIN_X = 94;
    public static final int MAIN_RIGHT = 256;
    public static final int GAUGE_X = 100;
    public static final int GAUGE_Y = 30;
    public static final int GAUGE_W = 150;
    public static final int GAUGE_H = 38;
    public static final int GAUGE_SEGMENTS = 12;
    public static final int ENERGY_X = 268;
    public static final int ENERGY_Y = 18;
    public static final int ENERGY_W = 12;
    public static final int ENERGY_H = 54;
    public static final int FUEL_X = 265;
    public static final int FUEL_Y = 80;
    public static final int CAPACITY_UPG_X = 150;
    public static final int CAPACITY_UPG_Y = 58;
    public static final int RANGE_UPG_X = 180;
    public static final int RANGE_UPG_Y = 58;
    public static final int PLAYER_Y = 122;
    public static final int HOTBAR_Y = 180;

    public BatteryMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new ItemStacksResourceHandler(BatteryBlockEntity.TOTAL_SLOTS),
                new SimpleContainerData(BatteryBlockEntity.DATA_SIZE));
    }

    public BatteryMenu(int id, Inventory playerInventory, ResourceHandler<ItemResource> handler, ContainerData data) {
        super(ModMenuTypes.BATTERY.get(), id);
        this.data = data;

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                BatteryBlockEntity.FUEL_SLOT, FUEL_X, FUEL_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
        });

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                BatteryBlockEntity.CAPACITY_UPGRADE_SLOT, CAPACITY_UPG_X, CAPACITY_UPG_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
        });

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                BatteryBlockEntity.RANGE_UPGRADE_SLOT, RANGE_UPG_X, RANGE_UPG_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, MAIN_X + col * 18, PLAYER_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, MAIN_X + col * 18, HOTBAR_Y));
        }

        addDataSlots(data);
    }

    private void noopSet(int index, ItemResource resource, int amount) {}

    // Energia sincronizada em duas metades de 16 bits (DataSlots truncam para short)
    public int getEnergyStored() {
        return (data.get(BatteryBlockEntity.DATA_ENERGY_HI) << 16)
                | (data.get(BatteryBlockEntity.DATA_ENERGY_LO) & 0xFFFF);
    }

    public int getEffectiveCapacity() {
        int capTier = data.get(BatteryBlockEntity.DATA_CAPACITY_TIER);
        return BatteryBlockEntity.ENERGY_CAPACITY * (1 << capTier);
    }
    public int getActiveLinks() { return data.get(BatteryBlockEntity.DATA_LINKS); }
    public int getCapacityUpgradeTier() { return data.get(BatteryBlockEntity.DATA_CAPACITY_TIER); }
    public int getRangeUpgradeTier() { return data.get(BatteryBlockEntity.DATA_RANGE_TIER); }
    public boolean isCharging() { return data.get(BatteryBlockEntity.DATA_CHARGING) == 1; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            stack = stackInSlot.copy();
            if (index < BatteryBlockEntity.TOTAL_SLOTS) {
                if (!moveItemStackTo(stackInSlot, BatteryBlockEntity.TOTAL_SLOTS, slots.size(), true))
                    return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(stackInSlot, BatteryBlockEntity.FUEL_SLOT, BatteryBlockEntity.FUEL_SLOT + 1, false)
                        && !moveItemStackTo(stackInSlot, BatteryBlockEntity.CAPACITY_UPGRADE_SLOT, BatteryBlockEntity.TOTAL_SLOTS, false))
                    return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return stack;
    }

    @Override public boolean stillValid(Player player) { return true; }
}
