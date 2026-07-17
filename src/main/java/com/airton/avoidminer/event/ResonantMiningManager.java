package com.airton.avoidminer.event;

import com.airton.avoidminer.AvoidMiner;
import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.ModParticleTypes;
import com.airton.avoidminer.item.ResonantMiningRules;
import com.airton.avoidminer.item.ResonantPickaxeItem;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Unit;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = AvoidMiner.MODID)
public final class ResonantMiningManager {
    public static final ResourceKey<Enchantment> RESONANT_MINER = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(AvoidMiner.MODID, "resonant_miner")
    );
    private static final Map<UUID, MiningWave> ACTIVE_WAVES = new HashMap<>();
    private static final Map<UUID, MiningCharge> ACTIVE_CHARGES = new HashMap<>();

    private ResonantMiningManager() {
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        ItemStack stack = event.getEntity().getItemInHand(event.getHand());
        if (stack.is(ModItems.RESONANT_PICKAXE.get())) {
            return;
        }
        int enchantmentLevel = getEnchantmentLevel(event.getEntity().level(), stack);
        int forwardRange = ResonantMiningRules.forwardRange(false, enchantmentLevel);
        if (forwardRange <= 0) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getEntity() instanceof ServerPlayer player) {
            beginCharge(player);
        }
    }

    public static void handleChargeAction(ServerPlayer player, byte action) {
        if (action == 0) {
            beginCharge(player);
        } else if (action == 1) {
            finishCharge(player, true);
        } else {
            finishCharge(player, false);
        }
    }

    private static void beginCharge(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        int enchantmentLevel = getEnchantmentLevel(player.level(), stack);
        if (stack.is(ModItems.RESONANT_PICKAXE.get()) || enchantmentLevel <= 0
                || player.getCooldowns().isOnCooldown(stack)
                || ACTIVE_WAVES.containsKey(player.getUUID())) {
            return;
        }
        ACTIVE_CHARGES.putIfAbsent(player.getUUID(), new MiningCharge(
                stack.getItem(), enchantmentLevel, player.level().getGameTime()));
    }

    private static void finishCharge(ServerPlayer player, boolean release) {
        MiningCharge charge = ACTIVE_CHARGES.remove(player.getUUID());
        if (!release || charge == null) return;

        ItemStack stack = player.getMainHandItem();
        long heldTicks = player.level().getGameTime() - charge.startedAt();
        if (heldTicks < ResonantMiningRules.CHARGE_TICKS
                || !stack.is(charge.item())
                || getEnchantmentLevel(player.level(), stack) != charge.enchantmentLevel()
                || player.getCooldowns().isOnCooldown(stack)) {
            return;
        }
        if (tryLaunch(player, stack)) {
            player.getCooldowns().addCooldown(stack, ResonantMiningRules.COOLDOWN_TICKS);
        }
    }

    /** Dispara a onda para o jogador se ele ainda não tiver uma ativa. */
    public static boolean tryLaunch(ServerPlayer player, ItemStack stack) {
        boolean resonantPickaxe = stack.is(ModItems.RESONANT_PICKAXE.get());
        int enchantmentLevel = getEnchantmentLevel(player.level(), stack);
        int forwardRange = ResonantMiningRules.forwardRange(resonantPickaxe, enchantmentLevel);
        if (forwardRange <= 0 || ACTIVE_WAVES.containsKey(player.getUUID())) {
            return false;
        }
        launch(player, stack, resonantPickaxe, enchantmentLevel, forwardRange);
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCharges(event.getServer());
        Iterator<Map.Entry<UUID, MiningWave>> iterator = ACTIVE_WAVES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, MiningWave> entry = iterator.next();
            if (!entry.getValue().advance(event.getServer())) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ACTIVE_WAVES.clear();
        ACTIVE_CHARGES.clear();
    }

    private static void launch(ServerPlayer player, ItemStack stack, boolean resonantPickaxe,
                               int enchantmentLevel, int forwardRange) {
        Vec3 direction = player.getLookAngle().normalize();
        Vec3 origin = player.getEyePosition();
        Vec3 right = direction.cross(new Vec3(0.0, 1.0, 0.0));
        if (right.lengthSqr() < 1.0E-6) {
            right = direction.cross(new Vec3(1.0, 0.0, 0.0));
        }
        right = right.normalize();
        Vec3 up = right.cross(direction).normalize();

        ACTIVE_WAVES.put(player.getUUID(), new MiningWave(
                player.level().dimension(), player.getUUID(), stack.getItem(),
                resonantPickaxe, enchantmentLevel, forwardRange, origin, direction, right, up
        ));
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        player.level().playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_CHARGE,
                SoundSource.PLAYERS, 1.5F, 1.25F);
        player.level().gameEvent(GameEvent.INSTRUMENT_PLAY, player.position(), GameEvent.Context.of(player));
    }

    public static int getEnchantmentLevel(Level level, ItemStack stack) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(RESONANT_MINER)
                .map(holder -> stack.getEnchantments().getLevel(holder))
                .orElse(0);
    }

    private static void tickCharges(MinecraftServer server) {
        Iterator<Map.Entry<UUID, MiningCharge>> iterator = ACTIVE_CHARGES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, MiningCharge> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            MiningCharge charge = entry.getValue();
            ItemStack stack = player == null ? ItemStack.EMPTY : player.getMainHandItem();
            if (player == null || stack.isEmpty() || !stack.is(charge.item())
                    || getEnchantmentLevel(player.level(), stack) != charge.enchantmentLevel()
                    || player.getCooldowns().isOnCooldown(stack)) {
                iterator.remove();
                continue;
            }
            int heldTicks = (int) Math.min(Integer.MAX_VALUE,
                    player.level().getGameTime() - charge.startedAt());
            ResonantPickaxeItem.emitChargeFeedback(
                    (ServerLevel) player.level(), player, heldTicks, InteractionHand.MAIN_HAND);
        }
    }

    private static final DustParticleOptions GOLD_RING = new DustParticleOptions(0xFFC838, 1.6f);
    private static final DustParticleOptions GOLD_HAZE = new DustParticleOptions(0xFFE28A, 1.1f);

    private static void emitWaveFront(ServerLevel level, Vec3 center, Vec3 direction) {
        level.sendParticles(ModParticleTypes.GOLDEN_SONIC_BOOM.get(), true, true,
                center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);

        Vec3 side = direction.cross(new Vec3(0.0, 1.0, 0.0));
        if (side.lengthSqr() < 1.0E-6) {
            side = direction.cross(new Vec3(1.0, 0.0, 0.0));
        }
        side = side.normalize();
        Vec3 up = side.cross(direction).normalize();
        double radius = ResonantMiningRules.SIDE_RADIUS + 0.15;
        for (int i = 0; i < 28; i++) {
            double angle = Math.PI * 2.0 * i / 28.0;
            Vec3 point = center
                    .add(side.scale(Math.cos(angle) * radius))
                    .add(up.scale(Math.sin(angle) * radius));
            level.sendParticles(GOLD_RING, true, true,
                    point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.sendParticles(GOLD_HAZE, true, true,
                center.x, center.y, center.z, 10, radius * 0.65, radius * 0.65, radius * 0.65, 0.02);
        level.sendParticles(ParticleTypes.WAX_ON, true, true,
                center.x, center.y, center.z, 8,
                Math.abs(direction.x) + 0.4, Math.abs(direction.y) + 0.4,
                Math.abs(direction.z) + 0.4, 0.02);
    }

    private static final class MiningWave {
        private final ResourceKey<Level> dimension;
        private final UUID playerId;
        private final Item sourceItem;
        private final boolean resonantPickaxe;
        private final int enchantmentLevel;
        private final int forwardRange;
        private final Vec3 origin;
        private final Vec3 direction;
        private final Vec3 right;
        private final Vec3 up;
        private final Set<BlockPos> tunnelPositions = new LinkedHashSet<>();
        private int step;
        private int lavaStabilizationTicks;
        private int blocksTowardDurability;

        private MiningWave(ResourceKey<Level> dimension, UUID playerId, Item sourceItem,
                           boolean resonantPickaxe, int enchantmentLevel, int forwardRange,
                           Vec3 origin, Vec3 direction, Vec3 right, Vec3 up) {
            this.dimension = dimension;
            this.playerId = playerId;
            this.sourceItem = sourceItem;
            this.resonantPickaxe = resonantPickaxe;
            this.enchantmentLevel = enchantmentLevel;
            this.forwardRange = forwardRange;
            this.origin = origin;
            this.direction = direction;
            this.right = right;
            this.up = up;
        }

        private boolean advance(MinecraftServer server) {
            ServerLevel level = server.getLevel(dimension);
            if (level == null) {
                return false;
            }
            if (lavaStabilizationTicks > 0) {
                if (lavaStabilizationTicks % ResonantMiningRules.LAVA_RECHECK_INTERVAL == 0) {
                    stabilizeTunnelLava(level, tunnelPositions);
                }
                lavaStabilizationTicks--;
                return lavaStabilizationTicks > 0;
            }

            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            ItemStack stack = player == null ? ItemStack.EMPTY : player.getMainHandItem();
            if (player == null || player.level() != level || stack.isEmpty()
                    || !stack.is(sourceItem)
                    || getEnchantmentLevel(level, stack) != enchantmentLevel
                    || ResonantMiningRules.forwardRange(resonantPickaxe, enchantmentLevel) != forwardRange) {
                return false;
            }

            step++;
            Vec3 center = origin.add(direction.scale(step));
            emitWaveFront(level, center, direction);
            if (step == 1) {
                level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM,
                        SoundSource.PLAYERS, 2.0F, 1.15F);
            }

            Set<BlockPos> waveFront = planePositions(center, right, up);
            tunnelPositions.addAll(waveFront);
            boolean extinguishedLava = containAdjacentLava(level, tunnelPositions);
            boolean alreadyUnbreakable = stack.has(DataComponents.UNBREAKABLE);
            int brokenBlocks = 0;
            if (!alreadyUnbreakable) stack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
            try {
                for (BlockPos pos : waveFront) {
                    if (stack.isEmpty() || !level.hasChunkAt(pos)) {
                        continue;
                    }
                    FluidState fluidState = level.getFluidState(pos);
                    boolean sourceLava = false;
                    if (fluidState.is(FluidTags.LAVA)) {
                        extinguishedLava = true;
                        if (fluidState.isSource()) {
                            level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
                            sourceLava = true;
                        } else {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                            continue;
                        }
                    }
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || !state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                            || !stack.isCorrectToolForDrops(state)) {
                        if (sourceLava) {
                            level.destroyBlock(pos, false);
                        }
                        continue;
                    }
                    if (player.gameMode.destroyBlock(pos)) brokenBlocks++;
                }
            } finally {
                if (!alreadyUnbreakable) stack.remove(DataComponents.UNBREAKABLE);
            }

            if (!alreadyUnbreakable && !player.hasInfiniteMaterials() && brokenBlocks > 0) {
                blocksTowardDurability += brokenBlocks;
                while (blocksTowardDurability >= ResonantMiningRules.BLOCKS_PER_DURABILITY
                        && !stack.isEmpty()) {
                    blocksTowardDurability -= ResonantMiningRules.BLOCKS_PER_DURABILITY;
                    stack.hurtAndBreak(1, level, player,
                            item -> player.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
                }
            }
            extinguishedLava |= clearTunnelLava(level, tunnelPositions);
            if (extinguishedLava) {
                level.playSound(null, BlockPos.containing(center), SoundEvents.LAVA_EXTINGUISH,
                        SoundSource.BLOCKS, 0.45F, 1.25F);
            }

            if (step >= forwardRange || stack.isEmpty()) {
                lavaStabilizationTicks = ResonantMiningRules.LAVA_STABILIZATION_TICKS;
            }
            return true;
        }

        private static boolean containAdjacentLava(ServerLevel level, Set<BlockPos> tunnel) {
            boolean changed = false;
            for (BlockPos pos : boundaryPositions(tunnel)) {
                if (!level.hasChunkAt(pos)) {
                    continue;
                }
                FluidState fluidState = level.getFluidState(pos);
                if (!fluidState.is(FluidTags.LAVA)) {
                    continue;
                }
                level.setBlock(pos, fluidState.isSource()
                        ? Blocks.OBSIDIAN.defaultBlockState()
                        : Blocks.AIR.defaultBlockState(), 3);
                changed = true;
            }
            return changed;
        }

        private static void stabilizeTunnelLava(ServerLevel level, Set<BlockPos> tunnel) {
            containAdjacentLava(level, tunnel);
            clearTunnelLava(level, tunnel);
        }

        private static boolean clearTunnelLava(ServerLevel level, Set<BlockPos> tunnel) {
            boolean changed = false;
            for (BlockPos pos : tunnel) {
                if (!level.hasChunkAt(pos)) {
                    continue;
                }
                FluidState fluidState = level.getFluidState(pos);
                if (fluidState.is(FluidTags.LAVA)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    changed = true;
                }
            }
            return changed;
        }

        private static Set<BlockPos> boundaryPositions(Set<BlockPos> tunnel) {
            Set<BlockPos> boundary = new LinkedHashSet<>();
            for (BlockPos pos : tunnel) {
                for (Direction direction : Direction.values()) {
                    BlockPos adjacent = pos.relative(direction);
                    if (!tunnel.contains(adjacent)) {
                        boundary.add(adjacent);
                    }
                }
            }
            return boundary;
        }

        private static Set<BlockPos> planePositions(Vec3 center, Vec3 right, Vec3 up) {
            Set<BlockPos> positions = new LinkedHashSet<>();
            for (int horizontal = -ResonantMiningRules.SIDE_RADIUS;
                 horizontal <= ResonantMiningRules.SIDE_RADIUS; horizontal++) {
                for (int vertical = -ResonantMiningRules.SIDE_RADIUS;
                     vertical <= ResonantMiningRules.SIDE_RADIUS; vertical++) {
                    positions.add(BlockPos.containing(center
                            .add(right.scale(horizontal))
                            .add(up.scale(vertical))));
                }
            }
            return positions;
        }
    }

    private record MiningCharge(Item item, int enchantmentLevel, long startedAt) {}
}
