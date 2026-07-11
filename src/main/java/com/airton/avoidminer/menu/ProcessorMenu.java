package com.airton.avoidminer.menu;

import com.airton.avoidminer.ModMenuTypes;
import com.airton.avoidminer.block.ProcessorBlock;
import com.airton.avoidminer.block.entity.ProcessorBlockEntity;
import com.airton.avoidminer.block.entity.ProcessorBlockEntity.Tier;
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

public class ProcessorMenu extends AbstractContainerMenu {
    private final ContainerData data;
    private final Tier tier;

    // Geometria compartilhada com a ProcessorScreen (textura 288x206)
    public static final int TEXTURE_W = 288;
    public static final int TEXTURE_H = 206;
    public static final int PANEL_W = 88;
    public static final int MAIN_X = 94;
    public static final int MAIN_RIGHT = 256;
    public static final int MAIN_CENTER_X = (MAIN_X + MAIN_RIGHT) / 2;

    public static final int ENERGY_X = 268;
    public static final int ENERGY_Y = 18;
    public static final int ENERGY_W = 12;
    public static final int ENERGY_H = 54;
    public static final int FUEL_X = 265;
    public static final int FUEL_Y = 80;

    // Tiers 2/3: colunas verticais (entrada > progresso > saída), estilo Mekanism
    public static final int COLUMN_SPACING = 26;
    public static final int INPUT_Y = 22;
    public static final int ARROW_Y = 42;
    public static final int ARROW_H = 16;
    public static final int OUTPUT_Y = 62;

    // Tier 1: linha horizontal (entrada > seta > saída)
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

    public static int inputSlotX(Tier t, int i) {
        if (t.inputCount == 1) return T1_INPUT_X;
        int totalW = t.inputCount * 18 + (t.inputCount - 1) * (COLUMN_SPACING - 18);
        return MAIN_CENTER_X - totalW / 2 + i * COLUMN_SPACING;
    }

    public static int inputSlotY(Tier t) {
        return t.inputCount == 1 ? T1_ROW_Y : INPUT_Y;
    }

    public static int outputSlotX(Tier t, int i) {
        return t.inputCount == 1 ? T1_OUTPUT_X : inputSlotX(t, i);
    }

    public static int outputSlotY(Tier t) {
        return t.inputCount == 1 ? T1_ROW_Y : OUTPUT_Y;
    }

    public ProcessorMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(id, playerInventory, readTier(playerInventory, extraData));
    }

    private ProcessorMenu(int id, Inventory playerInventory, Tier tier) {
        this(id, playerInventory, new ItemStacksResourceHandler(tier.getTotalSlots()),
                new SimpleContainerData(ProcessorBlockEntity.DATA_SIZE), tier);
    }

    private static Tier readTier(Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        if (extraData != null) {
            BlockPos pos = extraData.readBlockPos();
            if (playerInventory.player.level().getBlockState(pos).getBlock() instanceof ProcessorBlock block) {
                return block.getTier();
            }
        }
        return Tier.TIER_1;
    }

    public ProcessorMenu(int id, Inventory playerInventory, ResourceHandler<ItemResource> handler, ContainerData data, Tier tier) {
        super(ModMenuTypes.PROCESSOR.get(), id);
        this.data = data;
        this.tier = tier;

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                ProcessorBlockEntity.FUEL_SLOT, FUEL_X, FUEL_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
        });

        for (int i = 0; i < tier.inputCount; i++) {
            addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                    tier.getInputStart() + i, inputSlotX(tier, i), inputSlotY(tier)) {
                @Override public boolean mayPlace(ItemStack stack) {
                    return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
                }
            });
        }

        for (int i = 0; i < tier.inputCount; i++) {
            addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                    tier.getOutputStart() + i, outputSlotX(tier, i), outputSlotY(tier)) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
            });
        }

        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                tier.getEnergyUpgradeSlot(), UPG_ENERGY_X, UPG_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
            }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new ResourceHandlerSlot(handler, handler instanceof ItemStacksResourceHandler h ? h::set : this::noopSet,
                tier.getSpeedUpgradeSlot(), UPG_SPEED_X, UPG_Y) {
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

    public Tier getTier() { return tier; }
    public boolean isBurning() { return data.get(0) > 0; }
    public int getEnergyStored() { return data.get(0); }
    public int getEnergyCapacity() { return data.get(1); }
    public int getMaxProgress() { return data.get(2); }
    public int getMachineTier() { return data.get(3); }
    public int getInputCount() { return data.get(4); }
    public int getBaseTicksPerProcess() { return data.get(5); }
    public int getInputProgress(int index) {
        if (index < 0 || index >= ProcessorBlockEntity.MAX_INPUTS) return 0;
        return data.get(ProcessorBlockEntity.DATA_PROGRESS + index);
    }
    public boolean isOutputBlocked() { return data.get(ProcessorBlockEntity.DATA_STALLED) == 1; }
    public int getEnergyUpgradeTier() { return data.get(ProcessorBlockEntity.DATA_ENERGY_UPG); }
    public int getSpeedUpgradeTier() { return data.get(ProcessorBlockEntity.DATA_SPEED_UPG); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            stack = stackInSlot.copy();
            int totalSlots = tier.getTotalSlots();
            if (index < totalSlots) {
                if (!moveItemStackTo(stackInSlot, totalSlots, slots.size(), true))
                    return ItemStack.EMPTY;
            } else {
                // mayPlace filtra cada destino: entradas, depois upgrades, depois combustível
                if (!moveItemStackTo(stackInSlot, tier.getInputStart(), tier.getInputEnd() + 1, false)
                        && !moveItemStackTo(stackInSlot, tier.getEnergyUpgradeSlot(), tier.getSpeedUpgradeSlot() + 1, false)
                        && !moveItemStackTo(stackInSlot, ProcessorBlockEntity.FUEL_SLOT, ProcessorBlockEntity.FUEL_SLOT + 1, false)) {
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
