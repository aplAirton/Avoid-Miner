package com.airton.avoidminer.event;

import com.airton.avoidminer.AvoidMiner;
import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.SeismicBootsRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = AvoidMiner.MODID)
public final class SeismicPropulsionBootsHandler {
    private static final DustParticleOptions WARDEN_CYAN = new DustParticleOptions(0x12D9D5, 1.2F);
    private static final DustParticleOptions WARDEN_TEAL = new DustParticleOptions(0x075A67, 1.05F);
    private static final int ANIMATION_TICKS = 8;
    private static final Map<UUID, FallState> FALL_STATES = new HashMap<>();
    private static final Map<MinecraftServer, List<ImpactAnimation>> ACTIVE_ANIMATIONS = new IdentityHashMap<>();

    private SeismicPropulsionBootsHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!boots.is(ModItems.SEISMIC_PROPULSION_BOOTS.get()) || player.isInWater()) {
            FALL_STATES.remove(player.getUUID());
            return;
        }

        if (player.onGround()) {
            FALL_STATES.remove(player.getUUID());
            return;
        }

        FallState state = FALL_STATES.computeIfAbsent(player.getUUID(),
                ignored -> new FallState(player.getY()));
        state.highestY = Math.max(state.highestY, player.getY());
        double geometricFallDistance = Math.max(0.0, state.highestY - player.getY());
        state.maxFallDistance = Math.max(state.maxFallDistance,
                Math.max(player.fallDistance, geometricFallDistance));
        if (SeismicBootsRules.shouldArm(player.isShiftKeyDown(), false,
                player.getDeltaMovement().y, state.maxFallDistance)) {
            state.armed = true;
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!boots.is(ModItems.SEISMIC_PROPULSION_BOOTS.get())) {
            return;
        }

        FallState state = FALL_STATES.remove(player.getUUID());
        double trackedDistance = state == null ? 0.0 : state.maxFallDistance;
        double fallDistance = Math.max(event.getDistance(), trackedDistance);
        if (!SeismicBootsRules.shouldTriggerImpact(state != null && state.armed,
                player.isShiftKeyDown(), fallDistance)) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        event.setCanceled(true);
        activateImpact(level, player, boots, fallDistance);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !event.getSource().is(DamageTypeTags.IS_FALL)) {
            return;
        }

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!boots.is(ModItems.SEISMIC_PROPULSION_BOOTS.get())) {
            return;
        }

        FallState state = FALL_STATES.remove(player.getUUID());
        double trackedDistance = state == null ? 0.0 : state.maxFallDistance;
        double estimatedDistance = Math.max(SeismicBootsRules.MIN_FALL_DISTANCE,
                event.getOriginalAmount() + 3.0);
        double fallDistance = Math.max(trackedDistance, estimatedDistance);
        if (!SeismicBootsRules.shouldTriggerImpact(state != null && state.armed,
                player.isShiftKeyDown(), fallDistance)) {
            return;
        }

        event.setCanceled(true);
        activateImpact((ServerLevel) player.level(), player, boots, fallDistance);
    }

    private static void activateImpact(ServerLevel level, ServerPlayer player, ItemStack boots,
                                       double fallDistance) {
        player.resetFallDistance();
        triggerShockwave(level, player, fallDistance);
        boots.hurtAndBreak(1, level, player,
                item -> player.onEquippedItemBroken(item, EquipmentSlot.FEET));
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        FALL_STATES.clear();
        ACTIVE_ANIMATIONS.remove(event.getServer());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        List<ImpactAnimation> animations = ACTIVE_ANIMATIONS.get(event.getServer());
        if (animations == null) {
            return;
        }

        Iterator<ImpactAnimation> iterator = animations.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().advance(event.getServer())) {
                iterator.remove();
            }
        }
        if (animations.isEmpty()) {
            ACTIVE_ANIMATIONS.remove(event.getServer());
        }
    }

    private static void triggerShockwave(ServerLevel level, ServerPlayer player, double fallDistance) {
        double radius = SeismicBootsRules.shockwaveRadius(fallDistance);
        float baseDamage = SeismicBootsRules.shockwaveDamage(fallDistance);
        Vec3 origin = player.position();
        AABB area = player.getBoundingBox().inflate(radius, 2.5, radius);

        for (Monster monster : level.getEntitiesOfClass(Monster.class, area,
                target -> target.isAlive() && target.distanceToSqr(player) <= radius * radius)) {
            double distance = Math.sqrt(monster.distanceToSqr(player));
            float damage = SeismicBootsRules.damageAtDistance(baseDamage, distance, radius);
            monster.hurtServer(level, level.damageSources().playerAttack(player), damage);
        }

        ACTIVE_ANIMATIONS.computeIfAbsent(level.getServer(), ignored -> new ArrayList<>())
                .add(new ImpactAnimation(level.dimension(), player.blockPosition(), radius));
        level.sendParticles(ParticleTypes.SCULK_SOUL, true, true,
                origin.x, origin.y + 0.2, origin.z, 28,
                radius * 0.3, 0.25, radius * 0.3, 0.04);
        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.PLAYERS, 1.4F, 0.7F);
    }

    private static void emitGroundRing(ServerLevel level, BlockPos center, double radius, int animationTick) {
        int samples = Math.max(24, (int) Math.ceil(radius * 5.0));
        for (int i = 0; i < samples; i++) {
            double angle = Math.PI * 2.0 * i / samples + animationTick * 0.11;
            double jitter = (level.getRandom().nextDouble() - 0.5) * 0.45;
            int x = center.getX() + (int) Math.round(Math.cos(angle) * (radius + jitter));
            int z = center.getZ() + (int) Math.round(Math.sin(angle) * (radius + jitter));
            BlockPos ground = findGround(level, new BlockPos(x, center.getY(), z));
            if (ground == null) {
                continue;
            }

            BlockState state = level.getBlockState(ground);
            if (!state.is(BlockTags.DIRT)
                    && !state.is(BlockTags.BASE_STONE_OVERWORLD)
                    && !state.is(BlockTags.BASE_STONE_NETHER)) {
                continue;
            }

            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), true, true,
                    ground.getX() + 0.5, ground.getY() + 1.05, ground.getZ() + 0.5,
                    3, 0.22, 0.16, 0.22, 0.1);
            if (i % 3 == 0) {
                level.sendParticles((i & 1) == 0 ? WARDEN_CYAN : WARDEN_TEAL, true, true,
                        ground.getX() + 0.5, ground.getY() + 1.1, ground.getZ() + 0.5,
                        1, 0.0, 0.06, 0.0, 0.025);
            }
        }
    }

    private static BlockPos findGround(ServerLevel level, BlockPos start) {
        for (int offset = 0; offset <= 3; offset++) {
            BlockPos candidate = start.below(offset);
            if (!level.getBlockState(candidate).isAir()) {
                return candidate;
            }
        }
        return null;
    }

    private static final class FallState {
        private double highestY;
        private double maxFallDistance;
        private boolean armed;

        private FallState(double initialY) {
            this.highestY = initialY;
        }
    }

    private static final class ImpactAnimation {
        private final ResourceKey<Level> dimension;
        private final BlockPos center;
        private final double maxRadius;
        private int tick;

        private ImpactAnimation(ResourceKey<Level> dimension, BlockPos center, double maxRadius) {
            this.dimension = dimension;
            this.center = center;
            this.maxRadius = maxRadius;
        }

        private boolean advance(MinecraftServer server) {
            ServerLevel level = server.getLevel(dimension);
            if (level == null || tick >= ANIMATION_TICKS) {
                return false;
            }

            tick++;
            double progress = tick / (double) ANIMATION_TICKS;
            double radius = Math.max(1.0, maxRadius * progress);
            emitGroundRing(level, center, radius, tick);
            return tick < ANIMATION_TICKS;
        }
    }

}
