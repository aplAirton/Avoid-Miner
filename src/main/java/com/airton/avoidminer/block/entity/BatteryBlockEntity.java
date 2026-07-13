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
    public static final int CAPACITY_INSTALL_SLOT = 1;
    public static final int RANGE_UPGRADE_SLOT = 2;
    public static final int TOTAL_SLOTS = 3;

    public static final int ENERGY_CAPACITY_BASE = 4_000_000;

    public static final int[] CAPACITY_BONUS = {0, 10_000_000, 20_000_000, 30_000_000};

    public static final int DATA_ENERGY_LO = 0;
    public static final int DATA_ENERGY_HI = 1;
    public static final int DATA_LINKS = 2;
    public static final int DATA_CHARGING = 3;
    public static final int DATA_CAPACITY_LO = 4;
    public static final int DATA_CAPACITY_HI = 5;
    public static final int DATA_RANGE_TIER = 6;
    public static final int DATA_INSTALLED_CAPACITY = 7;
    public static final int DATA_SIZE = 8;

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
            if (slot == CAPACITY_INSTALL_SLOT) {
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
    private long installedCapacityBonus;
    private int installedCapacityUpgrades;

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
                case DATA_INSTALLED_CAPACITY -> installedCapacityUpgrades;
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
        return (int) Math.min(Integer.MAX_VALUE, ENERGY_CAPACITY_BASE + installedCapacityBonus);
    }

    public int getInstalledCapacityUpgrades() { return installedCapacityUpgrades; }

    public static int capacityBonus(ItemStack stack) {
        if (stack.is(ModItems.CAPACITY_UPGRADE_TIER_1.get())) return CAPACITY_BONUS[1];
        if (stack.is(ModItems.CAPACITY_UPGRADE_TIER_2.get())) return CAPACITY_BONUS[2];
        if (stack.is(ModItems.CAPACITY_UPGRADE_TIER_3.get())) return CAPACITY_BONUS[3];
        return 0;
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

        dirty |= be.installCapacityUpgrades();

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

    private boolean installCapacityUpgrades() {
        ItemResource resource = itemHandler.getResource(CAPACITY_INSTALL_SLOT);
        int amount = itemHandler.getAmountAsInt(CAPACITY_INSTALL_SLOT);
        if (resource.isEmpty() || amount <= 0) return false;

        int bonusPerItem = capacityBonus(resource.toStack(1));
        if (bonusPerItem <= 0) return false;

        long remainingCapacity = Integer.MAX_VALUE - (long) ENERGY_CAPACITY_BASE - installedCapacityBonus;
        int consumed = (int) Math.min(amount, Math.max(0L, remainingCapacity / bonusPerItem));
        if (consumed <= 0) return false;

        installedCapacityBonus += (long) bonusPerItem * consumed;
        installedCapacityUpgrades = (int) Math.min(
                Integer.MAX_VALUE,
                (long) installedCapacityUpgrades + consumed
        );
        itemHandler.set(CAPACITY_INSTALL_SLOT, resource, amount - consumed);
        return true;
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
        output.putLong("InstalledCapacityBonus", installedCapacityBonus);
        output.putInt("InstalledCapacityUpgrades", installedCapacityUpgrades);
        output.putInt("BatteryLayoutVersion", 2);
        itemHandler.serialize(output.child("Inventory"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyBuffer = input.getIntOr("Energy", 0);
        installedCapacityBonus = Math.max(0L, input.getLongOr("InstalledCapacityBonus", 0L));
        installedCapacityUpgrades = Math.max(0, input.getIntOr("InstalledCapacityUpgrades", 0));

        // Old batteries used 27 live capacity slots. Convert every installed item
        // into the permanent bonus while preserving fuel and range upgrades.
        ValueInput invInput = input.childOrEmpty("Inventory");
        invInput.read(StacksResourceHandler.VALUE_IO_KEY, ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(stacks -> {
            int n = stacks.size();
            if (n == 0) return;

            copyStack(stacks.get(0), FUEL_SLOT);
            if (n >= 29) {
                for (int i = 1; i < 28; i++) {
                    migrateCapacityStack(stacks.get(i));
                }
                copyStack(stacks.get(28), RANGE_UPGRADE_SLOT);
            } else {
                if (n > 1) copyStack(stacks.get(1), CAPACITY_INSTALL_SLOT);
                if (n > 2) copyStack(stacks.get(2), RANGE_UPGRADE_SLOT);
            }
        });

        installedCapacityBonus = Math.min(
                installedCapacityBonus,
                Integer.MAX_VALUE - (long) ENERGY_CAPACITY_BASE
        );
        energyBuffer = Math.min(energyBuffer, getEffectiveCapacity());
    }

    private void copyStack(ItemStack stack, int slot) {
        if (!stack.isEmpty()) itemHandler.set(slot, ItemResource.of(stack), stack.getCount());
    }

    private void migrateCapacityStack(ItemStack stack) {
        if (stack.isEmpty()) return;
        int bonus = capacityBonus(stack);
        if (bonus <= 0) return;
        long maxBonus = Integer.MAX_VALUE - (long) ENERGY_CAPACITY_BASE;
        long accepted = Math.min((long) bonus * stack.getCount(), maxBonus - installedCapacityBonus);
        int count = (int) Math.max(0L, accepted / bonus);
        installedCapacityBonus += (long) bonus * count;
        installedCapacityUpgrades = (int) Math.min(
                Integer.MAX_VALUE,
                (long) installedCapacityUpgrades + count
        );
    }
}
