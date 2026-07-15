package com.airton.avoidminer.block.entity;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.menu.MagnetiteBarrelMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * Barril de Magnetita — aspira instantaneamente itens num raio de 8 blocos
 * para seus 81 slots e, com a Melhoria de Absorção de Experiência instalada,
 * também absorve orbes de XP, guardando os pontos internamente até o jogador
 * resgatá-los pelos botões da interface. A Melhoria de Empilhamento faz cada
 * slot comportar 4x o tamanho do pacote (4 não-empilháveis, 256 empilháveis).
 */
public class MagnetiteBarrelBlockEntity extends BlockEntity {
    public static final int STORAGE_START = 0;
    public static final int STORAGE_COUNT = 81;
    public static final int UPG_XP_SLOT = 81;
    public static final int UPG_STACK_SLOT = 82;
    public static final int UPG_ITEM_SLOT = 83;
    public static final int TOTAL_SLOTS = 84;

    public static final double ABSORB_RADIUS = 8.0;
    public static final int STACK_MULTIPLIER = 4;

    // XP em pontos excede 16 bits, então sincroniza em duas metades
    public static final int DATA_XP_LO = 0;
    public static final int DATA_XP_HI = 1;
    public static final int DATA_SLOTS_USED = 2;
    public static final int DATA_HAS_XP_UPG = 3;
    public static final int DATA_HAS_STACK_UPG = 4;
    public static final int DATA_HAS_ITEM_UPG = 5;
    public static final int DATA_SIZE = 6;

    private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) { setChanged(); }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            if (resource.isEmpty()) return false;
            ItemStack stack = resource.toStack(1);
            if (slot == UPG_XP_SLOT) return stack.is(ModItems.XP_ABSORB_UPGRADE.get());
            if (slot == UPG_STACK_SLOT) return stack.is(ModItems.ITEM_STACKING_UPGRADE.get());
            if (slot == UPG_ITEM_SLOT) return stack.is(ModItems.ITEM_ABSORB_UPGRADE.get());
            return slot >= STORAGE_START && slot < STORAGE_START + STORAGE_COUNT;
        }

        @Override
        public long getCapacityAsLong(int slot, ItemResource resource) {
            if (slot >= UPG_XP_SLOT) return 1;
            long base = super.getCapacityAsLong(slot, resource);
            return hasStackUpgrade() ? base * STACK_MULTIPLIER : base;
        }
    };

    private final ResourceHandler<ItemResource> sideHandler = new ResourceHandler<>() {
        @Override public int size() { return itemHandler.size(); }
        @Override public ItemResource getResource(int index) { return itemHandler.getResource(index); }
        @Override public long getAmountAsLong(int index) { return itemHandler.getAmountAsLong(index); }
        @Override public long getCapacityAsLong(int index, ItemResource resource) { return itemHandler.getCapacityAsLong(index, resource); }
        @Override public boolean isValid(int index, ItemResource resource) { return itemHandler.isValid(index, resource); }
        @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) { return 0; }
        @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index >= STORAGE_START && index < STORAGE_START + STORAGE_COUNT) {
                return itemHandler.extract(index, resource, amount, transaction);
            }
            return 0;
        }
    };

    private int storedXp;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case DATA_XP_LO -> storedXp & 0xFFFF;
                case DATA_XP_HI -> storedXp >>> 16;
                case DATA_SLOTS_USED -> countUsedSlots();
                case DATA_HAS_XP_UPG -> hasXpUpgrade() ? 1 : 0;
                case DATA_HAS_STACK_UPG -> hasStackUpgrade() ? 1 : 0;
                case DATA_HAS_ITEM_UPG -> hasItemUpgrade() ? 1 : 0;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case DATA_XP_LO -> storedXp = (storedXp & ~0xFFFF) | (value & 0xFFFF);
                case DATA_XP_HI -> storedXp = (storedXp & 0xFFFF) | (value << 16);
            }
        }
        @Override public int getCount() { return DATA_SIZE; }
    };

    public MagnetiteBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGNETITE_BARREL.get(), pos, state);
    }

    public boolean hasXpUpgrade() {
        ItemResource r = itemHandler.getResource(UPG_XP_SLOT);
        return !r.isEmpty() && r.toStack(1).is(ModItems.XP_ABSORB_UPGRADE.get());
    }

    public boolean hasStackUpgrade() {
        ItemResource r = itemHandler.getResource(UPG_STACK_SLOT);
        return !r.isEmpty() && r.toStack(1).is(ModItems.ITEM_STACKING_UPGRADE.get());
    }

    public boolean hasItemUpgrade() {
        ItemResource r = itemHandler.getResource(UPG_ITEM_SLOT);
        return !r.isEmpty() && r.toStack(1).is(ModItems.ITEM_ABSORB_UPGRADE.get());
    }

    public int getStoredXp() { return storedXp; }

    public void setStoredXp(int value) {
        storedXp = Math.max(0, value);
        setChanged();
    }

    private int countUsedSlots() {
        int used = 0;
        for (int i = STORAGE_START; i < STORAGE_START + STORAGE_COUNT; i++) {
            if (!itemHandler.getResource(i).isEmpty() && itemHandler.getAmountAsInt(i) > 0) used++;
        }
        return used;
    }

    /** Resgate de XP pelos botões da GUI: {@code maxPoints < 0} transfere tudo. */
    public void payOutXp(ServerPlayer player, int maxPoints) {
        int points = maxPoints < 0 ? storedXp : Math.min(maxPoints, storedXp);
        if (points <= 0) return;
        storedXp -= points;
        player.giveExperiencePoints(points);
        setChanged();
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.BLOCKS, 0.9f, 1.0f);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MagnetiteBarrelBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        AABB area = new AABB(pos).inflate(ABSORB_RADIUS);

        // Absorção de itens só com a Melhoria de Absorção de Itens instalada
        if (be.hasItemUpgrade()) {
            for (ItemEntity itemEntity : serverLevel.getEntitiesOfClass(ItemEntity.class, area,
                    entity -> entity.isAlive() && !entity.getItem().isEmpty())) {
                ItemStack remainder = be.absorbStack(itemEntity.getItem());
                if (remainder.isEmpty()) {
                    itemEntity.discard();
                } else if (remainder.getCount() != itemEntity.getItem().getCount()) {
                    itemEntity.setItem(remainder);
                }
            }
        }

        if (be.hasXpUpgrade() && be.storedXp < Integer.MAX_VALUE - 32768) {
            for (ExperienceOrb orb : serverLevel.getEntitiesOfClass(ExperienceOrb.class, area,
                    entity -> entity.isAlive())) {
                be.storedXp += orb.getValue();
                serverLevel.sendParticles(ParticleTypes.GLOW, true, false,
                        orb.getX(), orb.getY() + 0.2, orb.getZ(), 2, 0.1, 0.1, 0.1, 0.02);
                orb.discard();
                be.setChanged();
            }
        }
    }

    /** Tenta guardar o stack; retorna o que não coube. */
    private ItemStack absorbStack(ItemStack stack) {
        ItemResource resource = ItemResource.of(stack);
        int remaining = stack.getCount();
        try (Transaction tx = Transaction.openRoot()) {
            for (int slot = STORAGE_START; slot < STORAGE_START + STORAGE_COUNT && remaining > 0; slot++) {
                remaining -= itemHandler.insert(slot, resource, remaining, tx);
            }
            tx.commit();
        }
        if (remaining <= 0) return ItemStack.EMPTY;
        ItemStack leftover = stack.copy();
        leftover.setCount(remaining);
        return leftover;
    }

    public void openMenu(ServerPlayer player) {
        player.openMenu(getMenuProvider(), worldPosition);
    }

    public MenuProvider getMenuProvider() {
        return new SimpleMenuProvider(
                (id, inv, p) -> new MagnetiteBarrelMenu(id, inv, itemHandler, data, this),
                Component.translatable("container.avoidminer.magnetite_barrel")
        );
    }

    public ResourceHandler<ItemResource> getItemHandler() { return itemHandler; }

    /**
     * Visão restrita aos slots de melhoria — usada pelo UpgradeApplier
     * (agachado + clique direito), para a melhoria não cair no armazenamento,
     * que aceita qualquer item.
     */
    public ResourceHandler<ItemResource> getUpgradeOnlyHandler() {
        return new ResourceHandler<>() {
            private boolean upgradeSlot(int index) { return index >= UPG_XP_SLOT && index <= UPG_ITEM_SLOT; }
            @Override public int size() { return itemHandler.size(); }
            @Override public ItemResource getResource(int index) { return itemHandler.getResource(index); }
            @Override public long getAmountAsLong(int index) { return itemHandler.getAmountAsLong(index); }
            @Override public long getCapacityAsLong(int index, ItemResource resource) { return itemHandler.getCapacityAsLong(index, resource); }
            @Override public boolean isValid(int index, ItemResource resource) {
                return upgradeSlot(index) && itemHandler.isValid(index, resource);
            }
            @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
                return upgradeSlot(index) ? itemHandler.insert(index, resource, amount, transaction) : 0;
            }
            @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) { return 0; }
        };
    }

    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        return side == null ? itemHandler : sideHandler;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("StoredXp", storedXp);
        output.putInt("SlotCount", TOTAL_SLOTS);
        itemHandler.serialize(output.child("Inventory"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedXp = input.getIntOr("StoredXp", 0);
        int oldCount = input.getIntOr("SlotCount", TOTAL_SLOTS);
        ItemStacksResourceHandler tmp = new ItemStacksResourceHandler(Math.max(oldCount, TOTAL_SLOTS));
        tmp.deserialize(input.childOrEmpty("Inventory"));
        int slots = Math.min(tmp.size(), TOTAL_SLOTS);
        for (int i = 0; i < slots; i++) {
            ItemResource r = tmp.getResource(i);
            int a = tmp.getAmountAsInt(i);
            if (!r.isEmpty() && a > 0) itemHandler.set(i, r, a);
        }
    }
}
