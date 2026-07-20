package com.airton.avoidminer.menu;

import com.airton.avoidminer.ModMenuTypes;
import com.airton.avoidminer.item.FilterCardItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class FilterCardMenu extends AbstractContainerMenu {
    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 204;

    public static final int SEARCH_X = 8;
    public static final int SEARCH_Y = 18;
    public static final int SEARCH_W = 160;
    public static final int SEARCH_H = 14;

    public static final int GRID_X = 8;
    public static final int GRID_Y = 50;
    public static final int GRID_COLS = 9;
    public static final int GRID_ROWS = 2;
    public static final int CELL_SIZE = 18;

    public static final int PLAYER_INV_X = 8;
    public static final int PLAYER_INV_Y = 100;
    public static final int HOTBAR_Y = 158;

    public static final int BUTTON_REMOVE_BASE = 0;
    public static final int BUTTON_ADD_BASE = 20000;

    private final Player player;

    public FilterCardMenu(int id, Inventory inv) {
        super(ModMenuTypes.FILTER_CARD.get(), id);
        this.player = inv.player;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9,
                        PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, PLAYER_INV_X + col * 18, HOTBAR_Y));
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        ItemStack stack = findFilterCard(player);
        if (stack.isEmpty()) return false;

        if (id >= BUTTON_REMOVE_BASE && id < BUTTON_REMOVE_BASE + FilterCardItem.MAX_ENTRIES) {
            FilterCardItem.removeEntry(stack, id - BUTTON_REMOVE_BASE);
            return true;
        }
        if (id >= BUTTON_ADD_BASE) {
            int blockId = id - BUTTON_ADD_BASE;
            Block block = BuiltInRegistries.BLOCK.byId(blockId);
            if (block != Blocks.AIR) {
                Identifier identifier = BuiltInRegistries.BLOCK.getKey(block);
                FilterCardItem.addEntry(stack, identifier);
            }
            return true;
        }
        return false;
    }

    private static ItemStack findFilterCard(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof FilterCardItem) return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
