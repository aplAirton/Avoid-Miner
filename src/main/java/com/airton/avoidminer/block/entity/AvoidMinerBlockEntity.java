package com.airton.avoidminer.block.entity;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.block.AvoidMinerBlock;
import com.airton.avoidminer.menu.AvoidMinerMenu;
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
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

public class AvoidMinerBlockEntity extends BlockEntity {
    public static final int FUEL_SLOT = 0;
    public static final int OUTPUT_START = 1;
    public static final int OUTPUT_COUNT = 27;
    public static final int UPGRADE_SLOT_START = 28;
    public static final int WORLD_SLOT = 31;
    public static final int ENCHANT_SLOT = 32;
    public static final int TOTAL_SLOTS = 33;

    private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) {
            setChanged();
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            if (resource.isEmpty()) return false;
            ItemStack stack = resource.toStack(1);
            if (slot == FUEL_SLOT) {
                return level != null && stack.getBurnTime(RecipeType.SMELTING, level.fuelValues()) > 0;
            }
            if (slot >= OUTPUT_START && slot < UPGRADE_SLOT_START) {
                return false;
            }
            if (slot == WORLD_SLOT) {
                return stack.is(ModItems.BOTANY_UPGRADE.get()) || stack.is(ModItems.NETHER_UPGRADE.get());
            }
            if (slot == ENCHANT_SLOT) {
                return stack.is(ModItems.FORTUNE_UPGRADE.get()) || stack.is(ModItems.SILK_UPGRADE.get());
            }
            if (slot >= UPGRADE_SLOT_START && slot < WORLD_SLOT) {
                int upgradeSlotIndex = slot - UPGRADE_SLOT_START;
                int allowedSlots = getUpgradeSlotCount();
                if (upgradeSlotIndex >= allowedSlots) return false;
                UpgradeType type = UpgradeType.fromStack(stack);
                if (type == null) return false;
                int upgradeTier = UpgradeType.tierFromStack(stack);
                int maxTier = getTier().ordinal() + 1;
                if (upgradeTier > maxTier) return false;
                for (int i = UPGRADE_SLOT_START; i < WORLD_SLOT; i++) {
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

    private final ResourceHandler<ItemResource> bottomHandler = new ResourceHandler<>() {
        @Override public int size() { return itemHandler.size(); }
        @Override public ItemResource getResource(int index) { return itemHandler.getResource(index); }
        @Override public long getAmountAsLong(int index) { return itemHandler.getAmountAsLong(index); }
        @Override public long getCapacityAsLong(int index, ItemResource resource) { return itemHandler.getCapacityAsLong(index, resource); }
        @Override public boolean isValid(int index, ItemResource resource) { return itemHandler.isValid(index, resource); }
        @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) { return 0; }
        @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index >= OUTPUT_START && index < UPGRADE_SLOT_START) {
                return itemHandler.extract(index, resource, amount, transaction);
            }
            return 0;
        }
    };

    private final ResourceHandler<ItemResource> sideHandler = new ResourceHandler<>() {
        @Override public int size() { return itemHandler.size(); }
        @Override public ItemResource getResource(int index) { return itemHandler.getResource(index); }
        @Override public long getAmountAsLong(int index) { return itemHandler.getAmountAsLong(index); }
        @Override public long getCapacityAsLong(int index, ItemResource resource) { return itemHandler.getCapacityAsLong(index, resource); }
        @Override public boolean isValid(int index, ItemResource resource) { return itemHandler.isValid(index, resource); }
        @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index == FUEL_SLOT) return itemHandler.insert(index, resource, amount, transaction);
            if (index == WORLD_SLOT) return itemHandler.insert(index, resource, amount, transaction);
            if (index == ENCHANT_SLOT) return itemHandler.insert(index, resource, amount, transaction);
            return 0;
        }
        @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) { return 0; }
    };

    private int energyBuffer;
    private int progress;
    private int tierId;
    private float energyFraction;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyBuffer;
                case 1 -> getEnergyCapacity();
                case 2 -> progress;
                case 3 -> getEffectiveTicks();
                case 4 -> upgradeTypeId(UPGRADE_SLOT_START);
                case 5 -> upgradeTierId(UPGRADE_SLOT_START);
                case 6 -> upgradeTypeId(UPGRADE_SLOT_START + 1);
                case 7 -> upgradeTierId(UPGRADE_SLOT_START + 1);
                case 8 -> upgradeTypeId(UPGRADE_SLOT_START + 2);
                case 9 -> upgradeTierId(UPGRADE_SLOT_START + 2);
                case 10 -> getUpgradeSlotCount();
                case 11 -> getTier().ordinal() + 1;
                case 12 -> getEnergyCostPerGeneration();
                case 13 -> getWorldMode();
                case 14 -> getEnchantMode();
                case 15 -> isOutputFull() ? 1 : 0;
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
        public int getCount() { return 16; }
    };

    public int getEnergyCostPerGeneration() {
        return Math.max(1, Math.round(getTier().energyPerTick * getTier().ticksPerGeneration * getEnergyMultiplier()));
    }

    public int getWorldMode() {
        ItemResource r = itemHandler.getResource(WORLD_SLOT);
        if (r.isEmpty()) return 0;
        ItemStack stack = r.toStack(1);
        if (stack.is(ModItems.BOTANY_UPGRADE.get())) return 1;
        if (stack.is(ModItems.NETHER_UPGRADE.get())) return 2;
        return 0;
    }

    public int getEnchantMode() {
        ItemResource r = itemHandler.getResource(ENCHANT_SLOT);
        if (r.isEmpty()) return 0;
        ItemStack stack = r.toStack(1);
        if (stack.is(ModItems.FORTUNE_UPGRADE.get())) return 1;
        if (stack.is(ModItems.SILK_UPGRADE.get())) return 2;
        return 0;
    }

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

    public AvoidMinerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AVOID_MINER.get(), pos, state);
        this.tierId = getTierIdFromBlock(state);
    }

    private int getTierIdFromBlock(BlockState state) {
        if (state.getBlock() instanceof AvoidMinerBlock block) {
            return block.getTier().ordinal() + 1;
        }
        return 3;
    }

    private Tier getTierFromBlock(BlockState state) {
        if (state.getBlock() instanceof AvoidMinerBlock block) {
            return block.getTier();
        }
        return Tier.TIER_3;
    }

    public enum UpgradeType {
        MINING, ENERGY, SPEED;

        @Nullable
        public static UpgradeType fromStack(ItemStack stack) {
            if (stack.isEmpty()) return null;
            if (stack.is(ModItems.MINING_UPGRADE_TIER_1.get())
                    || stack.is(ModItems.MINING_UPGRADE_TIER_2.get())
                    || stack.is(ModItems.MINING_UPGRADE_TIER_3.get())) return MINING;
            if (stack.is(ModItems.ENERGY_UPGRADE_TIER_1.get())
                    || stack.is(ModItems.ENERGY_UPGRADE_TIER_2.get())
                    || stack.is(ModItems.ENERGY_UPGRADE_TIER_3.get())) return ENERGY;
            if (stack.is(ModItems.SPEED_UPGRADE_TIER_1.get())
                    || stack.is(ModItems.SPEED_UPGRADE_TIER_2.get())
                    || stack.is(ModItems.SPEED_UPGRADE_TIER_3.get())) return SPEED;
            return null;
        }

        public static int tierFromStack(ItemStack stack) {
            if (stack.is(ModItems.MINING_UPGRADE_TIER_1.get())
                    || stack.is(ModItems.ENERGY_UPGRADE_TIER_1.get())
                    || stack.is(ModItems.SPEED_UPGRADE_TIER_1.get())) return 1;
            if (stack.is(ModItems.MINING_UPGRADE_TIER_2.get())
                    || stack.is(ModItems.ENERGY_UPGRADE_TIER_2.get())
                    || stack.is(ModItems.SPEED_UPGRADE_TIER_2.get())) return 2;
            if (stack.is(ModItems.MINING_UPGRADE_TIER_3.get())
                    || stack.is(ModItems.ENERGY_UPGRADE_TIER_3.get())
                    || stack.is(ModItems.SPEED_UPGRADE_TIER_3.get())) return 3;
            return 0;
        }
    }

    public enum Tier {
        TIER_1(2000, 80, 1, 0.30f),
        TIER_2(4000, 60, 1, 0.40f),
        TIER_3(8000, 40, 1, 0.60f);

        public final int energyCapacity;
        public final int ticksPerGeneration;
        public final int energyPerTick;
        public final float successChance;
        public final Supplier<List<WeightedResource>> resources;
        public final Supplier<List<WeightedResource>> botanyResources;
        public final Supplier<List<WeightedResource>> netherResources;

        Tier(int energyCapacity, int ticksPerGeneration, int energyPerTick, float successChance) {
            this.energyCapacity = energyCapacity;
            this.ticksPerGeneration = ticksPerGeneration;
            this.energyPerTick = energyPerTick;
            this.successChance = successChance;
            this.resources = switch (this) {
                case TIER_1 -> () -> List.of(
                    new WeightedResource(new ItemStack(Items.COAL), 556),
                    new WeightedResource(new ItemStack(Items.RAW_IRON), 250),
                    new WeightedResource(new ItemStack(Items.REDSTONE), 100),
                    new WeightedResource(new ItemStack(Items.LAPIS_LAZULI), 60),
                    new WeightedResource(new ItemStack(Items.RAW_GOLD), 25),
                    new WeightedResource(new ItemStack(Items.EMERALD), 6),
                    new WeightedResource(new ItemStack(Items.DIAMOND), 3)
                );
                case TIER_2 -> () -> List.of(
                    new WeightedResource(new ItemStack(Items.COAL), 510),
                    new WeightedResource(new ItemStack(Items.RAW_IRON), 200),
                    new WeightedResource(new ItemStack(Items.REDSTONE), 120),
                    new WeightedResource(new ItemStack(Items.LAPIS_LAZULI), 80),
                    new WeightedResource(new ItemStack(Items.RAW_GOLD), 50),
                    new WeightedResource(new ItemStack(Items.EMERALD), 25),
                    new WeightedResource(new ItemStack(Items.DIAMOND), 15)
                );
                case TIER_3 -> () -> List.of(
                    new WeightedResource(new ItemStack(Items.COAL), 450),
                    new WeightedResource(new ItemStack(Items.RAW_IRON), 210),
                    new WeightedResource(new ItemStack(Items.REDSTONE), 120),
                    new WeightedResource(new ItemStack(Items.LAPIS_LAZULI), 90),
                    new WeightedResource(new ItemStack(Items.RAW_GOLD), 75),
                    new WeightedResource(new ItemStack(Items.EMERALD), 40),
                    new WeightedResource(new ItemStack(Items.DIAMOND), 30)
                );
            };
            this.botanyResources = switch (this) {
                case TIER_1 -> () -> List.of(
                    new WeightedResource(new ItemStack(Items.OAK_LOG), 350),
                    new WeightedResource(new ItemStack(Items.BIRCH_LOG), 250),
                    new WeightedResource(new ItemStack(Items.SPRUCE_LOG), 200),
                    new WeightedResource(new ItemStack(Items.STICK), 100),
                    new WeightedResource(new ItemStack(Items.OAK_SAPLING), 50),
                    new WeightedResource(new ItemStack(Items.BIRCH_SAPLING), 50)
                );
                case TIER_2 -> () -> List.of(
                    new WeightedResource(new ItemStack(Items.OAK_LOG), 250),
                    new WeightedResource(new ItemStack(Items.BIRCH_LOG), 180),
                    new WeightedResource(new ItemStack(Items.SPRUCE_LOG), 150),
                    new WeightedResource(new ItemStack(Items.JUNGLE_LOG), 150),
                    new WeightedResource(new ItemStack(Items.ACACIA_LOG), 120),
                    new WeightedResource(new ItemStack(Items.DARK_OAK_LOG), 120),
                    new WeightedResource(new ItemStack(Items.STICK), 80),
                    new WeightedResource(new ItemStack(Items.OAK_SAPLING), 40),
                    new WeightedResource(new ItemStack(Items.SPRUCE_SAPLING), 25),
                    new WeightedResource(new ItemStack(Items.BIRCH_SAPLING), 25)
                );
                case TIER_3 -> () -> List.of(
                    new WeightedResource(new ItemStack(Items.OAK_LOG), 180),
                    new WeightedResource(new ItemStack(Items.BIRCH_LOG), 130),
                    new WeightedResource(new ItemStack(Items.SPRUCE_LOG), 110),
                    new WeightedResource(new ItemStack(Items.JUNGLE_LOG), 110),
                    new WeightedResource(new ItemStack(Items.ACACIA_LOG), 90),
                    new WeightedResource(new ItemStack(Items.DARK_OAK_LOG), 90),
                    new WeightedResource(new ItemStack(Items.CHERRY_LOG), 80),
                    new WeightedResource(new ItemStack(Items.MANGROVE_LOG), 60),
                    new WeightedResource(new ItemStack(Items.BAMBOO), 70),
                    new WeightedResource(new ItemStack(Items.STICK), 60),
                    new WeightedResource(new ItemStack(Items.OAK_SAPLING), 40),
                    new WeightedResource(new ItemStack(Items.SPRUCE_SAPLING), 25),
                    new WeightedResource(new ItemStack(Items.BIRCH_SAPLING), 25),
                    new WeightedResource(new ItemStack(Items.JUNGLE_SAPLING), 15),
                    new WeightedResource(new ItemStack(Items.ACACIA_SAPLING), 15),
                    new WeightedResource(new ItemStack(Items.DARK_OAK_SAPLING), 15),
                    new WeightedResource(new ItemStack(Items.CHERRY_SAPLING), 10),
                    new WeightedResource(new ItemStack(Items.MANGROVE_PROPAGULE), 10),
                    new WeightedResource(new ItemStack(Items.VINE), 30),
                    new WeightedResource(new ItemStack(Items.MOSS_BLOCK), 20)
                );
            };
            this.netherResources = switch (this) {
                case TIER_1 -> () -> List.of(
                    new WeightedResource(new ItemStack(Items.QUARTZ), 700),
                    new WeightedResource(new ItemStack(Items.GOLD_NUGGET), 250),
                    new WeightedResource(new ItemStack(Items.ANCIENT_DEBRIS), 50)
                );
                case TIER_2 -> () -> List.of(
                    new WeightedResource(new ItemStack(Items.QUARTZ), 550),
                    new WeightedResource(new ItemStack(Items.GOLD_NUGGET), 350),
                    new WeightedResource(new ItemStack(Items.ANCIENT_DEBRIS), 100)
                );
                case TIER_3 -> () -> List.of(
                    new WeightedResource(new ItemStack(Items.QUARTZ), 400),
                    new WeightedResource(new ItemStack(Items.GOLD_NUGGET), 450),
                    new WeightedResource(new ItemStack(Items.ANCIENT_DEBRIS), 150)
                );
            };
        }
    }

    public Tier getTier() {
        if (level != null) {
            return getTierFromBlock(level.getBlockState(worldPosition));
        }
        return Tier.values()[Math.clamp(tierId - 1, 0, 2)];
    }

    public int getEnergyCapacity() { return getTier().energyCapacity; }
    public int getTicksPerGeneration() { return getTier().ticksPerGeneration; }
    public int getUpgradeSlotCount() { return getTier().ordinal() + 1; }

    private ItemStack getUpgradeStack(int slot) {
        ItemResource resource = itemHandler.getResource(slot);
        int amount = itemHandler.getAmountAsInt(slot);
        if (resource.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        return resource.toStack(amount);
    }

    private float getMiningMultiplier() {
        float mult = 1.0f;
        for (int i = 0; i < getUpgradeSlotCount(); i++) {
            ItemStack stack = getUpgradeStack(UPGRADE_SLOT_START + i);
            if (UpgradeType.fromStack(stack) == UpgradeType.MINING) {
                int t = UpgradeType.tierFromStack(stack);
                mult = switch (t) { case 1 -> 1.5f; case 2 -> 1.7f; case 3 -> 2.0f; default -> 1.0f; };
            }
        }
        return mult;
    }

    private float getEnergyMultiplier() {
        float mult = 1.0f;
        for (int i = 0; i < getUpgradeSlotCount(); i++) {
            ItemStack stack = getUpgradeStack(UPGRADE_SLOT_START + i);
            if (UpgradeType.fromStack(stack) == UpgradeType.ENERGY) {
                int t = UpgradeType.tierFromStack(stack);
                mult = switch (t) { case 1 -> 0.8f; case 2 -> 0.7f; case 3 -> 0.6f; default -> 1.0f; };
            }
        }
        return mult;
    }

    private float getSpeedMultiplier() {
        float mult = 1.0f;
        for (int i = 0; i < getUpgradeSlotCount(); i++) {
            ItemStack stack = getUpgradeStack(UPGRADE_SLOT_START + i);
            if (UpgradeType.fromStack(stack) == UpgradeType.SPEED) {
                int t = UpgradeType.tierFromStack(stack);
                mult = switch (t) { case 1 -> 1.5f; case 2 -> 1.7f; case 3 -> 2.0f; default -> 1.0f; };
            }
        }
        return mult;
    }

    private int getEffectiveTicks() {
        return Math.max(1, (int) (getTier().ticksPerGeneration / getSpeedMultiplier()));
    }

    private List<WeightedResource> getActivePool(Tier tier) {
        return switch (getWorldMode()) {
            case 1 -> tier.botanyResources.get();
            case 2 -> tier.netherResources.get();
            default -> tier.resources.get();
        };
    }

    private boolean isOutputFull() {
        for (int i = OUTPUT_START; i < UPGRADE_SLOT_START; i++) {
            if (itemHandler.getResource(i).isEmpty() || itemHandler.getAmountAsInt(i) <= 0) {
                return false;
            }
        }
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AvoidMinerBlockEntity be) {
        Tier tier = be.getTier();
        boolean wasBurning = be.isBurning();
        boolean dirty = false;

        if (level.hasNeighborSignal(pos)) {
            if (wasBurning) {
                level.setBlock(pos, state.setValue(AvoidMinerBlock.LIT, false), 3);
            }
            return;
        }

        boolean outputFull = be.isOutputFull();

        if (!outputFull && be.energyBuffer > 0) {
            // Per-tick cost = energyPerTick * energyMult * speedMult
            // Total per gen = ticksPerGen * energyPerTick * energyMult (speed cancels out)
            float rawCostPerTick = tier.energyPerTick * be.getEnergyMultiplier() * be.getSpeedMultiplier();
            be.energyFraction += rawCostPerTick;
            int cost = (int) be.energyFraction;
            be.energyFraction -= cost;
            be.energyBuffer = Math.max(0, be.energyBuffer - cost);
            be.progress++;
            dirty = true;

            if (be.progress >= be.getEffectiveTicks()) {
                be.progress = 0;
                float chance = tier.successChance * be.getMiningMultiplier();
                if (level.getRandom().nextFloat() < chance) {
                    be.generateResource(tier);
                }
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

        boolean burningNow = be.isBurning() && !outputFull;
        if (state.getValue(AvoidMinerBlock.LIT) != burningNow) {
            level.setBlock(pos, state.setValue(AvoidMinerBlock.LIT, burningNow), 3);
        }

        if (dirty) be.setChanged();
    }

    private void generateResource(Tier tier) {
        List<WeightedResource> pool = getActivePool(tier);
        int totalWeight = pool.stream().mapToInt(WeightedResource::weight).sum();
        int roll = level.getRandom().nextInt(totalWeight);
        int cumulative = 0;
        ItemStack result = null;
        for (WeightedResource r : pool) {
            cumulative += r.weight;
            if (roll < cumulative) {
                result = r.stack.copy();
                break;
            }
        }
        if (result == null) result = new ItemStack(Items.COAL);

        int amount = 1;
        int worldMode = getWorldMode();
        int enchantMode = getEnchantMode();
        boolean isAncientDebris = result.is(Items.ANCIENT_DEBRIS);

        // Fortune: multiply output (not for botany, not for ancient_debris)
        if (enchantMode == 1 && worldMode != 1 && !isAncientDebris) {
            amount = level.getRandom().nextInt(4) + 1; // 1-4, avg 2.5
        }

        // Silk: convert to ore blocks (overworld and nether only, not for botany)
        if (enchantMode == 2 && worldMode != 1) {
            result = applySilkTouch(result, worldMode == 2, level.getRandom().nextBoolean());
        }

        result.setCount(amount);

        ItemResource resultResource = ItemResource.of(result);
        int toPlace = result.getCount();
        for (int i = OUTPUT_START; i < UPGRADE_SLOT_START && toPlace > 0; i++) {
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

        // Upgrade drops
        ItemStack upgradeDrop = null;
        if (tier == Tier.TIER_1 && worldMode == 2 && level.getRandom().nextFloat() < 0.005f) {
            upgradeDrop = new ItemStack(ModItems.UPGRADE_TIER_2.get());
        } else if (tier == Tier.TIER_2 && worldMode == 2 && enchantMode == 1 && level.getRandom().nextFloat() < 0.005f) {
            upgradeDrop = new ItemStack(ModItems.UPGRADE_TIER_3.get());
        }
        if (upgradeDrop != null) {
            ItemResource upgRes = ItemResource.of(upgradeDrop);
            for (int i = OUTPUT_START; i < UPGRADE_SLOT_START; i++) {
                ItemResource existing = itemHandler.getResource(i);
                int amt = itemHandler.getAmountAsInt(i);
                if (existing.isEmpty()) {
                    itemHandler.set(i, upgRes, 1);
                    break;
                }
                if (existing.equals(upgRes) && amt < upgradeDrop.getMaxStackSize()) {
                    itemHandler.set(i, upgRes, amt + 1);
                    break;
                }
            }
        }
    }

    private ItemStack applySilkTouch(ItemStack input, boolean nether, boolean deepslate) {
        var item = input.getItem();
        if (nether) {
            if (item == Items.QUARTZ) return new ItemStack(Items.NETHER_QUARTZ_ORE);
            if (item == Items.GOLD_NUGGET) return new ItemStack(Items.NETHER_GOLD_ORE);
            return input;
        }
        if (item == Items.COAL) return new ItemStack(deepslate ? Items.DEEPSLATE_COAL_ORE : Items.COAL_ORE);
        if (item == Items.RAW_IRON) return new ItemStack(deepslate ? Items.DEEPSLATE_IRON_ORE : Items.IRON_ORE);
        if (item == Items.RAW_GOLD) return new ItemStack(deepslate ? Items.DEEPSLATE_GOLD_ORE : Items.GOLD_ORE);
        if (item == Items.REDSTONE) return new ItemStack(deepslate ? Items.DEEPSLATE_REDSTONE_ORE : Items.REDSTONE_ORE);
        if (item == Items.LAPIS_LAZULI) return new ItemStack(deepslate ? Items.DEEPSLATE_LAPIS_ORE : Items.LAPIS_ORE);
        if (item == Items.EMERALD) return new ItemStack(deepslate ? Items.DEEPSLATE_EMERALD_ORE : Items.EMERALD_ORE);
        if (item == Items.DIAMOND) return new ItemStack(deepslate ? Items.DEEPSLATE_DIAMOND_ORE : Items.DIAMOND_ORE);
        return input;
    }

    public boolean isBurning() { return energyBuffer > 0; }

    public void openMenu(ServerPlayer player) {
        player.openMenu(getMenuProvider(), worldPosition);
    }

    public MenuProvider getMenuProvider() {
        Tier t = getTier();
        String key = switch (t) {
            case TIER_1 -> "container.avoidminer.avoid_miner_tier_1";
            case TIER_2 -> "container.avoidminer.avoid_miner_tier_2";
            case TIER_3 -> "container.avoidminer.avoid_miner_tier_3";
        };
        return new SimpleMenuProvider(
                (id, playerInventory, player) -> new AvoidMinerMenu(id, playerInventory, itemHandler, data),
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
        output.putInt("SlotCount", TOTAL_SLOTS);
        itemHandler.serialize(output.child("Inventory"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyBuffer = input.getIntOr("Energy", 0);
        progress = input.getIntOr("Progress", 0);
        energyFraction = input.getFloatOr("EnergyFraction", 0.0f);
        int oldCount = input.getIntOr("SlotCount", 0);
        if (oldCount == 0) oldCount = 30;
        ItemStacksResourceHandler tmp = new ItemStacksResourceHandler(oldCount);
        tmp.deserialize(input.childOrEmpty("Inventory"));
        int slots = Math.min(tmp.size(), TOTAL_SLOTS);
        for (int i = 0; i < slots; i++) {
            ItemResource r = tmp.getResource(i);
            int a = tmp.getAmountAsInt(i);
            if (!r.isEmpty() && a > 0) {
                itemHandler.set(i, r, a);
            }
        }
    }

    public record WeightedResource(ItemStack stack, int weight) {}
}
