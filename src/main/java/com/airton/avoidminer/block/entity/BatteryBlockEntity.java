package com.airton.avoidminer.block.entity;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.block.BatteryBlock;
import com.airton.avoidminer.menu.BatteryMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class BatteryBlockEntity extends BlockEntity {
    public static final int FUEL_SLOT = 0;
    public static final int CAPACITY_SLOT_START = 1;
    public static final int CAPACITY_SLOT_COUNT = 27;
    public static final int CAPACITY_SLOT_END = CAPACITY_SLOT_START + CAPACITY_SLOT_COUNT; // 28
    public static final int RANGE_UPGRADE_SLOT = 28;
    public static final int TOTAL_SLOTS = 29;

    public static final int ENERGY_CAPACITY_BASE = 4_000_000;

    public static final int[] CAPACITY_BONUS = {0, 10_000_000, 20_000_000, 30_000_000};

    public static final int DATA_ENERGY_LO = 0;
    public static final int DATA_ENERGY_HI = 1;
    public static final int DATA_LINKS = 2;
    public static final int DATA_CHARGING = 3;
    public static final int DATA_CAPACITY_LO = 4;
    public static final int DATA_CAPACITY_HI = 5;
    public static final int DATA_RANGE_TIER = 6;
    public static final int DATA_SIZE = 7;

    private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) { setChanged(); }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            if (resource.isEmpty()) return false;
            ItemStack stack = resource.toStack(1);
            if (slot == FUEL_SLOT) {
                return level != null && stack.getBurnTime(RecipeType.SMELTING, level.fuelValues()) > 0;
            }
            if (slot >= CAPACITY_SLOT_START && slot < CAPACITY_SLOT_END) {
                return stack.is(ModItems.CAPACITY_UPGRADE_TIER_1.get())
                        || stack.is(ModItems.CAPACITY_UPGRADE_TIER_2.get())
                        || stack.is(ModItems.CAPACITY_UPGRADE_TIER_3.get());
            }
            if (slot == RANGE_UPGRADE_SLOT) {
                return stack.is(ModItems.RANGE_UPGRADE_TIER_1.get())
                        || stack.is(ModItems.RANGE_UPGRADE_TIER_2.get())
                        || stack.is(ModItems.RANGE_UPGRADE_TIER_3.get());
            }
            return false;
        }
    };

    private final ResourceHandler<ItemResource> sideHandler = new ResourceHandler<>() {
        @Override public int size() { return itemHandler.size(); }
        @Override public ItemResource getResource(int index) { return itemHandler.getResource(index); }
        @Override public long getAmountAsLong(int index) { return itemHandler.getAmountAsLong(index); }
        @Override public long getCapacityAsLong(int index, ItemResource resource) { return itemHandler.getCapacityAsLong(index, resource); }
        @Override public boolean isValid(int index, ItemResource resource) { return itemHandler.isValid(index, resource); }
        @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return index == FUEL_SLOT ? itemHandler.insert(index, resource, amount, transaction) : 0;
        }
        @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) { return 0; }
    };

    private int energyBuffer;
    private int chargingTicks;
    private int linksThisTick;
    private int linksDisplay;
    private long lastLinkGameTime = Long.MIN_VALUE;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            int totalCap = getEffectiveCapacity();
            return switch (index) {
                case DATA_ENERGY_LO -> energyBuffer & 0xFFFF;
                case DATA_ENERGY_HI -> energyBuffer >>> 16;
                case DATA_LINKS -> linksDisplay;
                case DATA_CHARGING -> chargingTicks > 0 ? 1 : 0;
                case DATA_CAPACITY_LO -> totalCap & 0xFFFF;
                case DATA_CAPACITY_HI -> totalCap >>> 16;
                case DATA_RANGE_TIER -> getRangeUpgradeTier();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case DATA_ENERGY_LO -> energyBuffer = (energyBuffer & ~0xFFFF) | (value & 0xFFFF);
                case DATA_ENERGY_HI -> energyBuffer = (energyBuffer & 0xFFFF) | (value << 16);
                case DATA_LINKS -> linksDisplay = value;
                case DATA_CHARGING -> chargingTicks = value > 0 ? 1 : 0;
            }
        }
        @Override public int getCount() { return DATA_SIZE; }
    };

    public BatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY.get(), pos, state);
    }

    public int getEnergy() { return energyBuffer; }

    public int getEffectiveCapacity() {
        int bonus = 0;
        for (int i = CAPACITY_SLOT_START; i < CAPACITY_SLOT_END; i++) {
            ItemResource res = itemHandler.getResource(i);
            if (res.isEmpty()) continue;
            ItemStack stack = res.toStack(1);
            if (stack.is(ModItems.CAPACITY_UPGRADE_TIER_1.get())) bonus += CAPACITY_BONUS[1];
            else if (stack.is(ModItems.CAPACITY_UPGRADE_TIER_2.get())) bonus += CAPACITY_BONUS[2];
            else if (stack.is(ModItems.CAPACITY_UPGRADE_TIER_3.get())) bonus += CAPACITY_BONUS[3];
        }
        return ENERGY_CAPACITY_BASE + bonus;
    }

    public int getMaxLinks() {
        return switch (getRangeUpgradeTier()) {
            case 1 -> 2;
            case 2 -> 4;
            case 3 -> 8;
            default -> 2;
        };
    }

    private int getRangeUpgradeTier() {
        ItemResource res = itemHandler.getResource(RANGE_UPGRADE_SLOT);
        if (res.isEmpty()) return 0;
        ItemStack stack = res.toStack(1);
        if (stack.is(ModItems.RANGE_UPGRADE_TIER_1.get())) return 1;
        if (stack.is(ModItems.RANGE_UPGRADE_TIER_2.get())) return 2;
        if (stack.is(ModItems.RANGE_UPGRADE_TIER_3.get())) return 3;
        return 0;
    }

    public int extractEnergy(int amount) {
        if (amount <= 0 || energyBuffer <= 0 || level == null) return 0;
        long gt = level.getGameTime();
        if (gt != lastLinkGameTime) {
            lastLinkGameTime = gt;
            linksThisTick = 0;
        }
        if (linksThisTick >= getMaxLinks()) return 0;
        int drained = Math.min(amount, energyBuffer);
        energyBuffer -= drained;
        linksThisTick++;
        linksDisplay = linksThisTick;
        setChanged();
        return drained;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BatteryBlockEntity be) {
        boolean dirty = false;

        if (be.chargingTicks > 0) be.chargingTicks--;
        if (level.getGameTime() - be.lastLinkGameTime > 2 && be.linksDisplay != 0) {
            be.linksDisplay = 0;
        }

        int capacity = be.getEffectiveCapacity();
        if (be.energyBuffer < capacity) {
            ItemResource fuelRes = be.itemHandler.getResource(FUEL_SLOT);
            int fuelAmt = be.itemHandler.getAmountAsInt(FUEL_SLOT);
            if (!fuelRes.isEmpty() && fuelAmt > 0) {
                ItemStack fuelStack = fuelRes.toStack(1);
                int burnTime = fuelStack.getBurnTime(RecipeType.SMELTING, level.fuelValues());
                if (burnTime > 0) {
                    int energy = Math.max(1, burnTime / 5);
                    if (be.energyBuffer + energy <= capacity) {
                        be.energyBuffer += energy;
                        be.itemHandler.set(FUEL_SLOT, fuelRes, fuelAmt - 1);
                        be.chargingTicks = 8;
                        dirty = true;
                    }
                }
            }
        }

        boolean lit = be.energyBuffer > 0;
        if (state.getValue(BatteryBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(BatteryBlock.LIT, lit), 3);
        }

        if (dirty) be.setChanged();
    }

    public void openMenu(ServerPlayer player) {
        player.openMenu(getMenuProvider(), worldPosition);
    }

    public MenuProvider getMenuProvider() {
        return new SimpleMenuProvider(
                (id, inv, p) -> new BatteryMenu(id, inv, itemHandler, data),
                Component.translatable("container.avoidminer.battery")
        );
    }

    public ResourceHandler<ItemResource> getItemHandler() { return itemHandler; }

    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        return side == null ? itemHandler : sideHandler;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Energy", energyBuffer);
        itemHandler.serialize(output.child("Inventory"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyBuffer = input.getIntOr("Energy", 0);

        // itemHandler.deserialize() would resize the handler to match old save size (3).
        // Instead, manually read stacks to handle migration: old (3) -> new (29).
        ValueInput invInput = input.childOrEmpty("Inventory");
        invInput.read(StacksResourceHandler.VALUE_IO_KEY, ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(stacks -> {
            int n = stacks.size();
            if (n == 3) {
                if (!stacks.get(0).isEmpty())
                    itemHandler.set(FUEL_SLOT, ItemResource.of(stacks.get(0)), stacks.get(0).getCount());
                if (!stacks.get(1).isEmpty())
                    itemHandler.set(CAPACITY_SLOT_START, ItemResource.of(stacks.get(1)), stacks.get(1).getCount());
                if (!stacks.get(2).isEmpty())
                    itemHandler.set(RANGE_UPGRADE_SLOT, ItemResource.of(stacks.get(2)), stacks.get(2).getCount());
            } else {
                for (int i = 0; i < Math.min(n, TOTAL_SLOTS); i++) {
                    ItemStack stack = stacks.get(i);
                    if (!stack.isEmpty())
                        itemHandler.set(i, ItemResource.of(stack), stack.getCount());
                }
            }
        });
    }
}
