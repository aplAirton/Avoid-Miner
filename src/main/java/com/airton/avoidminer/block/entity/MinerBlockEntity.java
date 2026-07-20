package com.airton.avoidminer.block.entity;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.block.MinerBlock;
import com.airton.avoidminer.item.FilterCardItem;
import com.airton.avoidminer.item.RangeCardItem;
import com.airton.avoidminer.menu.MinerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

public class MinerBlockEntity extends BlockEntity {
    public enum Tier {
        TIER_1(1, 30),
        TIER_2(3, 60),
        TIER_3(3, 30);

        public final int blocksPerTick;
        public final int ticksPerBlock;

        Tier(int blocksPerTick, int ticksPerBlock) {
            this.blocksPerTick = blocksPerTick;
            this.ticksPerBlock = ticksPerBlock;
        }
    }

    public static final int RANGE_SLOT = 0;
    public static final int FILTER_SLOT = 1;
    public static final int UPGRADE_SLOT = 2;
    public static final int TOTAL_SLOTS = 3;

    public static final int STATUS_IDLE = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_NO_REDSTONE = 4;
    public static final int STATUS_NO_RANGE_CARD = 6;
    public static final int STATUS_INVALID_RANGE_CARD = 7;
    public static final int STATUS_NO_CONTAINER = 8;
    public static final int STATUS_CONTAINER_FULL = 9;

    public static final int DATA_STATUS = 0;
    public static final int DATA_MINED = 1;
    public static final int DATA_TOTAL_BLOCKS = 2;
    public static final int DATA_OVERLAY = 3;
    public static final int DATA_POS_X = 4;
    public static final int DATA_POS_Y = 5;
    public static final int DATA_POS_Z = 6;
    public static final int DATA_MIN_X = 7;
    public static final int DATA_MAX_X = 8;
    public static final int DATA_MIN_Y = 9;
    public static final int DATA_MAX_Y = 10;
    public static final int DATA_MIN_Z = 11;
    public static final int DATA_MAX_Z = 12;

    private Tier tier;
    private int tierId;

    private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) {
            initialized = false;
            finished = false;
            breakAccumulator = 0;
            if (slot == FILTER_SLOT) {
                totalBlocks = 0;
                totalMined = 0;
            }
            setChanged();
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            if (resource.isEmpty()) return false;
            return switch (slot) {
                case RANGE_SLOT -> resource.getItem() instanceof RangeCardItem;
                case FILTER_SLOT -> resource.getItem() instanceof FilterCardItem;
                case UPGRADE_SLOT -> isValidUpgrade(resource.getItem());
                default -> false;
            };
        }
    };

    private int totalMined;
    private int totalBlocks;
    private int minX, maxX, minY, maxY, minZ, maxZ;
    private int scanY;
    private int scanX, scanZ;
    private boolean initialized;
    private int breakAccumulator;
    private boolean finished;
    private int status;
    private boolean overlayEnabled;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_STATUS -> status;
                case DATA_MINED -> totalMined;
                case DATA_TOTAL_BLOCKS -> totalBlocks;
                case DATA_OVERLAY -> overlayEnabled ? 1 : 0;
                case DATA_POS_X -> worldPosition.getX();
                case DATA_POS_Y -> worldPosition.getY();
                case DATA_POS_Z -> worldPosition.getZ();
                case DATA_MIN_X -> minX;
                case DATA_MAX_X -> maxX;
                case DATA_MIN_Y -> minY;
                case DATA_MAX_Y -> maxY;
                case DATA_MIN_Z -> minZ;
                case DATA_MAX_Z -> maxZ;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() { return 16; }
    };

    public MinerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MINER.get(), pos, state);
        this.tier = ((MinerBlock) state.getBlock()).getTier();
        this.tierId = tier.ordinal();
        this.status = STATUS_IDLE;
    }

    public Tier getTier() { return tier; }
    public ResourceHandler<ItemResource> getItemHandler() { return itemHandler; }
    public ContainerData getContainerData() { return data; }
    public int getTotalMined() { return totalMined; }
    public boolean isFinished() { return finished; }
    public int getStatus() { return status; }
    public int getTierOrdinal() { return tier.ordinal(); }
    public boolean isOverlayEnabled() { return overlayEnabled; }
    public boolean hasRange() {
        ItemResource r = itemHandler.getResource(RANGE_SLOT);
        if (r.isEmpty()) return false;
        return RangeCardItem.hasCompleteData(r.toStack(1));
    }
    public int getMinX() { return minX; }
    public int getMaxX() { return maxX; }
    public int getMinZ() { return minZ; }
    public int getMaxZ() { return maxZ; }
    public int getMinY() { return minY; }
    public int getMaxY() { return maxY; }
    public int getTotalBlocks() { return totalBlocks; }

    public void upgradeTier(int targetOrdinal) {
        Tier newTier = switch (targetOrdinal) {
            case 1 -> Tier.TIER_2;
            case 2 -> Tier.TIER_3;
            default -> Tier.TIER_1;
        };
        this.tier = newTier;
        this.tierId = targetOrdinal;
        initialized = false;
        finished = false;
        scanY = 0; scanX = 0; scanZ = 0;
        BlockState current = getBlockState();
        if (current.hasProperty(MinerBlock.TIER)) {
            level.setBlock(worldPosition, current.setValue(MinerBlock.TIER, targetOrdinal + 1), 3);
        }
        setChanged();
    }

    public void toggleOverlay() {
        overlayEnabled = !overlayEnabled;
        setChanged();
    }

    public void resetMining() {
        initialized = false;
        finished = false;
        breakAccumulator = 0;
        scanY = 0;
        scanX = 0;
        scanZ = 0;
        totalMined = 0;
        totalBlocks = 0;
        setChanged();
    }

    private int getProgressPercent() {
        if (!initialized || totalBlocks <= 0) return 0;
        long done = (long) (scanY - minY) * (maxX - minX + 1) * (maxZ - minZ + 1)
                + (long) (scanX - minX) * (maxZ - minZ + 1) + (scanZ - minZ);
        return (int) (done * 100 / totalBlocks);
    }

    public void openMenu(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new MinerMenu(id, inv, this),
                Component.translatable("container.avoidminer.miner")));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MinerBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        ItemResource rangeResource = be.itemHandler.getResource(RANGE_SLOT);
        if (rangeResource.isEmpty()) {
            be.setStatus(STATUS_NO_RANGE_CARD, state);
            return;
        }
        ItemStack rangeStack = rangeResource.toStack(1);
        if (!RangeCardItem.hasCompleteData(rangeStack)) {
            be.setStatus(STATUS_INVALID_RANGE_CARD, state);
            return;
        }

        if (!be.initialized) {
            be.initBounds(pos);
        }

        if (be.finished) {
            be.setStatus(STATUS_FINISHED, state);
            return;
        }

        boolean hasRedstone = level.hasNeighborSignal(pos);
        if (!hasRedstone) {
            be.setStatus(STATUS_NO_REDSTONE, state);
            return;
        }

        if (!be.hasAdjacentContainer(level, pos, state)) {
            be.setStatus(STATUS_NO_CONTAINER, state);
            return;
        }

        if (!be.hasContainerSpace(level, pos, state)) {
            be.setStatus(STATUS_CONTAINER_FULL, state);
            return;
        }

        be.breakAccumulator += be.tier.blocksPerTick;

        while (be.breakAccumulator >= be.tier.ticksPerBlock) {
            be.breakAccumulator -= be.tier.ticksPerBlock;
            be.setStatus(STATUS_RUNNING, state);

            if (!be.mineOneBlock(serverLevel, pos, state)) {
                be.finished = true;
                be.setStatus(STATUS_FINISHED, state);
                return;
            }

            if (!be.hasContainerSpace(level, pos, state)) {
                be.setStatus(STATUS_CONTAINER_FULL, state);
                return;
            }
        }
    }

    private void setStatus(int newStatus, BlockState state) {
        if (this.status != newStatus) {
            this.status = newStatus;
            setChanged();
        }
        boolean lit = newStatus == STATUS_RUNNING;
        if (state.getValue(MinerBlock.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(MinerBlock.LIT, lit), 3);
        }
    }

    private void initBounds(BlockPos pos) {
        ItemResource rangeResource = itemHandler.getResource(RANGE_SLOT);
        ItemStack rangeStack = rangeResource.toStack(1);
        int x1 = RangeCardItem.getX1(rangeStack);
        int z1 = RangeCardItem.getZ1(rangeStack);
        int x2 = RangeCardItem.getX2(rangeStack);
        int z2 = RangeCardItem.getZ2(rangeStack);
        int y1 = RangeCardItem.getY1(rangeStack);
        int y2 = RangeCardItem.getY2(rangeStack);
        minX = Math.min(x1, x2);
        maxX = Math.max(x1, x2);
        minZ = Math.min(z1, z2);
        maxZ = Math.max(z1, z2);
        minY = Math.min(y1, y2);
        maxY = Math.max(y1, y2);
        scanY = maxY;
        scanX = minX;
        scanZ = minZ;

        int mx = pos.getX();
        int mz = pos.getZ();

        totalBlocks = 0;
        if (level != null) {
            for (int y = maxY; y >= minY; y--) {
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        if (Math.abs(x - mx) <= 1 && Math.abs(z - mz) <= 1) continue;
                        BlockPos target = new BlockPos(x, y, z);
                        BlockState bs = level.getBlockState(target);
                        if (isMineable(bs, target)) {
                            totalBlocks++;
                        }
                    }
                }
            }
        }

        if (totalBlocks <= 0) totalBlocks = 1;
        initialized = true;
    }

    private boolean isMineable(BlockState state, BlockPos pos) {
        if (state.isAir()) return false;
        if (state.getBlock().equals(Blocks.BEDROCK)) return false;
        if (state.getDestroySpeed(level, pos) < 0) return false;
        if (state.liquid()) return false;
        return passesFilter(state);
    }

    private boolean passesFilter(BlockState state) {
        List<Identifier> filter = getFilterEntries();
        if (filter.isEmpty()) return true;
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return filter.contains(id);
    }

    private List<Identifier> getFilterEntries() {
        ItemResource filterResource = itemHandler.getResource(FILTER_SLOT);
        if (filterResource.isEmpty()) return List.of();
        ItemStack filterStack = filterResource.toStack(1);
        return FilterCardItem.getEntries(filterStack);
    }

    public boolean removeFilterEntry(int index) {
        ItemResource filterResource = itemHandler.getResource(FILTER_SLOT);
        if (filterResource.isEmpty()) return false;
        ItemStack filterStack = filterResource.toStack(1);
        int before = FilterCardItem.getEntries(filterStack).size();
        FilterCardItem.removeEntry(filterStack, index);
        int after = FilterCardItem.getEntries(filterStack).size();
        if (before == after) return false;
        itemHandler.set(FILTER_SLOT, ItemResource.of(filterStack), filterStack.getCount());
        initialized = false;
        finished = false;
        breakAccumulator = 0;
        setChanged();
        return true;
    }

    public boolean clearFilter() {
        ItemResource filterResource = itemHandler.getResource(FILTER_SLOT);
        if (filterResource.isEmpty()) return false;
        ItemStack filterStack = filterResource.toStack(1);
        if (FilterCardItem.getEntries(filterStack).isEmpty()) return false;
        FilterCardItem.clearEntries(filterStack);
        itemHandler.set(FILTER_SLOT, ItemResource.of(filterStack), filterStack.getCount());
        initialized = false;
        finished = false;
        breakAccumulator = 0;
        setChanged();
        return true;
    }

    private boolean mineOneBlock(ServerLevel level, BlockPos minerPos, BlockState minerState) {
        int mx = minerPos.getX();
        int mz = minerPos.getZ();

        while (scanY >= minY) {
            while (scanX <= maxX) {
                while (scanZ <= maxZ) {
                    BlockPos target = new BlockPos(scanX, scanY, scanZ);
                    BlockState targetState = level.getBlockState(target);

                    boolean inProtectedZone = Math.abs(scanX - mx) <= 1 && Math.abs(scanZ - mz) <= 1;

                    scanZ++;

                    if (inProtectedZone) continue;

                    boolean mineable = !targetState.isAir()
                            && !targetState.getBlock().equals(Blocks.BEDROCK)
                            && targetState.getDestroySpeed(level, target) >= 0
                            && passesFilter(targetState);

                    if (mineable) {
                        List<ItemStack> drops = getDropsWithUpgrade(level, target, targetState);
                        level.destroyBlock(target, false);
                        for (ItemStack drop : drops) {
                            outputToAdjacentContainers(level, minerPos, minerState, drop);
                        }
                        totalMined++;
                        setChanged();
                        return true;
                    }
                }
                scanX++;
                scanZ = minZ;
            }
            scanY--;
            scanX = minX;
            scanZ = minZ;
        }
        return false;
    }

    private List<ItemStack> getDropsWithUpgrade(ServerLevel level, BlockPos pos, BlockState state) {
        List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos));

        if (hasUpgrade(ModItems.SILK_UPGRADE.get())) {
            net.minecraft.world.item.Item blockItem = state.getBlock().asItem();
            if (blockItem != net.minecraft.world.item.Items.AIR) {
                drops = List.of(new ItemStack(blockItem));
            }
        }

        if (hasUpgrade(ModItems.FORTUNE_UPGRADE.get())) {
            List<ItemStack> multiplied = new java.util.ArrayList<>();
            net.minecraft.util.RandomSource random = level.getRandom();
            for (ItemStack drop : drops) {
                multiplied.add(drop);
                net.minecraft.world.item.Item item = drop.getItem();
                if (item == net.minecraft.world.item.Items.RAW_IRON
                        || item == net.minecraft.world.item.Items.RAW_COPPER
                        || item == net.minecraft.world.item.Items.RAW_GOLD
                        || item == net.minecraft.world.item.Items.REDSTONE
                        || item == net.minecraft.world.item.Items.LAPIS_LAZULI
                        || item == net.minecraft.world.item.Items.DIAMOND
                        || item == net.minecraft.world.item.Items.EMERALD
                        || item == net.minecraft.world.item.Items.COAL
                        || item == net.minecraft.world.item.Items.QUARTZ
                        || item == net.minecraft.world.item.Items.NETHERITE_SCRAP
                        || item == net.minecraft.world.item.Items.GLOWSTONE_DUST) {
                    int bonus = random.nextInt(4);
                    if (bonus > 0) {
                        multiplied.add(new ItemStack(item, bonus));
                    }
                }
            }
            drops = multiplied;
        }

        return drops;
    }

    private boolean isValidUpgrade(net.minecraft.world.item.Item item) {
        return item == ModItems.FORTUNE_UPGRADE.get() || item == ModItems.SILK_UPGRADE.get();
    }

    private boolean hasUpgrade(net.minecraft.world.item.Item upgrade) {
        ItemResource res = itemHandler.getResource(UPGRADE_SLOT);
        return !res.isEmpty() && res.getItem() == upgrade;
    }

    private boolean hasAdjacentContainer(Level level, BlockPos pos, BlockState state) {
        Direction front = state.getValue(MinerBlock.FACING);
        if (isContainerAt(level, pos.above())) return true;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (dir == front) continue;
            if (isContainerAt(level, pos.relative(dir))) return true;
        }
        return false;
    }

    private boolean hasContainerSpace(Level level, BlockPos pos, BlockState state) {
        Direction front = state.getValue(MinerBlock.FACING);
        Direction[] sides = {Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (Direction dir : sides) {
            if (dir == front) continue;
            BlockPos target = pos.relative(dir);
            if (hasSpaceAt(level, target)) return true;
        }
        return false;
    }

    private void outputToAdjacentContainers(ServerLevel level, BlockPos pos, BlockState state, ItemStack stack) {
        if (stack.isEmpty()) return;
        int remaining = stack.getCount();
        Direction front = state.getValue(MinerBlock.FACING);

        Direction[] sides = {Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (Direction dir : sides) {
            if (dir == front) continue;
            BlockPos target = pos.relative(dir);
            int before = remaining;
            remaining = outputToAt(level, target, new ItemStack(stack.getItem(), remaining));
            if (remaining <= 0) return;
        }

        if (remaining > 0) {
            Block.popResource(level, pos.above(), new ItemStack(stack.getItem(), remaining));
        }
    }

    private static boolean isContainerAt(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return false;
        if (be instanceof net.minecraft.world.Container) return true;
        return Capabilities.Item.BLOCK.getCapability(level, pos, null, be, null) != null;
    }

    private static boolean hasSpaceAt(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return false;

        if (be instanceof net.minecraft.world.Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack slot = container.getItem(i);
                if (slot.isEmpty() || slot.getCount() < slot.getMaxStackSize()) return true;
            }
        }

        ResourceHandler<ItemResource> handler = Capabilities.Item.BLOCK.getCapability(level, pos, null, be, null);
        if (handler instanceof ItemStacksResourceHandler stacks) {
            for (int i = 0; i < stacks.size(); i++) {
                ItemResource res = stacks.getResource(i);
                int count = stacks.getAmountAsInt(i);
                if (res.isEmpty() || count < res.getItem().getDefaultMaxStackSize()) return true;
            }
        }

        return false;
    }

    private static int outputToAt(Level level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) return 0;
        int remaining = stack.getCount();

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof net.minecraft.world.Container container) {
            ItemStack result = insertIntoContainer(container, new ItemStack(stack.getItem(), remaining));
            remaining = result.getCount();
        }
        if (remaining <= 0) return 0;

        if (be != null) {
            ResourceHandler<ItemResource> handler = Capabilities.Item.BLOCK.getCapability(level, pos, null, be, null);
            if (handler != null) {
                int slots = handler instanceof ItemStacksResourceHandler h ? h.size() : 0;
                for (int slot = 0; slot < slots && remaining > 0; slot++) {
                    try (Transaction tx = Transaction.openRoot()) {
                        int inserted = handler.insert(slot,
                                ItemResource.of(new ItemStack(stack.getItem(), remaining)), remaining, tx);
                        if (inserted > 0) {
                            tx.commit();
                            remaining -= inserted;
                        }
                    }
                }
            }
        }

        return remaining;
    }

    private static ItemStack insertIntoContainer(net.minecraft.world.Container container, ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack remaining = stack.copy();
        for (int i = 0; i < container.getContainerSize() && !remaining.isEmpty(); i++) {
            ItemStack slot = container.getItem(i);
            if (slot.isEmpty()) {
                container.setItem(i, remaining.copy());
                container.setChanged();
                return ItemStack.EMPTY;
            }
            if (ItemStack.isSameItemSameComponents(slot, remaining)) {
                int space = slot.getMaxStackSize() - slot.getCount();
                int transfer = Math.min(space, remaining.getCount());
                if (transfer > 0) {
                    slot.grow(transfer);
                    remaining.shrink(transfer);
                    container.setChanged();
                }
            }
        }
        return remaining;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("tierId", tierId);
        output.putInt("totalMined", totalMined);
        output.putInt("totalBlocks", totalBlocks);
        output.putInt("minX", minX);
        output.putInt("maxX", maxX);
        output.putInt("minY", minY);
        output.putInt("maxY", maxY);
        output.putInt("minZ", minZ);
        output.putInt("maxZ", maxZ);
        output.putInt("scanY", scanY);
        output.putInt("scanX", scanX);
        output.putInt("scanZ", scanZ);
        output.putBoolean("initialized", initialized);
        output.putBoolean("finished", finished);
        output.putBoolean("overlayEnabled", overlayEnabled);
        output.putInt("status", status);
        output.putInt("breakAccumulator", breakAccumulator);
        itemHandler.serialize(output.child("Inventory"));
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tierId = input.getIntOr("tierId", 0);
        totalMined = input.getIntOr("totalMined", 0);
        totalBlocks = input.getIntOr("totalBlocks", 0);
        minX = input.getIntOr("minX", 0);
        maxX = input.getIntOr("maxX", 0);
        minY = input.getIntOr("minY", 0);
        maxY = input.getIntOr("maxY", 0);
        minZ = input.getIntOr("minZ", 0);
        maxZ = input.getIntOr("maxZ", 0);
        scanY = input.getIntOr("scanY", 0);
        scanX = input.getIntOr("scanX", 0);
        scanZ = input.getIntOr("scanZ", 0);
        initialized = input.getBooleanOr("initialized", false);
        finished = input.getBooleanOr("finished", false);
        overlayEnabled = input.getBooleanOr("overlayEnabled", false);
        status = input.getIntOr("status", STATUS_IDLE);
        breakAccumulator = input.getIntOr("breakAccumulator", 0);
        ItemStacksResourceHandler tmp = new ItemStacksResourceHandler(TOTAL_SLOTS);
        tmp.deserialize(input.childOrEmpty("Inventory"));
        for (int i = 0; i < Math.min(tmp.size(), TOTAL_SLOTS); i++) {
            ItemResource r = tmp.getResource(i);
            int a = tmp.getAmountAsInt(i);
            if (!r.isEmpty() && a > 0) {
                itemHandler.set(i, r, a);
            }
        }
    }
}
