package com.airton.avoidminer.menu;

import com.airton.avoidminer.ModMenuTypes;
import com.airton.avoidminer.block.entity.LootrBlockEntity;
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

public class LootrMenu extends AbstractContainerMenu {
    private final ContainerData data;

    // Geometria compartilhada com a LootrScreen (textura 288x206).
    // Fileira y=90: cartão dourado à esquerda, chevrons, 3 melhorias dedicadas à direita.
    public static final int TEXTURE_W = 288;
    public static final int TEXTURE_H = 206;
    public static final int PANEL_W = 88;
    public static final int MAIN_X = 94;
    public static final int MAIN_RIGHT = 256;
    public static final int OUT_Y = 20;
    public static final int UPG_Y = 90;
    public static final int UPG_SPACING = 22;
    public static final int UPG_X = 194;      // primeiro slot de melhoria (194/216/238)
    public static final int CARD_X = 100;
    public static final int CARD_Y = 90;
    public static final int ENERGY_X = 268;
    public static final int ENERGY_Y = 18;
    public static final int ENERGY_W = 12;
    public static final int ENERGY_H = 54;
    public static final int FUEL_X = 265;
    public static final int FUEL_Y = 80;
    public static final int PLAYER_Y = 122;
    public static final int HOTBAR_Y = 180;

    // Índices na lista de slots do menu (ordem de addSlot), usados pela screen
    public static final int SLOT_INDEX_CARD = 1;
    public static final int SLOT_INDEX_UPG_SPEED = 2;
    public static final int SLOT_INDEX_UPG_LOOT = 3;
    public static final int SLOT_INDEX_UPG_RARITY = 4;

    public LootrMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new ItemStacksResourceHandler(LootrBlockEntity.TOTAL_SLOTS), new SimpleContainerData(11));
    }

    public LootrMenu(int id, Inventory playerInventory, ResourceHandler<ItemResource> handler, ContainerData data) {
        super(ModMenuTypes.LOOTR.get(), id);
        this.data = data;

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                LootrBlockEntity.FUEL_SLOT, FUEL_X, FUEL_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
        });

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                LootrBlockEntity.CARD_SLOT, CARD_X, CARD_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
        });

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                LootrBlockEntity.UPG_SPEED, UPG_X + 0 * UPG_SPACING, UPG_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
            @Override public int getMaxStackSize() { return 1; }
        });

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                LootrBlockEntity.UPG_LOOT, UPG_X + 1 * UPG_SPACING, UPG_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
            @Override public int getMaxStackSize() { return 1; }
        });

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                LootrBlockEntity.UPG_RARITY, UPG_X + 2 * UPG_SPACING, UPG_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
            @Override public int getMaxStackSize() { return 1; }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = LootrBlockEntity.OUTPUT_START + row * 9 + col;
                addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                        index, MAIN_X + col * 18, OUT_Y + row * 18) {
                    @Override public boolean mayPlace(ItemStack stack) { return false; }
                });
            }
        }

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
    public int getProgress() { return data.get(2); }
    public int getMaxProgress() { return data.get(3); }
    public boolean hasRarityUpgrade() { return data.get(4) == 1; }
    public int getLootingLevel() { return data.get(5); }
    public boolean hasValidCard() { return data.get(6) == 1; }
    public boolean hasCard() { return data.get(10) == 1; }
    public boolean isOutputFull() { return data.get(7) == 1; }
    public int getSpeedUpgradeTier() { return data.get(8); }
    public boolean lastOperationSucceeded() { return data.get(9) == 1; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            stack = stackInSlot.copy();
            int total = LootrBlockEntity.TOTAL_SLOTS;
            if (index < total) {
                if (!moveItemStackTo(stackInSlot, total, slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (stackInSlot.getItem() instanceof com.airton.avoidminer.lootr.MobCardItem) {
                    if (!moveItemStackTo(stackInSlot, LootrBlockEntity.CARD_SLOT, LootrBlockEntity.CARD_SLOT + 1, false))
                        return ItemStack.EMPTY;
                } else {
                    FuelValues fuels = player.level().fuelValues();
                    boolean isFuel = stackInSlot.getBurnTime(RecipeType.SMELTING, fuels) > 0;
                    if (isFuel) {
                        if (!moveItemStackTo(stackInSlot, LootrBlockEntity.FUEL_SLOT, LootrBlockEntity.FUEL_SLOT + 1, false))
                            return ItemStack.EMPTY;
                    }
                    if (!moveItemStackTo(stackInSlot, LootrBlockEntity.UPG_SPEED, LootrBlockEntity.UPG_RARITY + 1, false)) {
                        if (!moveItemStackTo(stackInSlot, LootrBlockEntity.OUTPUT_START, LootrBlockEntity.OUTPUT_START + LootrBlockEntity.OUTPUT_COUNT, false))
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
