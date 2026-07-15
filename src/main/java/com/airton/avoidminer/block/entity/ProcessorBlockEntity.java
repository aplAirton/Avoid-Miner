package com.airton.avoidminer.block.entity;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.block.ProcessorBlock;
import com.airton.avoidminer.block.entity.AvoidMinerBlockEntity.UpgradeType;
import com.airton.avoidminer.energy.EnergyReceiver;
import com.airton.avoidminer.item.EnergyLinkItem;
import com.airton.avoidminer.menu.ProcessorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
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

import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

public class ProcessorBlockEntity extends BlockEntity implements EnergyReceiver {
    public static final int FUEL_SLOT = 0;
    public static final int MAX_INPUTS = 5;
    public static final int UPGRADE_COUNT = 2;

    public enum Tier {
        TIER_1(1, 4000, 200, 0.2F, 1),
        TIER_2(3, 8000, 100, 0.8F, 2),
        TIER_3(5, 12000, 50, 3.2F, 3);

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

    // Preenchido de forma preguiçosa: o primeiro uso acontece sempre depois do
    // registro dos itens (necessário porque o mapa referencia itens do próprio mod)
    private static final Map<Item, ItemStack> RECIPES = new LinkedHashMap<>();

    private static Map<Item, ItemStack> recipes() {
        if (RECIPES.isEmpty()) {
            buildRecipes();
        }
        return RECIPES;
    }

    private static void buildRecipes() {
        addRecipe(Items.COAL_ORE, Items.COAL, 4);
        addRecipe(Items.DEEPSLATE_COAL_ORE, Items.COAL, 4);
        addRecipe(Items.IRON_ORE, Items.RAW_IRON, 3);
        addRecipe(Items.DEEPSLATE_IRON_ORE, Items.RAW_IRON, 3);
        addRecipe(Items.COPPER_ORE, Items.RAW_COPPER, 6);
        addRecipe(Items.DEEPSLATE_COPPER_ORE, Items.RAW_COPPER, 6);
        addRecipe(Items.GOLD_ORE, Items.RAW_GOLD, 2);
        addRecipe(Items.DEEPSLATE_GOLD_ORE, Items.RAW_GOLD, 2);
        addRecipe(Items.REDSTONE_ORE, Items.REDSTONE, 4);
        addRecipe(Items.DEEPSLATE_REDSTONE_ORE, Items.REDSTONE, 4);
        addRecipe(Items.LAPIS_ORE, Items.LAPIS_LAZULI, 8);
        addRecipe(Items.DEEPSLATE_LAPIS_ORE, Items.LAPIS_LAZULI, 8);
        addRecipe(Items.EMERALD_ORE, Items.EMERALD, 2);
        addRecipe(Items.DEEPSLATE_EMERALD_ORE, Items.EMERALD, 2);
        addRecipe(Items.DIAMOND_ORE, Items.DIAMOND, 2);
        addRecipe(Items.DEEPSLATE_DIAMOND_ORE, Items.DIAMOND, 2);
        addRecipe(Items.NETHER_QUARTZ_ORE, Items.QUARTZ, 6);
        addRecipe(Items.NETHER_GOLD_ORE, Items.GOLD_NUGGET, 12);
        addRecipe(Items.ANCIENT_DEBRIS, Items.NETHERITE_SCRAP, 2);
        RECIPES.put(com.airton.avoidminer.ModItems.MAGNETITE_ORE.get().asItem(),
                new ItemStack(com.airton.avoidminer.ModItems.RAW_MAGNETITE.get(), 3));
        RECIPES.put(com.airton.avoidminer.ModItems.RAW_MAGNETITE_BLOCK.get().asItem(),
                new ItemStack(com.airton.avoidminer.ModItems.RAW_MAGNETITE.get(), 9));
        RECIPES.put(com.airton.avoidminer.ModItems.RAW_MAGNETITE.get().asItem(),
                new ItemStack(com.airton.avoidminer.ModItems.MAGNETITE_POWDER.get(), 2));
    }

    private static void addRecipe(Item inputItem, Item outputItem, int outputCount) {
        RECIPES.put(inputItem, new ItemStack(outputItem, outputCount));
    }

    public static Map<Item, ItemStack> getRecipeMap() {
        return recipes();
    }

    @Nullable
    public static ItemStack getRecipeResult(ItemStack input) {
        if (input.isEmpty()) return null;
        ItemStack result = recipes().get(input.getItem());
        return result == null ? null : result.copy();
    }

    public static boolean isProcessable(ItemStack stack) {
        return !stack.isEmpty() && recipes().containsKey(stack.getItem());
    }

    private final Tier tier;
    private int tierId;

    private final ItemStacksResourceHandler itemHandler;
    private final ResourceHandler<ItemResource> bottomHandler;
    private final ResourceHandler<ItemResource> sideHandler;

    private int energyBuffer;
    private final int[] progress = new int[MAX_INPUTS];
    private float energyFraction;
    private boolean autoBalance;

    // Data layout: 0=energy, 1=capacity, 2=effectiveTicks, 3=tierLevel, 4=inputCount,
    // 5=baseTicks, 6..10=progress, 11=stalled, 12=energy, 13=speed, 14=auto balance
    public static final int DATA_PROGRESS = 6;
    public static final int DATA_STALLED = DATA_PROGRESS + MAX_INPUTS;
    public static final int DATA_ENERGY_UPG = DATA_STALLED + 1;
    public static final int DATA_SPEED_UPG = DATA_ENERGY_UPG + 1;
    public static final int DATA_AUTO_BALANCE = DATA_SPEED_UPG + 1;
    public static final int DATA_SIZE = DATA_AUTO_BALANCE + 1;

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
        public int getCount() { return DATA_SIZE; }
    };

    private int upgradeTierAt(int slot) {
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
        return tier;
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
                    return isProcessable(stack);
                }
                if (slot >= tier.getOutputStart() && slot <= tier.getOutputEnd()) {
                    return false;
                }
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
        return new ResourceHandler<>() {
            @Override public int size() { return itemHandler.size(); }
            @Override public ItemResource getResource(int index) { return itemHandler.getResource(index); }
            @Override public long getAmountAsLong(int index) { return itemHandler.getAmountAsLong(index); }
            @Override public long getCapacityAsLong(int index, ItemResource resource) { return itemHandler.getCapacityAsLong(index, resource); }
            @Override public boolean isValid(int index, ItemResource resource) { return itemHandler.isValid(index, resource); }
            @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) { return 0; }
            @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
                if (index >= tier.getOutputStart() && index <= tier.getOutputEnd()) {
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
                if (index == FUEL_SLOT) return itemHandler.insert(index, resource, amount, transaction);
                if (index >= tier.getInputStart() && index <= tier.getInputEnd()) return itemHandler.insert(index, resource, amount, transaction);
                return 0;
            }
            @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) { return 0; }
        };
    }

    private ItemStack getStackIn(int slot) {
        ItemResource resource = itemHandler.getResource(slot);
        int amount = itemHandler.getAmountAsInt(slot);
        if (resource.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        return resource.toStack(amount);
    }

    private float getEnergyMultiplier() {
        ItemStack stack = getStackIn(tier.getEnergyUpgradeSlot());
        if (UpgradeType.fromStack(stack) != UpgradeType.ENERGY) return 1.0f;
        return switch (UpgradeType.tierFromStack(stack)) {
            case 1 -> 0.8f; case 2 -> 0.7f; case 3 -> 0.6f; default -> 1.0f;
        };
    }

    private float getSpeedMultiplier() {
        ItemStack stack = getStackIn(tier.getSpeedUpgradeSlot());
        if (UpgradeType.fromStack(stack) != UpgradeType.SPEED) return 1.0f;
        return switch (UpgradeType.tierFromStack(stack)) {
            case 1 -> 1.5f; case 2 -> 1.7f; case 3 -> 2.0f; default -> 1.0f;
        };
    }

    private int getEffectiveTicks() {
        return Math.max(1, (int) (tier.ticksPerProcess / getSpeedMultiplier()));
    }

    private boolean canOutputAccept(int outputSlot, ItemStack result) {
        ItemResource slotResource = itemHandler.getResource(outputSlot);
        int slotAmount = itemHandler.getAmountAsInt(outputSlot);
        if (slotResource.isEmpty()) return true;
        return slotResource.equals(ItemResource.of(result))
                && slotAmount + result.getCount() <= result.getMaxStackSize();
    }

    private boolean isAnyOutputBlocked() {
        for (int i = 0; i < tier.inputCount; i++) {
            int inputSlot = tier.getInputStart() + i;
            int outputSlot = tier.getOutputStart() + i;
            ItemResource inputRes = itemHandler.getResource(inputSlot);
            if (!inputRes.isEmpty() && itemHandler.getAmountAsInt(inputSlot) > 0) {
                ItemStack result = getRecipeResult(inputRes.toStack(1));
                if (result != null && !canOutputAccept(outputSlot, result)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ProcessorBlockEntity be) {
        Tier tier = be.getTier();
        boolean dirty = false;

        if (be.autoBalance && tier.inputCount > 1) {
            dirty = MachineInputBalancer.balance(be.itemHandler, tier.getInputStart(), tier.inputCount);
        }

        if (level.hasNeighborSignal(pos)) {
            if (state.getValue(ProcessorBlock.LIT)) {
                level.setBlock(pos, state.setValue(ProcessorBlock.LIT, false), 3);
            }
            return;
        }

        float energyMult = be.getEnergyMultiplier();
        float speedMult = be.getSpeedMultiplier();
        int maxProgress = be.getEffectiveTicks();

        boolean anyProcessing = false;

        for (int i = 0; i < tier.inputCount; i++) {
            int inputSlot = tier.getInputStart() + i;
            int outputSlot = tier.getOutputStart() + i;

            ItemResource inputRes = be.itemHandler.getResource(inputSlot);
            int inputAmount = be.itemHandler.getAmountAsInt(inputSlot);

            if (inputRes.isEmpty() || inputAmount <= 0) {
                if (be.progress[i] != 0) { be.progress[i] = 0; dirty = true; }
                continue;
            }

            ItemStack result = getRecipeResult(inputRes.toStack(1));
            if (result == null || !be.canOutputAccept(outputSlot, result)) {
                if (be.progress[i] != 0) { be.progress[i] = 0; dirty = true; }
                continue;
            }

            if (be.energyBuffer > 0) {
                // Custo por tick = 1 * energyMult * speedMult; custo total por processo
                // independe da velocidade (speed encurta o ciclo na mesma proporção)
                float rawCostPerTick = be.tier.baseEnergyPerTick * energyMult * speedMult;
                be.energyFraction += rawCostPerTick;
                int cost = (int) be.energyFraction;
                be.energyFraction -= cost;
                be.energyBuffer = Math.max(0, be.energyBuffer - cost);
                be.progress[i]++;
                dirty = true;
                anyProcessing = true;

                if (be.progress[i] >= maxProgress) {
                    be.progress[i] = 0;
                    be.processOneInput(inputSlot, outputSlot, result);
                }
            }
        }

        if (be.energyBuffer <= tier.energyCapacity - 1) {
            ItemResource fuelResource = be.itemHandler.getResource(FUEL_SLOT);
            int fuelAmount = be.itemHandler.getAmountAsInt(FUEL_SLOT);
            if (!fuelResource.isEmpty() && fuelAmount > 0) {
                ItemStack fuelStack = fuelResource.toStack(1);
                if (fuelStack.is(ModItems.ENERGY_LINK.get())) {
                    int pulled = EnergyLinkItem.drawEnergy(level, fuelStack, EnergyLinkItem.TRANSFER_PER_TICK);
                    if (pulled > 0) {
                        be.energyBuffer = Math.min(be.energyBuffer + pulled, tier.energyCapacity);
                        dirty = true;
                    }
                } else {
                    int burnTime = fuelStack.getBurnTime(RecipeType.SMELTING, level.fuelValues());
                    if (burnTime > 0) {
                        int energy = Math.max(1, burnTime / 5);
                        if (energy > 0 && be.energyBuffer + energy <= tier.energyCapacity) {
                            be.energyBuffer += energy;
                            if (fuelStack.is(Items.LAVA_BUCKET)) {
                                be.itemHandler.set(FUEL_SLOT, ItemResource.of(Items.BUCKET), 1);
                            } else {
                                be.itemHandler.set(FUEL_SLOT, fuelResource, fuelAmount - 1);
                            }
                            dirty = true;
                        }
                    }
                }
            }
        }

        boolean burningNow = anyProcessing && be.energyBuffer > 0;
        if (state.getValue(ProcessorBlock.LIT) != burningNow) {
            level.setBlock(pos, state.setValue(ProcessorBlock.LIT, burningNow), 3);
        }

        if (dirty) be.setChanged();
    }

    private void processOneInput(int inputSlot, int outputSlot, ItemStack result) {
        ItemResource inputResource = itemHandler.getResource(inputSlot);
        int inputAmount = itemHandler.getAmountAsInt(inputSlot);
        itemHandler.set(inputSlot, inputResource, inputAmount - 1);

        ItemResource resultResource = ItemResource.of(result);
        ItemResource slotResource = itemHandler.getResource(outputSlot);
        int slotAmount = itemHandler.getAmountAsInt(outputSlot);
        if (slotResource.isEmpty()) {
            itemHandler.set(outputSlot, resultResource, Math.min(result.getCount(), result.getMaxStackSize()));
        } else if (slotResource.equals(resultResource)) {
            int add = Math.min(result.getMaxStackSize() - slotAmount, result.getCount());
            itemHandler.set(outputSlot, resultResource, slotAmount + add);
        }
        setChanged();
    }

    public boolean isBurning() { return energyBuffer > 0; }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int maxAdd = Math.min(maxReceive, tier.energyCapacity - energyBuffer);
        if (maxAdd <= 0) return 0;
        if (!simulate) {
            energyBuffer += maxAdd;
            setChanged();
        }
        return maxAdd;
    }

    public void openMenu(ServerPlayer player) {
        player.openMenu(getMenuProvider(), worldPosition);
    }

    public MenuProvider getMenuProvider() {
        String key = switch (tier) {
            case TIER_1 -> "container.avoidminer.avoid_processor_tier_1";
            case TIER_2 -> "container.avoidminer.avoid_processor_tier_2";
            case TIER_3 -> "container.avoidminer.avoid_processor_tier_3";
        };
        return new SimpleMenuProvider(
                (id, playerInventory, player) -> new ProcessorMenu(
                        id, playerInventory, itemHandler, data, tier, this::toggleAutoBalance),
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

    public ResourceHandler<ItemResource> getItemHandler() { return itemHandler; }

    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        if (side == null) return itemHandler;
        return side == Direction.DOWN ? bottomHandler : sideHandler;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Energy", energyBuffer);
        for (int i = 0; i < MAX_INPUTS; i++) {
            output.putInt("Progress" + i, progress[i]);
        }
        output.putFloat("EnergyFraction", energyFraction);
        output.putInt("Tier", tier.tierLevel);
        output.putInt("SlotCount", tier.getTotalSlots());
        output.putInt("LayoutVersion", 2);
        output.putBoolean("AutoBalance", autoBalance);
        itemHandler.serialize(output.child("Inventory"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyBuffer = input.getIntOr("Energy", 0);
        for (int i = 0; i < MAX_INPUTS; i++) {
            progress[i] = input.getIntOr("Progress" + i, 0);
        }
        energyFraction = input.getFloatOr("EnergyFraction", 0.0f);
        tierId = input.getIntOr("Tier", tier.tierLevel);
        int oldCount = input.getIntOr("SlotCount", 0);
        if (oldCount == 0) oldCount = tier.getTotalSlots();
        int layoutVersion = input.getIntOr("LayoutVersion", 1);
        autoBalance = tier.inputCount > 1 && input.getBooleanOr("AutoBalance", false);

        ItemStacksResourceHandler tmp = new ItemStacksResourceHandler(Math.max(oldCount, tier.getTotalSlots()));
        tmp.deserialize(input.childOrEmpty("Inventory"));

        if (layoutVersion >= 2 && oldCount == tier.getTotalSlots()) {
            for (int i = 0; i < tier.getTotalSlots(); i++) {
                ItemResource r = tmp.getResource(i);
                int a = tmp.getAmountAsInt(i);
                if (!r.isEmpty() && a > 0) itemHandler.set(i, r, a);
            }
        } else {
            migrateLegacyInventory(tmp, oldCount);
        }
    }

    // Layout antigo (v1): fuel, N entradas, N saídas, M upgrades — com N/M diferentes dos atuais
    // (T3 tinha 6 entradas e 3 upgrades). Reencaixa cada seção no layout novo.
    private void migrateLegacyInventory(ItemStacksResourceHandler old, int oldCount) {
        int oldInputs, oldUpgrades;
        switch (oldCount) {
            case 5 -> { oldInputs = 1; oldUpgrades = 2; }
            case 9 -> { oldInputs = 3; oldUpgrades = 2; }
            case 16 -> { oldInputs = 6; oldUpgrades = 3; }
            default -> { oldInputs = Math.max(0, (oldCount - 1 - UPGRADE_COUNT) / 2); oldUpgrades = Math.max(0, oldCount - 1 - oldInputs * 2); }
        }

        ItemResource fuel = old.getResource(FUEL_SLOT);
        int fuelAmt = old.getAmountAsInt(FUEL_SLOT);
        if (!fuel.isEmpty() && fuelAmt > 0) itemHandler.set(FUEL_SLOT, fuel, fuelAmt);

        for (int i = 0; i < oldInputs; i++) {
            placeMigrated(old.getResource(1 + i), old.getAmountAsInt(1 + i),
                    i < tier.inputCount ? tier.getInputStart() + i : -1);
        }
        for (int i = 0; i < oldInputs; i++) {
            placeMigrated(old.getResource(1 + oldInputs + i), old.getAmountAsInt(1 + oldInputs + i),
                    i < tier.inputCount ? tier.getOutputStart() + i : -1);
        }
        for (int i = 0; i < oldUpgrades; i++) {
            ItemResource r = old.getResource(1 + oldInputs * 2 + i);
            int a = old.getAmountAsInt(1 + oldInputs * 2 + i);
            if (r.isEmpty() || a <= 0) continue;
            UpgradeType type = UpgradeType.fromStack(r.toStack(1));
            int target = switch (type) {
                case ENERGY -> tier.getEnergyUpgradeSlot();
                case SPEED -> tier.getSpeedUpgradeSlot();
                case null, default -> -1;
            };
            placeMigrated(r, a, target);
        }
    }

    private void placeMigrated(ItemResource resource, int amount, int preferredSlot) {
        if (resource.isEmpty() || amount <= 0) return;
        if (preferredSlot >= 0 && itemHandler.getResource(preferredSlot).isEmpty()) {
            itemHandler.set(preferredSlot, resource, amount);
            return;
        }
        for (int i = tier.getOutputStart(); i <= tier.getOutputEnd(); i++) {
            if (itemHandler.getResource(i).isEmpty()) {
                itemHandler.set(i, resource, amount);
                return;
            }
        }
    }
}
