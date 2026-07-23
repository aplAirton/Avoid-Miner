package com.airton.avoidminer.menu;

import com.airton.avoidminer.ModMenuTypes;
import com.airton.avoidminer.block.entity.MobGrinderBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class MobGrinderMenu extends AbstractContainerMenu {
    public static final int RANGE_X = 44;
    public static final int RANGE_Y = 22;
    public static final int DAMAGE_X = 62;
    public static final int DAMAGE_Y = 22;
    public static final int DAMAGE_CARD_X = 80;
    public static final int DAMAGE_CARD_Y = 22;
    public static final int LOOT_X = 98;
    public static final int LOOT_Y = 22;
    public static final int ATTRACTION_X = 116;
    public static final int ATTRACTION_Y = 22;
    public static final int FUEL_X = 150;
    public static final int FUEL_Y = 68;
    public static final int PLAYER_X = 8;
    public static final int PLAYER_Y = 100;
    public static final int HOTBAR_Y = 158;
    public static final int OVERLAY_BUTTON = 0;
    public static final int PACIFIST_BUTTON = 1;

    private final ContainerData data;
    private final Runnable toggleOverlay;
    private final Runnable togglePacifist;

    public ContainerData getData() { return data; }

    public MobGrinderMenu(int id, Inventory inv) {
        this(id, inv, new ItemStacksResourceHandler(MobGrinderBlockEntity.TOTAL_SLOTS),
                new net.minecraft.world.inventory.SimpleContainerData(MobGrinderBlockEntity.DATA_SIZE), () -> {}, () -> {});
    }

    public MobGrinderMenu(int id, Inventory inv, MobGrinderBlockEntity be) {
        this(id, inv, be.getItemHandler(), be.getContainerData(), be::toggleOverlay, be::togglePacifist);
    }

    private MobGrinderMenu(int id, Inventory inv, ResourceHandler<ItemResource> handler,
                           ContainerData data, Runnable toggleOverlay, Runnable togglePacifist) {
        super(ModMenuTypes.MOB_GRINDER.get(), id);
        this.data = data;
        this.toggleOverlay = toggleOverlay;
        this.togglePacifist = togglePacifist;
        addDataSlots(data);

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noop,
                MobGrinderBlockEntity.RANGE_SLOT, RANGE_X, RANGE_Y) {
            @Override public boolean mayPlace(ItemStack s) { return handler.isValid(getSlotIndex(), ItemResource.of(s)); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noop,
                MobGrinderBlockEntity.DAMAGE_SLOT, DAMAGE_X, DAMAGE_Y) {
            @Override public boolean mayPlace(ItemStack s) { return handler.isValid(getSlotIndex(), ItemResource.of(s)); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noop,
                MobGrinderBlockEntity.DAMAGE_CARD_SLOT, DAMAGE_CARD_X, DAMAGE_CARD_Y) {
            @Override public boolean mayPlace(ItemStack s) { return handler.isValid(getSlotIndex(), ItemResource.of(s)); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noop,
                MobGrinderBlockEntity.LOOT_SLOT, LOOT_X, LOOT_Y) {
            @Override public boolean mayPlace(ItemStack s) { return handler.isValid(getSlotIndex(), ItemResource.of(s)); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noop,
                MobGrinderBlockEntity.ATTRACTION_SLOT, ATTRACTION_X, ATTRACTION_Y) {
            @Override public boolean mayPlace(ItemStack s) { return handler.isValid(getSlotIndex(), ItemResource.of(s)); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noop,
                MobGrinderBlockEntity.FUEL_SLOT, FUEL_X, FUEL_Y) {
            @Override public boolean mayPlace(ItemStack s) { return handler.isValid(getSlotIndex(), ItemResource.of(s)); }
            @Override public int getMaxStackSize() { return 64; }
        });

        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++)
            addSlot(new Slot(inv, c + r * 9 + 9, PLAYER_X + c * 18, PLAYER_Y + r * 18));
        for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c, PLAYER_X + c * 18, HOTBAR_Y));
    }

    private void noop(int i, ItemResource r, int a) {}
    public int getRange() { return data.get(MobGrinderBlockEntity.DATA_RANGE); }
    public int getDamage() { return data.get(MobGrinderBlockEntity.DATA_DAMAGE); }
    public boolean isOverlayEnabled() { return data.get(MobGrinderBlockEntity.DATA_OVERLAY) == 1; }
    public boolean isPacifist() { return data.get(MobGrinderBlockEntity.DATA_PACIFIST) == 1; }
    public int getEnergyStored() { return data.get(MobGrinderBlockEntity.DATA_ENERGY); }
    public int getEnergyCapacity() { return data.get(MobGrinderBlockEntity.DATA_ENERGY_CAP); }
    public int getPosX() { return data.get(MobGrinderBlockEntity.DATA_POS_X); }
    public int getPosY() { return data.get(MobGrinderBlockEntity.DATA_POS_Y); }
    public int getPosZ() { return data.get(MobGrinderBlockEntity.DATA_POS_Z); }

    @Override public boolean clickMenuButton(Player p, int id) {
        if (id == OVERLAY_BUTTON) { toggleOverlay.run(); return true; }
        if (id == PACIFIST_BUTTON) { togglePacifist.run(); return true; }
        return false; }
    @Override public ItemStack quickMoveStack(Player p, int i) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player p) { return true; }
}
