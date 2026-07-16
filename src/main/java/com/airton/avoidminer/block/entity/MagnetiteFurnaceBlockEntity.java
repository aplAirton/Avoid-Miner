package com.airton.avoidminer.block.entity;

import com.airton.avoidminer.config.AvoidMinerServerConfig;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.ModBlocks;
import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.block.MagnetiteFurnaceBlock;
import com.airton.avoidminer.block.entity.AvoidMinerBlockEntity.UpgradeType;
import com.airton.avoidminer.energy.EnergyReceiver;
import com.airton.avoidminer.item.EnergyLinkItem;
import com.airton.avoidminer.menu.MagnetiteFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public final class MagnetiteFurnaceBlockEntity extends BlockEntity implements EnergyReceiver {
    public static final int FUEL_SLOT = 0;
    public static final int MAX_INPUTS = 5;

    public enum Tier {
        TIER_1(1, 4_000, 200, 0.2F, 1),
        TIER_2(3, 8_000, 100, 0.8F, 2),
        TIER_3(5, 12_000, 50, 3.2F, 3);

        public final int inputCount;
        public final int energyCapacity;
        public final int ticksPerProcess;
        public final float baseEnergyPerTick;
        public final int tierLevel;

        Tier(int inputCount, int energyCapacity, int ticksPerProcess, float baseEnergyPerTick, int tierLevel) {
            this.inputCount = inputCount;
            this.energyCapacity = energyCapacity;
            this.ticksPerProcess = ticksPerProcess;
            this.baseEnergyPerTick = baseEnergyPerTick;
            this.tierLevel = tierLevel;
        }

        public int getInputStart() { return FUEL_SLOT + 1; }
        public int getInputEnd() { return getInputStart() + inputCount - 1; }
        public int getOutputStart() { return getInputStart() + inputCount; }
        public int getOutputEnd() { return getOutputStart() + inputCount - 1; }
        public int getEnergyUpgradeSlot() { return getOutputEnd() + 1; }
        public int getSpeedUpgradeSlot() { return getOutputEnd() + 2; }
        public int getTotalSlots() { return getSpeedUpgradeSlot() + 1; }
    }

    public static final int DATA_PROGRESS = 6;
    public static final int DATA_STALLED = DATA_PROGRESS + MAX_INPUTS;
    public static final int DATA_ENERGY_UPG = DATA_STALLED + 1;
    public static final int DATA_SPEED_UPG = DATA_ENERGY_UPG + 1;
    public static final int DATA_AUTO_BALANCE = DATA_SPEED_UPG + 1;
    public static final int DATA_SIZE = DATA_AUTO_BALANCE + 1;

    private final Tier tier;
    private final ItemStacksResourceHandler itemHandler;
    private final ResourceHandler<ItemResource> bottomHandler;
    private final ResourceHandler<ItemResource> sideHandler;
    private final int[] progress = new int[MAX_INPUTS];
    private int energyBuffer;
    private float energyFraction;
    private boolean autoBalance;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            if (index == 0) return energyBuffer;
            if (index == 1) return tier.energyCapacity;
            if (index == 2) return getEffectiveTicks();
            if (index == 3) return tier.tierLevel;
            if (index == 4) return tier.inputCount;
            if (index == 5) return tier.ticksPerProcess;
            if (index >= DATA_PROGRESS && index < DATA_PROGRESS + MAX_INPUTS) {
                return progress[index - DATA_PROGRESS];
            }
            if (index == DATA_STALLED) return isAnyOutputBlocked() ? 1 : 0;
            if (index == DATA_ENERGY_UPG) return upgradeTierAt(tier.getEnergyUpgradeSlot());
            if (index == DATA_SPEED_UPG) return upgradeTierAt(tier.getSpeedUpgradeSlot());
            if (index == DATA_AUTO_BALANCE) return autoBalance ? 1 : 0;
            return 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) energyBuffer = value;
            if (index >= DATA_PROGRESS && index < DATA_PROGRESS + MAX_INPUTS) {
                progress[index - DATA_PROGRESS] = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_SIZE;
        }
    };

    public MagnetiteFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGNETITE_FURNACE.get(), pos, state);
        this.tier = getTierFromBlock(state);
        this.itemHandler = createItemHandler();
        this.bottomHandler = createBottomHandler();
        this.sideHandler = createSideHandler();
    }

    private static Tier getTierFromBlock(BlockState state) {
        return state.getBlock() instanceof MagnetiteFurnaceBlock block ? block.getTier() : Tier.TIER_1;
    }

    public Tier getTier() {
        return tier;
    }

    public static boolean isSmeltable(@Nullable Level level, ItemStack stack) {
        return level != null && !stack.isEmpty()
                && level.recipeAccess().propertySet(RecipePropertySet.FURNACE_INPUT).test(stack);
    }

    public static boolean isRawOreBlock(ItemStack stack) {
        return stack.is(Items.RAW_IRON_BLOCK)
                || stack.is(Items.RAW_COPPER_BLOCK)
                || stack.is(Items.RAW_GOLD_BLOCK)
                || stack.is(ModBlocks.RAW_MAGNETITE_BLOCK.asItem());
    }

    @Nullable
    private static ItemStack getRecipeResult(Level level, ItemStack input) {
        if (!(level instanceof ServerLevel serverLevel) || input.isEmpty()) return null;
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        return serverLevel.recipeAccess().getRecipeFor(RecipeType.SMELTING, recipeInput, serverLevel)
                .map(recipe -> recipe.value().assemble(recipeInput))
                .filter(result -> !result.isEmpty())
                .orElse(null);
    }

    private ItemStacksResourceHandler createItemHandler() {
        return new ItemStacksResourceHandler(tier.getTotalSlots()) {
            @Override
            protected void onContentsChanged(int slot, ItemStack previousContents) {
                setChanged();
            }

            @Override
            public boolean isValid(int slot, ItemResource resource) {
                if (resource.isEmpty()) return false;
                ItemStack stack = resource.toStack(1);
                if (slot == FUEL_SLOT) {
                    return (level != null && stack.getBurnTime(RecipeType.SMELTING, level.fuelValues()) > 0)
                            || stack.is(ModItems.ENERGY_LINK.get());
                }
                if (slot >= tier.getInputStart() && slot <= tier.getInputEnd()) {
                    return isSmeltable(level, stack);
                }
                if (slot >= tier.getOutputStart() && slot <= tier.getOutputEnd()) return false;
                if (slot == tier.getEnergyUpgradeSlot()) {
                    return UpgradeType.fromStack(stack) == UpgradeType.ENERGY
                            && UpgradeType.tierFromStack(stack) <= tier.tierLevel;
                }
                if (slot == tier.getSpeedUpgradeSlot()) {
                    return UpgradeType.fromStack(stack) == UpgradeType.SPEED
                            && UpgradeType.tierFromStack(stack) <= tier.tierLevel;
                }
                return false;
            }
        };
    }

    private ResourceHandler<ItemResource> createBottomHandler() {
        return new SidedHandler() {
            @Override
            public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
                return 0;
            }

            @Override
            public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
                return index >= tier.getOutputStart() && index <= tier.getOutputEnd()
                        ? itemHandler.extract(index, resource, amount, transaction) : 0;
            }
        };
    }

    private ResourceHandler<ItemResource> createSideHandler() {
        return new SidedHandler() {
            @Override
            public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
                if (index == FUEL_SLOT || index >= tier.getInputStart() && index <= tier.getInputEnd()) {
                    return itemHandler.insert(index, resource, amount, transaction);
                }
                return 0;
            }

            @Override
            public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
                return 0;
            }
        };
    }

    private abstract class SidedHandler implements ResourceHandler<ItemResource> {
        @Override public int size() { return itemHandler.size(); }
        @Override public ItemResource getResource(int index) { return itemHandler.getResource(index); }
        @Override public long getAmountAsLong(int index) { return itemHandler.getAmountAsLong(index); }
        @Override public long getCapacityAsLong(int index, ItemResource resource) {
            return itemHandler.getCapacityAsLong(index, resource);
        }
        @Override public boolean isValid(int index, ItemResource resource) { return itemHandler.isValid(index, resource); }
    }

    private ItemStack getStackIn(int slot) {
        ItemResource resource = itemHandler.getResource(slot);
        int amount = itemHandler.getAmountAsInt(slot);
        return resource.isEmpty() || amount <= 0 ? ItemStack.EMPTY : resource.toStack(amount);
    }

    private int upgradeTierAt(int slot) {
        ItemStack stack = getStackIn(slot);
        return stack.isEmpty() ? 0 : UpgradeType.tierFromStack(stack);
    }

    private float getEnergyMultiplier() {
        ItemStack stack = getStackIn(tier.getEnergyUpgradeSlot());
        int upgradeTier = UpgradeType.fromStack(stack) == UpgradeType.ENERGY
                ? UpgradeType.tierFromStack(stack) : 0;
        return MagnetiteFurnaceRules.energyMultiplier(upgradeTier);
    }

    private float getSpeedMultiplier() {
        ItemStack stack = getStackIn(tier.getSpeedUpgradeSlot());
        int upgradeTier = UpgradeType.fromStack(stack) == UpgradeType.SPEED
                ? UpgradeType.tierFromStack(stack) : 0;
        return MagnetiteFurnaceRules.speedMultiplier(upgradeTier);
    }

    private int getEffectiveTicks() {
        int upgradedTicks = MagnetiteFurnaceRules.effectiveTicks(
                tier.ticksPerProcess, upgradeTierAt(tier.getSpeedUpgradeSlot()));
        return AvoidMinerServerConfig.machineTicks(upgradedTicks);
    }

    private boolean canOutputAccept(int outputSlot, ItemStack result) {
        ItemResource current = itemHandler.getResource(outputSlot);
        int amount = itemHandler.getAmountAsInt(outputSlot);
        return current.isEmpty() || current.equals(ItemResource.of(result))
                && amount + result.getCount() <= result.getMaxStackSize();
    }

    private boolean isAnyOutputBlocked() {
        if (level == null) return false;
        for (int i = 0; i < tier.inputCount; i++) {
            ItemStack input = getStackIn(tier.getInputStart() + i);
            ItemStack result = getRecipeResult(level, input);
            if (result != null && !canOutputAccept(tier.getOutputStart() + i, result)) return true;
        }
        return false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MagnetiteFurnaceBlockEntity be) {
        if (be.autoBalance && be.tier.inputCount > 1) {
            MachineInputBalancer.balance(be.itemHandler, be.tier.getInputStart(), be.tier.inputCount);
        }
        if (level.hasNeighborSignal(pos)) {
            be.setLit(level, pos, state, false);
            return;
        }

        boolean dirty = false;
        boolean anyProcessing = false;
        int maxProgress = be.getEffectiveTicks();
        float rawCostPerTick = (float) (be.tier.baseEnergyPerTick * be.getEnergyMultiplier()
                * be.getSpeedMultiplier() * AvoidMinerServerConfig.machineSpeedMultiplier()
                * AvoidMinerServerConfig.machineEnergyMultiplier());

        for (int i = 0; i < be.tier.inputCount; i++) {
            int inputSlot = be.tier.getInputStart() + i;
            int outputSlot = be.tier.getOutputStart() + i;
            ItemStack input = be.getStackIn(inputSlot);
            boolean isBlockRecipe = isRawOreBlock(input);
            ItemStack result = getRecipeResult(level, input);
            if (result == null || !be.canOutputAccept(outputSlot, result)) {
                if (be.progress[i] != 0) {
                    be.progress[i] = 0;
                    dirty = true;
                }
                continue;
            }

            if (be.energyBuffer > 0) {
                float recipeCost = rawCostPerTick * (isBlockRecipe
                        ? MagnetiteFurnaceRules.RAW_ORE_BLOCK_ENERGY_MULTIPLIER : 1);
                float accumulatedCost = be.energyFraction + recipeCost;
                int cost = (int) accumulatedCost;
                if (cost > be.energyBuffer) continue;
                be.energyFraction = accumulatedCost - cost;
                be.energyBuffer -= cost;
                be.progress[i]++;
                anyProcessing = true;
                dirty = true;
                if (be.progress[i] >= maxProgress) {
                    be.progress[i] = 0;
                    be.smeltOne(inputSlot, outputSlot, result);
                }
            }
        }

        if (be.energyBuffer < be.tier.energyCapacity) {
            ItemResource fuelResource = be.itemHandler.getResource(FUEL_SLOT);
            int fuelAmount = be.itemHandler.getAmountAsInt(FUEL_SLOT);
            if (!fuelResource.isEmpty() && fuelAmount > 0) {
                ItemStack fuel = fuelResource.toStack(1);
                if (fuel.is(ModItems.ENERGY_LINK.get())) {
                    int pulled = EnergyLinkItem.drawEnergy(level, fuel, EnergyLinkItem.TRANSFER_PER_TICK);
                    if (pulled > 0) {
                        be.energyBuffer = Math.min(be.energyBuffer + pulled, be.tier.energyCapacity);
                        dirty = true;
                    }
                } else {
                    int burnTime = fuel.getBurnTime(RecipeType.SMELTING, level.fuelValues());
                    int energy = Math.max(1, burnTime / 5);
                    if (burnTime > 0 && be.energyBuffer + energy <= be.tier.energyCapacity) {
                        be.energyBuffer += energy;
                        if (fuel.is(Items.LAVA_BUCKET)) {
                            be.itemHandler.set(FUEL_SLOT, ItemResource.of(Items.BUCKET), 1);
                        } else {
                            be.itemHandler.set(FUEL_SLOT, fuelResource, fuelAmount - 1);
                        }
                        dirty = true;
                    }
                }
            }
        }

        be.setLit(level, pos, state, anyProcessing && be.energyBuffer > 0);
        if (dirty) be.setChanged();
    }

    private void setLit(Level level, BlockPos pos, BlockState state, boolean lit) {
        if (state.getValue(MagnetiteFurnaceBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(MagnetiteFurnaceBlock.LIT, lit), 3);
        }
    }

    private void smeltOne(int inputSlot, int outputSlot, ItemStack result) {
        ItemResource input = itemHandler.getResource(inputSlot);
        itemHandler.set(inputSlot, input, itemHandler.getAmountAsInt(inputSlot) - 1);
        ItemResource output = itemHandler.getResource(outputSlot);
        int outputAmount = itemHandler.getAmountAsInt(outputSlot);
        ItemResource resultResource = ItemResource.of(result);
        itemHandler.set(outputSlot, resultResource,
                output.isEmpty() ? result.getCount() : outputAmount + result.getCount());
        setChanged();
    }

    public boolean isBurning() {
        return energyBuffer > 0;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int accepted = Math.min(maxReceive, tier.energyCapacity - energyBuffer);
        if (accepted > 0 && !simulate) {
            energyBuffer += accepted;
            setChanged();
        }
        return Math.max(0, accepted);
    }

    public void openMenu(ServerPlayer player) {
        player.openMenu(getMenuProvider(), worldPosition);
    }

    private MenuProvider getMenuProvider() {
        String key = "container.avoidminer.magnetite_furnace_tier_" + tier.tierLevel;
        return new SimpleMenuProvider(
                (id, inventory, player) -> new MagnetiteFurnaceMenu(
                        id, inventory, itemHandler, data, tier, this::toggleAutoBalance),
                Component.translatable(key)
        );
    }

    public void toggleAutoBalance() {
        if (tier.inputCount <= 1) return;
        autoBalance = !autoBalance;
        if (autoBalance) {
            MachineInputBalancer.balance(itemHandler, tier.getInputStart(), tier.inputCount);
        }
        setChanged();
    }

    public ResourceHandler<ItemResource> getItemHandler() {
        return itemHandler;
    }

    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        if (side == null) return itemHandler;
        return side == Direction.DOWN ? bottomHandler : sideHandler;
    }

    public void dropContentsExceptUpgrades(ServerLevel level) {
        for (int slot = FUEL_SLOT; slot <= tier.getOutputEnd(); slot++) {
            ItemStack stack = getStackIn(slot);
            if (!stack.isEmpty()) Block.popResource(level, worldPosition, stack);
            itemHandler.set(slot, ItemResource.EMPTY, 0);
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Energy", energyBuffer);
        output.putFloat("EnergyFraction", energyFraction);
        output.putInt("Tier", tier.tierLevel);
        output.putInt("SlotCount", tier.getTotalSlots());
        output.putBoolean("AutoBalance", autoBalance);
        for (int i = 0; i < MAX_INPUTS; i++) output.putInt("Progress" + i, progress[i]);
        itemHandler.serialize(output.child("Inventory"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyBuffer = Math.clamp(input.getIntOr("Energy", 0), 0, tier.energyCapacity);
        energyFraction = input.getFloatOr("EnergyFraction", 0.0F);
        autoBalance = tier.inputCount > 1 && input.getBooleanOr("AutoBalance", false);
        for (int i = 0; i < MAX_INPUTS; i++) progress[i] = input.getIntOr("Progress" + i, 0);
        ItemStacksResourceHandler saved = new ItemStacksResourceHandler(
                Math.max(input.getIntOr("SlotCount", tier.getTotalSlots()), tier.getTotalSlots()));
        saved.deserialize(input.childOrEmpty("Inventory"));
        for (int i = 0; i < tier.getTotalSlots(); i++) {
            ItemResource resource = saved.getResource(i);
            int amount = saved.getAmountAsInt(i);
            if (!resource.isEmpty() && amount > 0) itemHandler.set(i, resource, amount);
        }
    }
}
