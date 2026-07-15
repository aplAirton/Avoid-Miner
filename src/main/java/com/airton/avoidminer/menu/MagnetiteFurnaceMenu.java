package com.airton.avoidminer.menu;

import com.airton.avoidminer.ModMenuTypes;
import com.airton.avoidminer.block.MagnetiteFurnaceBlock;
import com.airton.avoidminer.block.entity.MagnetiteFurnaceBlockEntity;
import com.airton.avoidminer.block.entity.MagnetiteFurnaceBlockEntity.Tier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

public final class MagnetiteFurnaceMenu extends AbstractContainerMenu {
    public static final int AUTO_BALANCE_BUTTON = 0;
    public static final int TEXTURE_W = 288;
    public static final int TEXTURE_H = 206;
    public static final int MAIN_X = 94;
    public static final int MAIN_RIGHT = 256;
    public static final int MAIN_CENTER_X = (MAIN_X + MAIN_RIGHT) / 2;
    public static final int ENERGY_X = 268;
    public static final int ENERGY_Y = 18;
    public static final int ENERGY_W = 12;
    public static final int ENERGY_H = 54;
    public static final int FUEL_X = 265;
    public static final int FUEL_Y = 80;
    public static final int AUTO_BALANCE_X = 266;
    public static final int AUTO_BALANCE_Y = 101;
    public static final int AUTO_BALANCE_SIZE = 16;
    public static final int COLUMN_SPACING = 26;
    public static final int INPUT_Y = 22;
    public static final int ARROW_Y = 42;
    public static final int ARROW_H = 16;
    public static final int OUTPUT_Y = 62;
    public static final int T1_ROW_Y = 40;
    public static final int T1_INPUT_X = 139;
    public static final int T1_ARROW_X = 161;
    public static final int T1_ARROW_W = 28;
    public static final int T1_ARROW_ROW_H = 12;
    public static final int T1_OUTPUT_X = 193;
    public static final int UPG_Y = 90;
    public static final int UPG_ENERGY_X = 151;
    public static final int UPG_SPEED_X = 181;
    public static final int PLAYER_Y = 122;
    public static final int HOTBAR_Y = 180;

    private final ContainerData data;
    private final Tier tier;
    private final Runnable toggleAutoBalance;

    public static int inputSlotX(Tier tier, int index) {
        if (tier.inputCount == 1) return T1_INPUT_X;
        int totalWidth = tier.inputCount * 18 + (tier.inputCount - 1) * (COLUMN_SPACING - 18);
        return MAIN_CENTER_X - totalWidth / 2 + index * COLUMN_SPACING;
    }

    public static int inputSlotY(Tier tier) {
        return tier.inputCount == 1 ? T1_ROW_Y : INPUT_Y;
    }

    public static int outputSlotX(Tier tier, int index) {
        return tier.inputCount == 1 ? T1_OUTPUT_X : inputSlotX(tier, index);
    }

    public static int outputSlotY(Tier tier) {
        return tier.inputCount == 1 ? T1_ROW_Y : OUTPUT_Y;
    }

    public MagnetiteFurnaceMenu(int id, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(id, inventory, readTier(inventory, extraData));
    }

    private MagnetiteFurnaceMenu(int id, Inventory inventory, Tier tier) {
        this(id, inventory, new ItemStacksResourceHandler(tier.getTotalSlots()),
                new SimpleContainerData(MagnetiteFurnaceBlockEntity.DATA_SIZE), tier, () -> {});
    }

    private static Tier readTier(Inventory inventory, RegistryFriendlyByteBuf extraData) {
        if (extraData != null) {
            BlockPos pos = extraData.readBlockPos();
            if (inventory.player.level().getBlockState(pos).getBlock() instanceof MagnetiteFurnaceBlock block) {
                return block.getTier();
            }
        }
        return Tier.TIER_1;
    }

    public MagnetiteFurnaceMenu(int id, Inventory inventory, ResourceHandler<ItemResource> handler,
                                ContainerData data, Tier tier, Runnable toggleAutoBalance) {
        super(ModMenuTypes.MAGNETITE_FURNACE.get(), id);
        this.data = data;
        this.tier = tier;
        this.toggleAutoBalance = toggleAutoBalance;

        addMachineSlot(handler, MagnetiteFurnaceBlockEntity.FUEL_SLOT, FUEL_X, FUEL_Y, true, false);
        for (int i = 0; i < tier.inputCount; i++) {
            addMachineSlot(handler, tier.getInputStart() + i,
                    inputSlotX(tier, i), inputSlotY(tier), true, false);
        }
        for (int i = 0; i < tier.inputCount; i++) {
            addMachineSlot(handler, tier.getOutputStart() + i,
                    outputSlotX(tier, i), outputSlotY(tier), false, false);
        }
        addMachineSlot(handler, tier.getEnergyUpgradeSlot(), UPG_ENERGY_X, UPG_Y, true, true);
        addMachineSlot(handler, tier.getSpeedUpgradeSlot(), UPG_SPEED_X, UPG_Y, true, true);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        MAIN_X + column * 18, PLAYER_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, MAIN_X + column * 18, HOTBAR_Y));
        }
        addDataSlots(data);
    }

    private void addMachineSlot(ResourceHandler<ItemResource> handler, int index, int x, int y,
                                boolean acceptsItems, boolean singleItem) {
        addSlot(new ResourceHandlerSlot(handler,
                handler instanceof ItemStacksResourceHandler stacks ? stacks::set : this::noopSet,
                index, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return acceptsItems && handler.isValid(getSlotIndex(), ItemResource.of(stack));
            }

            @Override
            public int getMaxStackSize() {
                return singleItem ? 1 : super.getMaxStackSize();
            }
        });
    }

    private void noopSet(int index, ItemResource resource, int amount) {
    }

    public Tier getTier() { return tier; }
    public boolean isBurning() { return data.get(0) > 0; }
    public int getEnergyStored() { return data.get(0); }
    public int getEnergyCapacity() { return data.get(1); }
    public int getMaxProgress() { return data.get(2); }
    public int getMachineTier() { return data.get(3); }
    public int getBaseTicksPerProcess() { return data.get(5); }
    public int getInputProgress(int index) {
        return index >= 0 && index < MagnetiteFurnaceBlockEntity.MAX_INPUTS
                ? data.get(MagnetiteFurnaceBlockEntity.DATA_PROGRESS + index) : 0;
    }
    public boolean isOutputBlocked() { return data.get(MagnetiteFurnaceBlockEntity.DATA_STALLED) == 1; }
    public int getEnergyUpgradeTier() { return data.get(MagnetiteFurnaceBlockEntity.DATA_ENERGY_UPG); }
    public int getSpeedUpgradeTier() { return data.get(MagnetiteFurnaceBlockEntity.DATA_SPEED_UPG); }
    public boolean isAutoBalanceEnabled() {
        return data.get(MagnetiteFurnaceBlockEntity.DATA_AUTO_BALANCE) == 1;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != AUTO_BALANCE_BUTTON || tier.inputCount <= 1) return false;
        toggleAutoBalance.run();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return original;

        ItemStack moving = slot.getItem();
        original = moving.copy();
        int machineSlots = tier.getTotalSlots();
        if (index < machineSlots) {
            if (!moveItemStackTo(moving, machineSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(moving, tier.getInputStart(), tier.getInputEnd() + 1, false)
                && !moveItemStackTo(moving, tier.getEnergyUpgradeSlot(), tier.getSpeedUpgradeSlot() + 1, false)
                && !moveItemStackTo(moving, MagnetiteFurnaceBlockEntity.FUEL_SLOT,
                MagnetiteFurnaceBlockEntity.FUEL_SLOT + 1, false)) {
            return ItemStack.EMPTY;
        }

        if (moving.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
