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

/**
 * Bateria — queima combustível e acumula grandes quantidades de energia já
 * transformada. Máquinas com um Link de Energia vinculado no slot de
 * combustível puxam energia daqui via {@link #extractEnergy}; várias máquinas
 * podem drenar a mesma bateria ao mesmo tempo (servidor single-thread).
 */
public class BatteryBlockEntity extends BlockEntity {
    public static final int FUEL_SLOT = 0;
    public static final int CAPACITY_UPGRADE_SLOT = 1;
    public static final int RANGE_UPGRADE_SLOT = 2;
    public static final int TOTAL_SLOTS = 3;
    public static final int ENERGY_CAPACITY = 4_000_000;

    // A energia excede 16 bits, então é sincronizada em duas metades
    public static final int DATA_ENERGY_LO = 0;
    public static final int DATA_ENERGY_HI = 1;
    public static final int DATA_LINKS = 2;
    public static final int DATA_CHARGING = 3;
    public static final int DATA_CAPACITY_TIER = 4;
    public static final int DATA_RANGE_TIER = 5;
    public static final int DATA_SIZE = 6;

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
            if (slot == CAPACITY_UPGRADE_SLOT) {
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
    private int chargingTicks;    // >0 enquanto queimou combustível há pouco
    private int linksThisTick;
    private int linksDisplay;
    private long lastLinkGameTime = Long.MIN_VALUE;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case DATA_ENERGY_LO -> energyBuffer & 0xFFFF;
                case DATA_ENERGY_HI -> energyBuffer >>> 16;
                case DATA_LINKS -> linksDisplay;
                case DATA_CHARGING -> chargingTicks > 0 ? 1 : 0;
                case DATA_CAPACITY_TIER -> getCapacityUpgradeTier();
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
        int capTier = getCapacityUpgradeTier();
        return ENERGY_CAPACITY * (1 << capTier); // x2, x4, x8
    }

    public int getMaxLinks() {
        return switch (getRangeUpgradeTier()) {
            case 1 -> 2;
            case 2 -> 4;
            case 3 -> 8;
            default -> 2;
        };
    }

    private int getCapacityUpgradeTier() {
        if (itemHandler.size() < 2) return 0;
        ItemResource res = itemHandler.getResource(1);
        if (res.isEmpty()) return 0;
        ItemStack stack = res.toStack(1);
        if (stack.is(ModItems.CAPACITY_UPGRADE_TIER_1.get())) return 1;
        if (stack.is(ModItems.CAPACITY_UPGRADE_TIER_2.get())) return 2;
        if (stack.is(ModItems.CAPACITY_UPGRADE_TIER_3.get())) return 3;
        return 0;
    }

    private int getRangeUpgradeTier() {
        if (itemHandler.size() < 3) return 0;
        ItemResource res = itemHandler.getResource(2);
        if (res.isEmpty()) return 0;
        ItemStack stack = res.toStack(1);
        if (stack.is(ModItems.RANGE_UPGRADE_TIER_1.get())) return 1;
        if (stack.is(ModItems.RANGE_UPGRADE_TIER_2.get())) return 2;
        if (stack.is(ModItems.RANGE_UPGRADE_TIER_3.get())) return 3;
        return 0;
    }

    /** Drena até {@code amount} de energia; usado pelos Links de Energia. */
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
        itemHandler.deserialize(input.childOrEmpty("Inventory"));
    }
}
