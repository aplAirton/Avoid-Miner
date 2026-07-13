package com.airton.avoidminer.block.entity;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.block.LootrBlock;
import com.airton.avoidminer.item.EnergyLinkItem;
import com.airton.avoidminer.lootr.MobCardItem;
import com.airton.avoidminer.lootr.MobCardType;
import com.airton.avoidminer.menu.LootrMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LootrBlockEntity extends BlockEntity {
    public static final int FUEL_SLOT = 0;
    public static final int CARD_SLOT = 1;
    public static final int OUTPUT_START = 2;
    public static final int OUTPUT_COUNT = 27;
    public static final int UPG_SPEED = 29;
    public static final int UPG_LOOT = 30;
    public static final int UPG_RARITY = 31;
    public static final int TOTAL_SLOTS = 32;

    private static final int ENERGY_CAPACITY = 20000;
    private static final int BASE_TICKS_PER_OPERATION = 200;
    private static final int ENERGY_PER_TICK_BASE = 4;

    private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) { setChanged(); }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            if (resource.isEmpty()) return false;
            ItemStack stack = resource.toStack(1);
            if (slot == FUEL_SLOT) {
                return (level != null && stack.getBurnTime(RecipeType.SMELTING, level.fuelValues()) > 0)
                        || stack.is(ModItems.ENERGY_LINK.get());
            }
            if (slot == CARD_SLOT) {
                return stack.getItem() instanceof MobCardItem;
            }
            if (slot >= OUTPUT_START && slot < OUTPUT_START + OUTPUT_COUNT) {
                return false;
            }
            if (slot == UPG_SPEED)
                return stack.is(ModItems.SPEED_UPGRADE_TIER_1.get())
                    || stack.is(ModItems.SPEED_UPGRADE_TIER_2.get())
                    || stack.is(ModItems.SPEED_UPGRADE_TIER_3.get());
            if (slot == UPG_LOOT)
                return stack.is(ModItems.LOOT_UPGRADE.get());
            if (slot == UPG_RARITY)
                return stack.is(ModItems.RARITY_UPGRADE.get());
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
            if (index >= OUTPUT_START && index < OUTPUT_START + OUTPUT_COUNT)
                return itemHandler.extract(index, resource, amount, transaction);
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
            if (index == FUEL_SLOT || index == CARD_SLOT) return itemHandler.insert(index, resource, amount, transaction);
            return 0;
        }
        @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) { return 0; }
    };

    private int energyBuffer;
    private int progress;
    private float energyFraction;
    private boolean lastOperationSucceeded;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> energyBuffer;
                case 1 -> ENERGY_CAPACITY;
                case 2 -> progress;
                case 3 -> getEffectiveTicks();
                case 4 -> hasRarityUpgrade() ? 1 : 0;
                case 5 -> getLootingLevel();
                case 6 -> hasValidCard() ? 1 : 0;
                case 7 -> isOutputFull() ? 1 : 0;
                case 8 -> getSpeedUpgradeTier();
                case 9 -> lastOperationSucceeded ? 1 : 0;
                case 10 -> hasCard() ? 1 : 0;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> energyBuffer = value;
                case 2 -> progress = value;
            }
        }
        @Override public int getCount() { return 11; }
    };

    public LootrBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LOOTR.get(), pos, state);
    }

    private int getSpeedUpgradeTier() {
        ItemStack stack = getUpgradeStack(UPG_SPEED);
        if (stack.is(ModItems.SPEED_UPGRADE_TIER_1.get())) return 1;
        if (stack.is(ModItems.SPEED_UPGRADE_TIER_2.get())) return 2;
        if (stack.is(ModItems.SPEED_UPGRADE_TIER_3.get())) return 3;
        return 0;
    }

    private float getSpeedMultiplier() {
        ItemStack stack = getUpgradeStack(UPG_SPEED);
        if (stack.is(ModItems.SPEED_UPGRADE_TIER_1.get())) return 1.5f;
        if (stack.is(ModItems.SPEED_UPGRADE_TIER_2.get())) return 1.7f;
        if (stack.is(ModItems.SPEED_UPGRADE_TIER_3.get())) return 2.0f;
        return 1.0f;
    }

    private boolean hasRarityUpgrade() {
        return getUpgradeStack(UPG_RARITY).is(ModItems.RARITY_UPGRADE.get());
    }

    private int getLootingLevel() {
        ItemStack stack = getUpgradeStack(UPG_LOOT);
        if (stack.is(ModItems.LOOT_UPGRADE.get())) return ModItems.lootUpgradeLevel(stack);
        return 0;
    }

    private int getEffectiveTicks() {
        return Math.max(1, (int) (BASE_TICKS_PER_OPERATION / getSpeedMultiplier()));
    }

    private boolean hasValidCard() {
        ItemStack stack = getSlotStack(CARD_SLOT);
        return stack.getItem() instanceof MobCardItem card
                && MobCardItem.isCompleted(stack, card.getCardType());
    }

    private boolean hasCard() {
        return getSlotStack(CARD_SLOT).getItem() instanceof MobCardItem;
    }

    @Nullable
    private MobCardType getActiveCardType() {
        ItemStack stack = getSlotStack(CARD_SLOT);
        if (stack.getItem() instanceof MobCardItem card && MobCardItem.isCompleted(stack, card.getCardType())) {
            return card.getCardType();
        }
        return null;
    }

    private boolean isOutputFull() {
        for (int i = OUTPUT_START; i < OUTPUT_START + OUTPUT_COUNT; i++) {
            ItemResource r = itemHandler.getResource(i);
            if (r.isEmpty() || itemHandler.getAmountAsInt(i) <= 0) return false;
        }
        return true;
    }

    private ItemStack getSlotStack(int slot) {
        ItemResource r = itemHandler.getResource(slot);
        if (r.isEmpty()) return ItemStack.EMPTY;
        return r.toStack(itemHandler.getAmountAsInt(slot));
    }

    private ItemStack getUpgradeStack(int slot) {
        ItemResource r = itemHandler.getResource(slot);
        if (r.isEmpty()) return ItemStack.EMPTY;
        return r.toStack(1);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LootrBlockEntity be) {
        boolean dirty = false;
        if (level.hasNeighborSignal(pos)) {
            if (state.getValue(LootrBlock.LIT)) {
                level.setBlock(pos, state.setValue(LootrBlock.LIT, false), 3);
            }
            return;
        }

        MobCardType card = be.getActiveCardType();
        boolean outputFull = be.isOutputFull();
        boolean canProcess = card != null && !outputFull;

        if (canProcess && be.energyBuffer > 0) {
            float rawCostPerTick = ENERGY_PER_TICK_BASE * be.getSpeedMultiplier();
            be.energyFraction += rawCostPerTick;
            int cost = (int) be.energyFraction;
            be.energyFraction -= cost;
            be.energyBuffer = Math.max(0, be.energyBuffer - cost);
            be.progress++;
            dirty = true;

            if (be.progress >= be.getEffectiveTicks()) {
                be.progress = 0;
                be.processOneOperation(card);
            }
        }

        if (be.energyBuffer < ENERGY_CAPACITY - 1) {
            ItemResource fuelRes = be.itemHandler.getResource(FUEL_SLOT);
            int fuelAmt = be.itemHandler.getAmountAsInt(FUEL_SLOT);
            if (!fuelRes.isEmpty() && fuelAmt > 0) {
                ItemStack fuelStack = fuelRes.toStack(1);
                if (fuelStack.is(ModItems.ENERGY_LINK.get())) {
                    int pulled = EnergyLinkItem.drawEnergy(level, fuelStack, EnergyLinkItem.TRANSFER_PER_TICK);
                    if (pulled > 0) {
                        be.energyBuffer = Math.min(be.energyBuffer + pulled, ENERGY_CAPACITY);
                        dirty = true;
                    }
                } else {
                    int burnTime = fuelStack.getBurnTime(RecipeType.SMELTING, level.fuelValues());
                    if (burnTime > 0) {
                        int energy = Math.max(1, burnTime / 5);
                        if (be.energyBuffer + energy <= ENERGY_CAPACITY) {
                            be.energyBuffer += energy;
                            if (fuelStack.is(Items.LAVA_BUCKET)) {
                                be.itemHandler.set(FUEL_SLOT, ItemResource.of(Items.BUCKET), 1);
                            } else {
                                be.itemHandler.set(FUEL_SLOT, fuelRes, fuelAmt - 1);
                            }
                            dirty = true;
                        }
                    }
                }
            }
        }

        boolean burning = canProcess && be.energyBuffer > 0;
        if (state.getValue(LootrBlock.LIT) != burning) {
            level.setBlock(pos, state.setValue(LootrBlock.LIT, burning), 3);
        }

        if (dirty) be.setChanged();
    }

    private void processOneOperation(MobCardType card) {
        RandomSource rng = level.getRandom();
        int lootingLevel = getLootingLevel();
        boolean rarity = hasRarityUpgrade();
        boolean producedAny = false;

        List<MobCardType.LootEntry> drops = card.normalDrops();
        for (MobCardType.LootEntry entry : drops) {
            if (rng.nextFloat() < entry.chance()) {
                int amount = entry.min() + (entry.max() > entry.min() ? rng.nextInt(entry.max() - entry.min() + 1) : 0);
                if (entry.lootingScales() && lootingLevel > 0) {
                    amount += rng.nextInt(lootingLevel + 1);
                }
                if (amount > 0) {
                    ItemStack result = new ItemStack(entry.item(), amount);
                    placeInOutput(result);
                    if (!result.isEmpty()) producedAny = true;
                }
            }
        }

        if (rarity) {
            for (MobCardType.RareEntry rare : card.rareDrops()) {
                if (rng.nextFloat() < rare.chance()) {
                    placeInOutput(new ItemStack(rare.item(), 1));
                    producedAny = true;
                }
            }
        }

        lastOperationSucceeded = producedAny;
        setChanged();
    }

    private void placeInOutput(ItemStack stack) {
        ItemResource resource = ItemResource.of(stack);
        int toPlace = stack.getCount();
        for (int i = OUTPUT_START; i < OUTPUT_START + OUTPUT_COUNT && toPlace > 0; i++) {
            ItemResource slotRes = itemHandler.getResource(i);
            int slotAmt = itemHandler.getAmountAsInt(i);
            if (slotRes.isEmpty()) {
                int place = Math.min(toPlace, stack.getMaxStackSize());
                itemHandler.set(i, resource, place);
                toPlace -= place;
            } else if (slotRes.equals(resource) && slotAmt < stack.getMaxStackSize()) {
                int add = Math.min(stack.getMaxStackSize() - slotAmt, toPlace);
                itemHandler.set(i, resource, slotAmt + add);
                toPlace -= add;
            }
        }
    }

    public void openMenu(ServerPlayer player) {
        player.openMenu(getMenuProvider(), worldPosition);
    }

    public MenuProvider getMenuProvider() {
        return new SimpleMenuProvider(
                (id, inv, p) -> new LootrMenu(id, inv, itemHandler, data),
                Component.translatable("container.avoidminer.avoid_lootr")
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
        output.putBoolean("LastOpSucceeded", lastOperationSucceeded);
        output.putInt("SlotCount", TOTAL_SLOTS);
        itemHandler.serialize(output.child("Inventory"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyBuffer = input.getIntOr("Energy", 0);
        progress = input.getIntOr("Progress", 0);
        energyFraction = input.getFloatOr("EnergyFraction", 0f);
        lastOperationSucceeded = input.getBooleanOr("LastOpSucceeded", false);
        int oldCount = input.getIntOr("SlotCount", 0);
        if (oldCount == 0) oldCount = TOTAL_SLOTS;
        ItemStacksResourceHandler tmp = new ItemStacksResourceHandler(oldCount);
        tmp.deserialize(input.childOrEmpty("Inventory"));
        int slots = Math.min(tmp.size(), TOTAL_SLOTS);
        for (int i = 0; i < slots; i++) {
            ItemResource r = tmp.getResource(i);
            int a = tmp.getAmountAsInt(i);
            if (!r.isEmpty() && a > 0) itemHandler.set(i, r, a);
        }
    }
}
