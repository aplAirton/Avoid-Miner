package com.airton.avoidminer.menu;

import com.airton.avoidminer.ModMenuTypes;
import com.airton.avoidminer.block.entity.ResonantRepairStationBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public final class ResonantRepairStationMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 2;
    private final ContainerData data;

    public ResonantRepairStationMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new ItemStacksResourceHandler(MACHINE_SLOTS), new SimpleContainerData(2));
    }

    public ResonantRepairStationMenu(int id, Inventory playerInventory,
                                     ResourceHandler<ItemResource> handler, ContainerData data) {
        super(ModMenuTypes.RESONANT_REPAIR_STATION.get(), id);
        this.data = data;
        IndexModifier<ItemResource> setter = handler instanceof ItemStacksResourceHandler stacks
                ? stacks::set : this::noopSet;
        addSlot(new ResourceHandlerSlot(handler, setter,
                ResonantRepairStationBlockEntity.CRYSTAL_SLOT, 44, 35) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(getSlotIndex(), ItemResource.of(stack));
            }
        });
        addSlot(new ResourceHandlerSlot(handler, setter,
                ResonantRepairStationBlockEntity.EQUIPMENT_SLOT, 116, 35) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(getSlotIndex(), ItemResource.of(stack));
            }
            @Override public int getMaxStackSize() { return 1; }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
        addDataSlots(data);
    }

    private void noopSet(int slot, ItemResource resource, int amount) {}
    public int progress() { return data.get(0); }
    public int maxProgress() { return Math.max(1, data.get(1)); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();
        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(original, MACHINE_SLOTS, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(original, 0, MACHINE_SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (original.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override public boolean stillValid(Player player) { return true; }
}
