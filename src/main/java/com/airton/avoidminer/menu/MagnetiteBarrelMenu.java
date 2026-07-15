package com.airton.avoidminer.menu;

import com.airton.avoidminer.ModMenuTypes;
import com.airton.avoidminer.block.entity.MagnetiteBarrelBlockEntity;
import net.minecraft.server.level.ServerPlayer;
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
import org.jetbrains.annotations.Nullable;

public class MagnetiteBarrelMenu extends AbstractContainerMenu {
    /** IDs dos botões da GUI (handleInventoryButtonClick). */
    public static final int BUTTON_XP_100 = 0;
    public static final int BUTTON_XP_ALL = 1;

    // Geometria compartilhada com a MagnetiteBarrelScreen (textura 262x276)
    public static final int TEXTURE_W = 262;
    public static final int TEXTURE_H = 276;
    public static final int PANEL_W = 88;
    public static final int MAIN_X = 94;
    public static final int MAIN_RIGHT = 256;
    public static final int GRID_Y = 18;
    public static final int UPG_ITEM_X = 9;
    public static final int UPG_XP_X = 35;
    public static final int UPG_STACK_X = 61;
    public static final int UPG_Y = 116;
    public static final int BTN_X = 6;
    public static final int BTN_W = 76;
    public static final int BTN_H = 14;
    public static final int BTN_100_Y = 52;
    public static final int BTN_ALL_Y = 72;
    public static final int PLAYER_Y = 194;
    public static final int HOTBAR_Y = 252;

    private final ContainerData data;
    @Nullable private final MagnetiteBarrelBlockEntity barrel;

    public MagnetiteBarrelMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new ItemStacksResourceHandler(MagnetiteBarrelBlockEntity.TOTAL_SLOTS),
                new SimpleContainerData(MagnetiteBarrelBlockEntity.DATA_SIZE), null);
    }

    public MagnetiteBarrelMenu(int id, Inventory playerInventory, ResourceHandler<ItemResource> handler,
                               ContainerData data, @Nullable MagnetiteBarrelBlockEntity barrel) {
        super(ModMenuTypes.MAGNETITE_BARREL.get(), id);
        this.data = data;
        this.barrel = barrel;

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int index = MagnetiteBarrelBlockEntity.STORAGE_START + row * 9 + col;
                addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                        index, MAIN_X + col * 18, GRID_Y + row * 18) {
                    @Override public boolean mayPlace(ItemStack stack) {
                        return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
                    }
                });
            }
        }

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                MagnetiteBarrelBlockEntity.UPG_XP_SLOT, UPG_XP_X, UPG_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
        });
        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                MagnetiteBarrelBlockEntity.UPG_STACK_SLOT, UPG_STACK_X, UPG_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
        });
        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                MagnetiteBarrelBlockEntity.UPG_ITEM_SLOT, UPG_ITEM_X, UPG_Y) {
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

    // XP em pontos sincronizado em duas metades de 16 bits
    public int getStoredXp() {
        return (data.get(MagnetiteBarrelBlockEntity.DATA_XP_HI) << 16)
                | (data.get(MagnetiteBarrelBlockEntity.DATA_XP_LO) & 0xFFFF);
    }

    public int getUsedSlots() { return data.get(MagnetiteBarrelBlockEntity.DATA_SLOTS_USED); }
    public boolean hasXpUpgrade() { return data.get(MagnetiteBarrelBlockEntity.DATA_HAS_XP_UPG) == 1; }
    public boolean hasStackUpgrade() { return data.get(MagnetiteBarrelBlockEntity.DATA_HAS_STACK_UPG) == 1; }
    public boolean hasItemUpgrade() { return data.get(MagnetiteBarrelBlockEntity.DATA_HAS_ITEM_UPG) == 1; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (barrel == null || !(player instanceof ServerPlayer serverPlayer)) return false;
        switch (id) {
            case BUTTON_XP_100 -> barrel.payOutXp(serverPlayer, 100);
            case BUTTON_XP_ALL -> barrel.payOutXp(serverPlayer, -1);
            default -> { return false; }
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            stack = stackInSlot.copy();
            int total = MagnetiteBarrelBlockEntity.TOTAL_SLOTS;
            if (index < total) {
                if (!moveItemStackTo(stackInSlot, total, slots.size(), true)) return ItemStack.EMPTY;
            } else {
                // upgrades primeiro (mayPlace filtra), depois o armazenamento
                if (!moveItemStackTo(stackInSlot, MagnetiteBarrelBlockEntity.UPG_XP_SLOT,
                        MagnetiteBarrelBlockEntity.UPG_ITEM_SLOT + 1, false)
                        && !moveItemStackTo(stackInSlot, MagnetiteBarrelBlockEntity.STORAGE_START,
                        MagnetiteBarrelBlockEntity.STORAGE_START + MagnetiteBarrelBlockEntity.STORAGE_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return stack;
    }

    @Override public boolean stillValid(Player player) { return true; }
}
