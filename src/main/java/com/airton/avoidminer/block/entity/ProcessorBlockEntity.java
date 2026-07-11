package com.airton.avoidminer.block.entity;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.block.ProcessorBlock;
import com.airton.avoidminer.block.entity.AvoidMinerBlockEntity.UpgradeType;
import com.airton.avoidminer.menu.ProcessorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

public class ProcessorBlockEntity extends BlockEntity {
    public static final int FUEL_SLOT = 0;

    public enum Tier {
        TIER_1(1, 2, 4000, 100, 1),
        TIER_2(3, 2, 8000, 300, 2),
        TIER_3(6, 3, 12000, 500, 3);

        public final int inputCount;
        public final int upgradeCount;
        public final int energyCapacity;
        public final int ticksPerProcess;
        public final int tierLevel;

        Tier(int inputCount, int upgradeCount, int energyCapacity, int ticksPerProcess, int tierLevel) {
            this.inputCount = inputCount;
            this.upgradeCount = upgradeCount;
            this.energyCapacity = energyCapacity;
            this.ticksPerProcess = ticksPerProcess;
            this.tierLevel = tierLevel;
        }

        public int getOutputStart() { return FUEL_SLOT + 1 + inputCount; }
        public int getUpgradeStart() { return getOutputStart() + 27; }
        public int getTotalSlots() { return getUpgradeStart() + upgradeCount; }

        public int getInputStart() { return FUEL_SLOT + 1; }
        public int getInputEnd() { return getInputStart() + inputCount - 1; }
        public int getOutputEnd() { return getOutputStart() + 26; }
        public int getUpgradeEnd() { return getUpgradeStart() + upgradeCount - 1; }
    }

    private static final Map<ItemStack, ItemStack> RECIPES = new HashMap<>();

    static {
        addRecipe(Items.DIAMOND_ORE, Items.DIAMOND, 2);
        addRecipe(Items.DEEPSLATE_DIAMOND_ORE, Items.DIAMOND, 2);
        addRecipe(Items.IRON_ORE, Items.RAW_IRON, 3);
        addRecipe(Items.DEEPSLATE_IRON_ORE, Items.RAW_IRON, 3);
        addRecipe(Items.COPPER_ORE, Items.RAW_COPPER, 6);
        addRecipe(Items.DEEPSLATE_COPPER_ORE, Items.RAW_COPPER, 6);
        addRecipe(Items.COAL_ORE, Items.COAL, 4);
        addRecipe(Items.DEEPSLATE_COAL_ORE, Items.COAL, 4);
        addRecipe(Items.REDSTONE_ORE, Items.REDSTONE, 4);
        addRecipe(Items.DEEPSLATE_REDSTONE_ORE, Items.REDSTONE, 4);
        addRecipe(Items.LAPIS_ORE, Items.LAPIS_LAZULI, 8);
        addRecipe(Items.DEEPSLATE_LAPIS_ORE, Items.LAPIS_LAZULI, 8);
        addRecipe(Items.NETHER_QUARTZ_ORE, Items.QUARTZ, 6);
        addRecipe(Items.GOLD_ORE, Items.RAW_GOLD, 2);
        addRecipe(Items.DEEPSLATE_GOLD_ORE, Items.RAW_GOLD, 2);
        addRecipe(Items.EMERALD_ORE, Items.EMERALD, 2);
        addRecipe(Items.DEEPSLATE_EMERALD_ORE, Items.EMERALD, 2);
        addRecipe(Items.NETHER_GOLD_ORE, Items.GOLD_NUGGET, 12);
        addRecipe(Items.ANCIENT_DEBRIS, Items.NETHERITE_SCRAP, 2);
    }

    private static void addRecipe(ItemStack input, ItemStack output) {
        RECIPES.put(input, output);
    }

    private static void addRecipe(net.minecraft.world.item.Item inputItem, net.minecraft.world.item.Item outputItem, int outputCount) {
        RECIPES.put(new ItemStack(inputItem), new ItemStack(outputItem, outputCount));
    }

    public static Map<ItemStack, ItemStack> getRecipeMap() {
        return RECIPES;
    }

    @Nullable
    public static ItemStack getRecipeResult(ItemStack input) {
        if (input.isEmpty()) return null;
        for (Map.Entry<ItemStack, ItemStack> entry : RECIPES.entrySet()) {
            if (entry.getKey().is(input.getItem())) {
                return entry.getValue().copy();
            }
        }
        return null;
    }

    private final Tier tier;
    private int tierId;

    private final ItemStacksResourceHandler itemHandler;
    private final ResourceHandler<ItemResource> bottomHandler;
    private final ResourceHandler<ItemResource> sideHandler;

    private int energyBuffer;
    private int progress;
    private float energyFraction;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyBuffer;
                case 1 -> tier.energyCapacity;
                case 2 -> progress;
                case 3 -> getEffectiveTicks();
                case 4 -> upgradeTypeId(tier.getUpgradeStart());
                case 5 -> upgradeTierId(tier.getUpgradeStart());
                case 6 -> tier.upgradeCount >= 2 ? upgradeTypeId(tier.getUpgradeStart() + 1) : 0;
                case 7 -> tier.upgradeCount >= 2 ? upgradeTierId(tier.getUpgradeStart() + 1) : 0;
                case 8 -> tier.upgradeCount >= 3 ? upgradeTypeId(tier.getUpgradeStart() + 2) : 0;
                case 9 -> tier.upgradeCount >= 3 ? upgradeTierId(tier.getUpgradeStart() + 2) : 0;
                case 10 -> tier.upgradeCount;
                case 11 -> tier.tierLevel;
                case 12 -> getEnergyCostPerProcess();
                case 13 -> isOutputFull() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energyBuffer = value;
                case 2 -> progress = value;
            }
        }

        @Override
        public int getCount() { return 14; }
    };

    private int upgradeTypeId(int slot) {
        ItemResource r = itemHandler.getResource(slot);
        if (r.isEmpty()) return 0;
        UpgradeType t = UpgradeType.fromStack(r.toStack(1));
        return t == null ? 0 : t.ordinal() + 1;
    }

    private int upgradeTierId(int slot) {
        ItemResource r = itemHandler.getResource(slot);
        if (r.isEmpty()) return 0;
        return UpgradeType.tierFromStack(r.toStack(1));
    }

    public ProcessorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PROCESSOR.get(), pos, state);
        this.tier = getTierFromBlock(state);
        this.tierId = tier.tierLevel;
        this.itemHandler = createItemHandler();
        this.bottomHandler = createBottomHandler();
        this.sideHandler = createSideHandler();
    }

    private Tier getTierFromBlock(BlockState state) {
        if (state.getBlock() instanceof ProcessorBlock block) {
            return block.getTier();
        }
        return Tier.TIER_1;
    }

    public Tier getTier() {
        if (level != null) {
            return getTierFromBlock(level.getBlockState(worldPosition));
        }
        return Tier.values()[Math.clamp(tierId - 1, 0, 2)];
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
                Tier t = getTier();
                if (slot == FUEL_SLOT) {
                    return level != null && stack.getBurnTime(RecipeType.SMELTING, level.fuelValues()) > 0;
                }
                if (slot >= t.getInputStart() && slot <= t.getInputEnd()) {
                    return getRecipeResult(stack) != null;
                }
                if (slot >= t.getOutputStart() && slot <= t.getOutputEnd()) {
                    return false;
                }
                if (slot >= t.getUpgradeStart() && slot <= t.getUpgradeEnd()) {
                    UpgradeType type = UpgradeType.fromStack(stack);
                    if (type == null) return false;
                    if (type == UpgradeType.MINING) return false;
                    for (int i = t.getUpgradeStart(); i <= t.getUpgradeEnd(); i++) {
                        if (i == slot) continue;
                        ItemResource other = getResource(i);
                        if (!other.isEmpty()) {
                            UpgradeType otherType = UpgradeType.fromStack(other.toStack(1));
                            if (otherType == type) return false;
                        }
                    }
                    return true;
                }
                return false;
            }
        };
    }

    private ResourceHandler<ItemResource> createBottomHandler() {
        Tier t = tier;
        return new ResourceHandler<>() {
            @Override public int size() { return itemHandler.size(); }
            @Override public ItemResource getResource(int index) { return itemHandler.getResource(index); }
            @Override public long getAmountAsLong(int index) { return itemHandler.getAmountAsLong(index); }
            @Override public long getCapacityAsLong(int index, ItemResource resource) { return itemHandler.getCapacityAsLong(index, resource); }
            @Override public boolean isValid(int index, ItemResource resource) { return itemHandler.isValid(index, resource); }
            @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) { return 0; }
            @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
                Tier t2 = getTier();
                if (index >= t2.getOutputStart() && index <= t2.getOutputEnd()) {
                    return itemHandler.extract(index, resource, amount, transaction);
                }
                return 0;
            }
        };
    }

    private ResourceHandler<ItemResource> createSideHandler() {
        return new ResourceHandler<>() {
            @Override public int size() { return itemHandler.size(); }
            @Override public ItemResource getResource(int index) { return itemHandler.getResource(index); }
            @Override public long getAmountAsLong(int index) { return itemHandler.getAmountAsLong(index); }
            @Override public long getCapacityAsLong(int index, ItemResource resource) { return itemHandler.getCapacityAsLong(index, resource); }
            @Override public boolean isValid(int index, ItemResource resource) { return itemHandler.isValid(index, resource); }
            @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
                Tier t = getTier();
                if (index == FUEL_SLOT) return itemHandler.insert(index, resource, amount, transaction);
                if (index >= t.getInputStart() && index <= t.getInputEnd()) return itemHandler.insert(index, resource, amount, transaction);
                return 0;
            }
            @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) { return 0; }
        };
    }

    public int getEnergyCostPerProcess() {
        return tier.ticksPerProcess;
    }

    private ItemStack getUpgradeStack(int slot) {
        ItemResource resource = itemHandler.getResource(slot);
        int amount = itemHandler.getAmountAsInt(slot);
        if (resource.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        return resource.toStack(amount);
    }

    private float getEnergyMultiplier() {
        float mult = 1.0f;
        Tier t = getTier();
        for (int i = t.getUpgradeStart(); i <= t.getUpgradeEnd(); i++) {
            ItemStack stack = getUpgradeStack(i);
            if (UpgradeType.fromStack(stack) == UpgradeType.ENERGY) {
                int tierVal = UpgradeType.tierFromStack(stack);
                mult = switch (tierVal) { case 1 -> 0.8f; case 2 -> 0.7f; case 3 -> 0.6f; default -> 1.0f; };
            }
        }
        return mult;
    }

    private float getSpeedMultiplier() {
        float mult = 1.0f;
        Tier t = getTier();
        for (int i = t.getUpgradeStart(); i <= t.getUpgradeEnd(); i++) {
            ItemStack stack = getUpgradeStack(i);
            if (UpgradeType.fromStack(stack) == UpgradeType.SPEED) {
                int tierVal = UpgradeType.tierFromStack(stack);
                mult = switch (tierVal) { case 1 -> 1.5f; case 2 -> 1.7f; case 3 -> 2.0f; default -> 1.0f; };
            }
        }
        return mult;
    }

    private int getEffectiveTicks() {
        return Math.max(1, (int) (tier.ticksPerProcess / getSpeedMultiplier()));
    }

    private boolean isOutputFull() {
        Tier t = getTier();
        for (int i = t.getOutputStart(); i <= t.getOutputEnd(); i++) {
            if (itemHandler.getResource(i).isEmpty() || itemHandler.getAmountAsInt(i) <= 0) {
                return false;
            }
        }
        return true;
    }

    private boolean hasAnyInput() {
        Tier t = getTier();
        for (int i = t.getInputStart(); i <= t.getInputEnd(); i++) {
            ItemResource r = itemHandler.getResource(i);
            if (!r.isEmpty() && itemHandler.getAmountAsInt(i) > 0) return true;
        }
        return false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ProcessorBlockEntity be) {
        Tier tier = be.getTier();
        boolean wasBurning = be.isBurning();
        boolean dirty = false;

        if (level.hasNeighborSignal(pos)) {
            if (wasBurning) {
                level.setBlock(pos, state.setValue(ProcessorBlock.LIT, false), 3);
            }
            return;
        }

        boolean outputFull = be.isOutputFull();
        boolean hasInput = be.hasAnyInput();

        if (!outputFull && hasInput && be.energyBuffer > 0) {
            float rawCostPerTick = 1 * be.getEnergyMultiplier() * be.getSpeedMultiplier();
            be.energyFraction += rawCostPerTick;
            int cost = (int) be.energyFraction;
            be.energyFraction -= cost;
            be.energyBuffer = Math.max(0, be.energyBuffer - cost);
            be.progress++;
            dirty = true;

            if (be.progress >= be.getEffectiveTicks()) {
                be.progress = 0;
                be.processAllInputs();
            }
        }

        if (!outputFull && be.energyBuffer <= tier.energyCapacity - 1) {
            ItemResource fuelResource = be.itemHandler.getResource(FUEL_SLOT);
            int fuelAmount = be.itemHandler.getAmountAsInt(FUEL_SLOT);
            if (!fuelResource.isEmpty() && fuelAmount > 0) {
                ItemStack fuelStack = fuelResource.toStack(1);
                int burnTime = fuelStack.getBurnTime(RecipeType.SMELTING, level.fuelValues());
                if (burnTime > 0) {
                    int energy = Math.max(1, burnTime / 5);
                    if (energy > 0 && be.energyBuffer + energy <= tier.energyCapacity) {
                        be.energyBuffer += energy;
                        be.itemHandler.set(FUEL_SLOT, fuelResource, fuelAmount - 1);
                        dirty = true;
                    }
                }
            }
        }

        if (!outputFull && !be.isBurning() && be.progress != 0) {
            be.progress = 0;
            dirty = true;
        }

        boolean burningNow = be.isBurning() && !outputFull && hasInput;
        if (state.getValue(ProcessorBlock.LIT) != burningNow) {
            level.setBlock(pos, state.setValue(ProcessorBlock.LIT, burningNow), 3);
        }

        if (dirty) be.setChanged();
    }

    private void processAllInputs() {
        Tier t = getTier();
        for (int slot = t.getInputStart(); slot <= t.getInputEnd(); slot++) {
            processInput(slot);
        }
    }

    private void processInput(int slot) {
        ItemResource inputResource = itemHandler.getResource(slot);
        int inputAmount = itemHandler.getAmountAsInt(slot);
        if (inputResource.isEmpty() || inputAmount <= 0) return;

        ItemStack inputStack = inputResource.toStack(1);
        ItemStack result = getRecipeResult(inputStack);
        if (result == null) return;

        itemHandler.set(slot, inputResource, inputAmount - 1);

        ItemResource resultResource = ItemResource.of(result);
        int toPlace = result.getCount();
        Tier t = getTier();
        for (int i = t.getOutputStart(); i <= t.getOutputEnd() && toPlace > 0; i++) {
            ItemResource slotResource = itemHandler.getResource(i);
            int slotAmount = itemHandler.getAmountAsInt(i);
            if (slotResource.isEmpty()) {
                itemHandler.set(i, resultResource, Math.min(toPlace, result.getMaxStackSize()));
                toPlace -= Math.min(toPlace, result.getMaxStackSize());
            } else if (slotResource.equals(resultResource) && slotAmount < result.getMaxStackSize()) {
                int canAdd = result.getMaxStackSize() - slotAmount;
                int add = Math.min(canAdd, toPlace);
                itemHandler.set(i, resultResource, slotAmount + add);
                toPlace -= add;
            }
        }
        setChanged();
    }

    public boolean isBurning() { return energyBuffer > 0; }

    public void openMenu(ServerPlayer player) {
        player.openMenu(getMenuProvider(), worldPosition);
    }

    public MenuProvider getMenuProvider() {
        Tier t = getTier();
        String key = switch (t) {
            case TIER_1 -> "container.avoidminer.avoid_processor_tier_1";
            case TIER_2 -> "container.avoidminer.avoid_processor_tier_2";
            case TIER_3 -> "container.avoidminer.avoid_processor_tier_3";
        };
        return new SimpleMenuProvider(
                (id, playerInventory, player) -> new ProcessorMenu(id, playerInventory, itemHandler, data, t),
                Component.translatable(key)
        );
    }

    public ResourceHandler<ItemResource> getItemHandler() { return itemHandler; }

    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        if (side == null) return itemHandler;
        return side == Direction.DOWN ? bottomHandler : sideHandler;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Energy", energyBuffer);
        output.putInt("Progress", progress);
        output.putFloat("EnergyFraction", energyFraction);
        output.putInt("Tier", tier.tierLevel);
        output.putInt("SlotCount", tier.getTotalSlots());
        itemHandler.serialize(output.child("Inventory"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyBuffer = input.getIntOr("Energy", 0);
        progress = input.getIntOr("Progress", 0);
        energyFraction = input.getFloatOr("EnergyFraction", 0.0f);
        tierId = input.getIntOr("Tier", 1);
        int oldCount = input.getIntOr("SlotCount", 0);
        if (oldCount == 0) oldCount = tier.getTotalSlots();
        ItemStacksResourceHandler tmp = new ItemStacksResourceHandler(oldCount);
        tmp.deserialize(input.childOrEmpty("Inventory"));
        int slots = Math.min(tmp.size(), tier.getTotalSlots());
        for (int i = 0; i < slots; i++) {
            ItemResource r = tmp.getResource(i);
            int a = tmp.getAmountAsInt(i);
            if (!r.isEmpty() && a > 0) {
                itemHandler.set(i, r, a);
            }
        }
    }
}
