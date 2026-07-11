package com.airton.avoidminer.menu;

import com.airton.avoidminer.ModMenuTypes;
import com.airton.avoidminer.block.entity.AvoidMinerBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class AvoidMinerMenu extends AbstractContainerMenu {
    private final ContainerData data;

    // Geometria compartilhada com a AvoidMinerScreen (textura 288x206)
    public static final int TEXTURE_W = 288;
    public static final int TEXTURE_H = 206;
    public static final int PANEL_W = 88;
    public static final int MAIN_X = 94;
    public static final int MAIN_RIGHT = 256;
    public static final int OUT_Y = 20;
    public static final int UPG_Y = 90;
    public static final int UPG_SPACING = 22;
    public static final int WORLD_X = 200;
    public static final int WORLD_Y = 90;
    public static final int ENCHANT_X = 238;
    public static final int ENCHANT_Y = 90;
    public static final int ENERGY_X = 268;
    public static final int ENERGY_Y = 18;
    public static final int ENERGY_W = 12;
    public static final int ENERGY_H = 54;
    public static final int FUEL_X = 265;
    public static final int FUEL_Y = 80;
    public static final int PLAYER_Y = 122;
    public static final int HOTBAR_Y = 180;

    public AvoidMinerMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new ItemStacksResourceHandler(AvoidMinerBlockEntity.TOTAL_SLOTS), new SimpleContainerData(16));
    }

    public AvoidMinerMenu(int id, Inventory playerInventory, ResourceHandler<ItemResource> handler, ContainerData data) {
        super(ModMenuTypes.AVOID_MINER.get(), id);
        this.data = data;

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                AvoidMinerBlockEntity.FUEL_SLOT, FUEL_X, FUEL_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = AvoidMinerBlockEntity.OUTPUT_START + row * 9 + col;
                addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                        index, MAIN_X + col * 18, OUT_Y + row * 18) {
                    @Override public boolean mayPlace(ItemStack stack) { return false; }
                });
            }
        }

        for (int i = 0; i < 3; i++) {
            int slotIndex = AvoidMinerBlockEntity.UPGRADE_SLOT_START + i;
            addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                    slotIndex, MAIN_X + i * UPG_SPACING, UPG_Y) {
                @Override public boolean mayPlace(ItemStack stack) {
                    return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
                }
                @Override public int getMaxStackSize() { return 1; }
            });
        }

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                AvoidMinerBlockEntity.WORLD_SLOT, WORLD_X, WORLD_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
            @Override public int getMaxStackSize() { return 1; }
        });

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                AvoidMinerBlockEntity.ENCHANT_SLOT, ENCHANT_X, ENCHANT_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
            @Override public int getMaxStackSize() { return 1; }
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

    public boolean isBurning() { return data.get(0) > 0; }
    public int getEnergyStored() { return data.get(0); }
    public int getEnergyCapacity() { return data.get(1); }
    public int getRawProgress() { return data.get(2); }
    public int getMaxProgress() { return data.get(3); }
    public int getUpgradeType(int slotIndex) { return data.get(4 + slotIndex * 2); }
    public int getUpgradeTier(int slotIndex) { return data.get(5 + slotIndex * 2); }
    public int getUpgradeSlotCount() { return data.get(10); }
    public int getMachineTier() { return data.get(11); }
    public int getEnergyCostPerGeneration() { return data.get(12); }
    public int getWorldMode() { return data.get(13); }
    public int getEnchantMode() { return data.get(14); }
    public boolean isOutputFull() { return data.get(15) == 1; }
    public ContainerData getData() { return data; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            stack = stackInSlot.copy();
            if (index < AvoidMinerBlockEntity.TOTAL_SLOTS) {
                if (!moveItemStackTo(stackInSlot, AvoidMinerBlockEntity.TOTAL_SLOTS, slots.size(), true))
                    return ItemStack.EMPTY;
            } else {
                FuelValues fuels = player.level().fuelValues();
                boolean isFuel = stackInSlot.getBurnTime(RecipeType.SMELTING, fuels) > 0;
                if (isFuel) {
                    if (!moveItemStackTo(stackInSlot, AvoidMinerBlockEntity.FUEL_SLOT, AvoidMinerBlockEntity.FUEL_SLOT + 1, false)) {
                        if (!moveItemStackTo(stackInSlot, AvoidMinerBlockEntity.UPGRADE_SLOT_START, AvoidMinerBlockEntity.TOTAL_SLOTS, false)) {
                            if (!moveItemStackTo(stackInSlot, AvoidMinerBlockEntity.OUTPUT_START, AvoidMinerBlockEntity.UPGRADE_SLOT_START, false))
                                return ItemStack.EMPTY;
                        }
                    }
                } else {
                    if (!moveItemStackTo(stackInSlot, AvoidMinerBlockEntity.UPGRADE_SLOT_START, AvoidMinerBlockEntity.TOTAL_SLOTS, false)) {
                        if (!moveItemStackTo(stackInSlot, AvoidMinerBlockEntity.OUTPUT_START, AvoidMinerBlockEntity.UPGRADE_SLOT_START, false))
                            return ItemStack.EMPTY;
                    }
                }
            }
            if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return stack;
    }

    @Override public boolean stillValid(Player player) { return true; }
}