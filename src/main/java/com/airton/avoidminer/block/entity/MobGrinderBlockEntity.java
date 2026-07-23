package com.airton.avoidminer.block.entity;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.block.MobGrinderBlock;
import com.airton.avoidminer.energy.EnergyReceiver;
import com.airton.avoidminer.item.AttractionCardItem;
import com.airton.avoidminer.item.DamageCardItem;
import com.airton.avoidminer.item.EnergyLinkItem;
import com.airton.avoidminer.item.MobGrinderRangeCardItem;
import com.airton.avoidminer.item.RangeCardItem;
import com.airton.avoidminer.menu.MobGrinderMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class MobGrinderBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity
        implements EnergyReceiver, IEnergyStorage {

    public static final int RANGE_SLOT = 0;
    public static final int DAMAGE_SLOT = 1;
    public static final int LOOT_SLOT = 2;
    public static final int FUEL_SLOT = 3;
    public static final int ATTRACTION_SLOT = 4;
    public static final int DAMAGE_CARD_SLOT = 5;
    public static final int TOTAL_SLOTS = 6;

    public static final int DATA_RANGE = 0;
    public static final int DATA_DAMAGE = 1;
    public static final int DATA_OVERLAY = 2;
    public static final int DATA_ENERGY = 3;
    public static final int DATA_ENERGY_CAP = 4;
    public static final int DATA_POS_X = 5;
    public static final int DATA_POS_Y = 6;
    public static final int DATA_POS_Z = 7;
    public static final int DATA_PACIFIST = 8;
    public static final int DATA_SIZE = 9;

    private static final int ENERGY_CAPACITY = 20000;
    private static final int ENERGY_PER_EXECUTION = 20;

    private int energyBuffer;
    private int tickCounter;
    private boolean overlayEnabled;
    private boolean pacifistMode;
    private ServerPlayer fakePlayer;

    private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(TOTAL_SLOTS) {
        @Override protected void onContentsChanged(int slot, ItemStack prev) { setChanged(); }
        @Override
        public boolean isValid(int slot, ItemResource resource) {
            if (resource.isEmpty()) return false;
            return switch (slot) {
                case RANGE_SLOT -> resource.getItem() instanceof MobGrinderRangeCardItem;
                case DAMAGE_SLOT -> resource.toStack(1).is(ItemTags.SWORDS)
                        || resource.toStack(1).is(ItemTags.AXES);
                case DAMAGE_CARD_SLOT -> resource.getItem() instanceof DamageCardItem;
                case LOOT_SLOT -> resource.getItem() == ModItems.LOOT_UPGRADE.get()
                        || resource.getItem() == ModItems.RARITY_UPGRADE.get();
                case FUEL_SLOT -> (level != null && resource.toStack(1).getBurnTime(RecipeType.SMELTING, level.fuelValues()) > 0)
                        || resource.getItem() == ModItems.ENERGY_LINK.get();
                case ATTRACTION_SLOT -> resource.getItem() instanceof AttractionCardItem;
                default -> false;
            };
        }
    };

    private final ContainerData data = new net.minecraft.world.inventory.SimpleContainerData(DATA_SIZE) {
        @Override public int get(int i) { return switch (i) {
            case DATA_RANGE -> hasRange() ? getRangeDisplay() : 0;
            case DATA_DAMAGE -> getDamageCardBonus();
            case DATA_OVERLAY -> overlayEnabled ? 1 : 0;
            case DATA_ENERGY -> energyBuffer;
            case DATA_ENERGY_CAP -> ENERGY_CAPACITY;
            case DATA_POS_X -> worldPosition.getX();
            case DATA_POS_Y -> worldPosition.getY();
            case DATA_POS_Z -> worldPosition.getZ();
            case DATA_PACIFIST -> pacifistMode ? 1 : 0;
            default -> 0;
        };}
    };

    public MobGrinderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOB_GRINDER.get(), pos, state);
    }

    public ResourceHandler<ItemResource> getItemHandler() { return itemHandler; }
    public ContainerData getContainerData() { return data; }
    public boolean isOverlayEnabled() { return overlayEnabled; }

    public boolean hasRange() {
        ItemResource r = itemHandler.getResource(RANGE_SLOT);
        return !r.isEmpty() && r.getItem() instanceof MobGrinderRangeCardItem;
    }

    public int getRangeRadius() {
        ItemResource r = itemHandler.getResource(RANGE_SLOT);
        if (r.isEmpty() || !(r.getItem() instanceof MobGrinderRangeCardItem card)) return 0;
        return card.getRange();
    }

    public int getRangeDisplay() {
        ItemResource r = itemHandler.getResource(RANGE_SLOT);
        if (r.isEmpty() || !(r.getItem() instanceof MobGrinderRangeCardItem card)) return 0;
        int size = card.getRange() * 2 + 1;
        return size;
    }

    public int getDamageCardBonus() {
        ItemResource r = itemHandler.getResource(DAMAGE_CARD_SLOT);
        if (r.isEmpty() || !(r.getItem() instanceof DamageCardItem d)) return 0;
        return d.getTier().damage;
    }

    private ItemStack getWeapon() {
        ItemResource r = itemHandler.getResource(DAMAGE_SLOT);
        if (r.isEmpty()) return ItemStack.EMPTY;
        return r.toStack(1);
    }

    public int getTickInterval() {
        ItemResource r = itemHandler.getResource(DAMAGE_CARD_SLOT);
        if (r.isEmpty()) return 20;
        if (r.getItem() instanceof DamageCardItem d) return d.getTier().tickInterval;
        return 20;
    }

    public void toggleOverlay() { overlayEnabled = !overlayEnabled; setChanged(); }
    public void togglePacifist() { pacifistMode = !pacifistMode; setChanged(); }

    @Override public int getEnergyStored() { return energyBuffer; }
    @Override public int getMaxEnergyStored() { return ENERGY_CAPACITY; }
    @Override public boolean canExtract() { return false; }
    @Override public boolean canReceive() { return true; }
    @Override public int extractEnergy(int max, boolean sim) { return 0; }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int space = ENERGY_CAPACITY - energyBuffer;
        int received = Math.min(maxReceive, space);
        if (!simulate && received > 0) { energyBuffer += received; setChanged(); }
        return received;
    }

    public void openMenu(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new MobGrinderMenu(id, inv, this),
                Component.translatable("container.avoidminer.mob_grinder")));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MobGrinderBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!level.hasNeighborSignal(pos)) { setLit(level, pos, state, false); return; }
        if (!be.hasRange()) { setLit(level, pos, state, false); return; }
        if (be.getDamageCardBonus() <= 0 && be.getWeapon().isEmpty()) { setLit(level, pos, state, false); return; }

        be.handleFuel(level);

        be.attractMobs(level, pos);

        be.tickCounter++;
        int interval = be.getTickInterval();
        if (be.tickCounter < interval) return;
        be.tickCounter = 0;

        if (be.energyBuffer < ENERGY_PER_EXECUTION) { setLit(level, pos, state, false); return; }

        int radius = be.getRangeRadius();
        if (radius <= 0) return;

        AABB area = new AABB(
                pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
                pos.getX() + radius + 1, pos.getY() + radius + 1, pos.getZ() + radius + 1);

        boolean any = false;
        ServerPlayer attacker = be.getFakePlayer(level);
        int cardBonus = be.getDamageCardBonus();
        boolean hasWeapon = !be.getWeapon().isEmpty();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (entity instanceof net.minecraft.world.entity.player.Player) continue;
            if (!be.pacifistMode && !(entity instanceof Monster)) continue;
            var dmgSource = level.damageSources().playerAttack(attacker);
            if (hasWeapon) {
                attacker.setPos(entity.getX(), entity.getY(), entity.getZ());
                attacker.attack(entity);
            }
            if (cardBonus > 0) {
                entity.hurt(dmgSource, cardBonus);
            }
            if (!hasWeapon && cardBonus <= 0) {
                entity.hurt(dmgSource, 1);
            }
            spawnHitParticles(serverLevel, entity);
            any = true;
        }
        if (any) {
            be.energyBuffer -= ENERGY_PER_EXECUTION;
            be.setChanged();
        }
        setLit(level, pos, state, any);
    }

    private static void setLit(Level level, BlockPos pos, BlockState state, boolean lit) {
        if (state.getValue(MobGrinderBlock.LIT) != lit)
            level.setBlock(pos, state.setValue(MobGrinderBlock.LIT, lit), 3);
    }

    private static void spawnHitParticles(ServerLevel level, LivingEntity entity) {
        level.sendParticles(ParticleTypes.CRIT,
                entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                4, 0.3, 0.5, 0.3, 0);
    }

    private ServerPlayer getFakePlayer(Level level) {
        if (fakePlayer == null) {
            fakePlayer = FakePlayerFactory.get((ServerLevel) level,
                    new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "MobGrinder"));
        }
        ItemStack weapon = getWeapon();
        if (!weapon.isEmpty()) {
            if (hasLootUpgrade()) {
                var holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.LOOTING);
                EnchantmentHelper.updateEnchantments(weapon, mut -> {
                    int current = mut.getLevel(holder);
                    mut.set(holder, Math.max(current, 3));
                });
            }
            fakePlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, weapon);
        } else if (hasLootUpgrade()) {
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            var holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.LOOTING);
            EnchantmentHelper.updateEnchantments(sword, mut -> mut.set(holder, 3));
            fakePlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, sword);
        } else {
            fakePlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        return fakePlayer;
    }

    private void attractMobs(Level level, BlockPos pos) {
        int radius = getAttractionRadius();
        if (radius <= 0) return;
        double cx = pos.getX() + 0.5, cy = pos.getY() + 0.5, cz = pos.getZ() + 0.5;
        AABB area = new AABB(
                pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
                pos.getX() + radius + 1, pos.getY() + radius + 1, pos.getZ() + radius + 1);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (entity instanceof net.minecraft.world.entity.player.Player) continue;
            if (!pacifistMode && !(entity instanceof Monster)) continue;
            if (entity.distanceToSqr(cx, cy, cz) < 4) continue;
            if (entity instanceof Mob mob && entity instanceof Monster) {
                mob.getNavigation().moveTo(cx, cy, cz, 1.2);
            } else {
                double dx = cx - entity.getX();
                double dy = cy - entity.getY();
                double dz = cz - entity.getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double speed = 0.08;
                entity.setDeltaMovement(entity.getDeltaMovement().add(
                        dx / dist * speed, dy / dist * speed, dz / dist * speed));
            }
        }
    }

    private int getAttractionRadius() {
        ItemResource r = itemHandler.getResource(ATTRACTION_SLOT);
        if (r.isEmpty() || !(r.getItem() instanceof AttractionCardItem card)) return 0;
        return card.getTier().radius;
    }

    private boolean hasLootUpgrade() {
        ItemResource r = itemHandler.getResource(LOOT_SLOT);
        return !r.isEmpty() && (r.getItem() == ModItems.LOOT_UPGRADE.get()
                || r.getItem() == ModItems.RARITY_UPGRADE.get());
    }

    private void handleFuel(Level level) {
        if (energyBuffer > ENERGY_CAPACITY - 1) return;
        ItemResource fuelRes = itemHandler.getResource(FUEL_SLOT);
        int amt = itemHandler.getAmountAsInt(FUEL_SLOT);
        if (fuelRes.isEmpty() || amt <= 0) return;
        ItemStack fuelStack = fuelRes.toStack(1);
        if (fuelStack.is(ModItems.ENERGY_LINK.get())) {
            int space = ENERGY_CAPACITY - energyBuffer;
            if (space > 0) {
                int pulled = EnergyLinkItem.drawEnergy(level, fuelStack, Math.min(space, EnergyLinkItem.TRANSFER_PER_TICK));
                if (pulled > 0) {
                    energyBuffer += pulled;
                    setChanged();
                }
            }
            return;
        }
        int burnTime = fuelStack.getBurnTime(RecipeType.SMELTING, level.fuelValues());
        if (burnTime > 0) {
            int energy = Math.max(1, burnTime / 5);
            if (energy > 0 && energyBuffer + energy <= ENERGY_CAPACITY) {
                energyBuffer += energy;
                if (fuelStack.is(Items.LAVA_BUCKET)) itemHandler.set(FUEL_SLOT, ItemResource.of(Items.BUCKET), 1);
                else itemHandler.set(FUEL_SLOT, fuelRes, amt - 1);
                setChanged();
            }
        }
    }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("Energy", energyBuffer);
        out.putBoolean("overlay", overlayEnabled);
        out.putBoolean("pacifist", pacifistMode);
        itemHandler.serialize(out.child("Inventory"));
    }

    @Override public void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        energyBuffer = in.getIntOr("Energy", 0);
        overlayEnabled = in.getBooleanOr("overlay", false);
        pacifistMode = in.getBooleanOr("pacifist", false);
        ItemStacksResourceHandler tmp = new ItemStacksResourceHandler(TOTAL_SLOTS);
        tmp.deserialize(in.childOrEmpty("Inventory"));
        for (int i = 0; i < Math.min(tmp.size(), TOTAL_SLOTS); i++) {
            ItemResource r = tmp.getResource(i); int a = tmp.getAmountAsInt(i);
            if (!r.isEmpty() && a > 0) itemHandler.set(i, r, a);
        }
    }
}
