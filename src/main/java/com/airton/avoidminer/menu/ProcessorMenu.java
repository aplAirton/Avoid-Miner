package com.airton.avoidminer.menu;

import com.airton.avoidminer.ModMenuTypes;
import com.airton.avoidminer.block.entity.ProcessorBlockEntity;
import com.airton.avoidminer.block.entity.ProcessorBlockEntity.Tier;
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

public class ProcessorMenu extends AbstractContainerMenu {
    private final ContainerData data;

    public ProcessorMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new ItemStacksResourceHandler(37), new SimpleContainerData(14), Tier.TIER_3);
    }

    public static final int PANEL_W = 64;
    public static final int MAIN_X = PANEL_W + 8;
    public static final int FUEL_X = PANEL_W + 44;
    public static final int FUEL_Y = 18;
    public static final int OUT_Y = 38;
    public static final int INPUT_Y = 96;
    public static final int INPUT_Y2 = 114;
    public static final int UPGRADE_X = 130;
    public static final int UPG_Y = 96;

    private final Tier tier;

    public ProcessorMenu(int id, Inventory playerInventory, ResourceHandler<ItemResource> handler, ContainerData data, Tier tier) {
        super(ModMenuTypes.PROCESSOR.get(), id);
        this.data = data;
        this.tier = tier;

        int inputStart = tier.getInputStart();
        int inputEnd = tier.getInputEnd();
        int outputStart = tier.getOutputStart();
        int outputEnd = tier.getOutputEnd();
        int upgradeStart = tier.getUpgradeStart();
        int upgradeEnd = tier.getUpgradeEnd();
        int playerY = tier == Tier.TIER_3 ? 136 : 120;
        int hotbarY = tier == Tier.TIER_3 ? 198 : 182;

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                ProcessorBlockEntity.FUEL_SLOT, FUEL_X, FUEL_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
        });

        for (int slot = inputStart; slot <= inputEnd; slot++) {
            int inputIndex = slot - inputStart;
            int ix, iy;
            if (tier == Tier.TIER_3 && inputIndex >= 3) {
                int col = inputIndex - 3;
                ix = MAIN_X + col * 18;
                iy = INPUT_Y2;
            } else {
                ix = MAIN_X + inputIndex * 18;
                iy = INPUT_Y;
            }
            final int s = slot;
            addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                    s, ix, iy) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
                }
            });
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = outputStart + row * 9 + col;
                addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                        index, MAIN_X + col * 18, OUT_Y + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
        }

        for (int slot = upgradeStart; slot <= upgradeEnd; slot++) {
            int upgIndex = slot - upgradeStart;
            int ux = UPGRADE_X + upgIndex * 18;
            final int s = slot;
            addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                    s, ux, UPG_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
                }
            });
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, MAIN_X + col * 18, playerY + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, MAIN_X + col * 18, hotbarY));
        }

        addDataSlots(data);
    }

    private void noopSet(int index, ItemResource resource, int amount) {
    }

    public boolean isBurning() { return data.get(0) > 0; }
    public int getEnergyStored() { return data.get(0); }
    public int getEnergyCapacity() { return data.get(1); }
    public int getRawProgress() { return data.get(2); }
    public int getMaxProgress() { return data.get(3); }
    public int getUpgradeType(int slotIndex) { return data.get(4 + slotIndex * 2); }
    public int getUpgradeTier(int slotIndex) { return data.get(5 + slotIndex * 2); }
    public int getUpgradeSlotCount() { return data.get(10); }
    public int getMachineTier() { return data.get(11); }
    public int getEnergyCostPerProcess() { return data.get(12); }
    public boolean isOutputFull() { return data.get(13) == 1; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            stack = stackInSlot.copy();

            int totalSlots = tier.getTotalSlots();

            if (index < totalSlots) {
                if (!moveItemStackTo(stackInSlot, totalSlots, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                FuelValues fuels = player.level().fuelValues();
                boolean isFuel = stackInSlot.getBurnTime(RecipeType.SMELTING, fuels) > 0;
                if (isFuel) {
                    if (!moveItemStackTo(stackInSlot, ProcessorBlockEntity.FUEL_SLOT, ProcessorBlockEntity.FUEL_SLOT + 1, false)) {
                        if (!moveItemStackTo(stackInSlot, tier.getInputStart(), tier.getInputEnd() + 1, false)) {
                            if (!moveItemStackTo(stackInSlot, tier.getUpgradeStart(), tier.getUpgradeEnd() + 1, false)) {
                                if (!moveItemStackTo(stackInSlot, tier.getOutputStart(), tier.getOutputEnd() + 1, false)) {
                                    return ItemStack.EMPTY;
                                }
                            }
                        }
                    }
                } else {
                    if (!moveItemStackTo(stackInSlot, tier.getInputStart(), tier.getInputEnd() + 1, false)) {
                        if (!moveItemStackTo(stackInSlot, tier.getUpgradeStart(), tier.getUpgradeEnd() + 1, false)) {
                            if (!moveItemStackTo(stackInSlot, tier.getOutputStart(), tier.getOutputEnd() + 1, false)) {
                                return ItemStack.EMPTY;
                            }
                        }
                    }
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return stack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
