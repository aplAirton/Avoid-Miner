package com.airton.avoidminer.enchantment;

import com.airton.avoidminer.AvoidMiner;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = AvoidMiner.MODID)
public final class ResonanceEnchantmentHandler {
    private static final ResourceKey<Enchantment> RESONANT_ECHO = enchantmentKey("resonant_echo");
    private static final ResourceKey<Enchantment> DISSONANCE = enchantmentKey("dissonance");
    private static final Map<UUID, DissonanceState> DISORIENTED_WARDENS = new HashMap<>();

    private ResonanceEnchantmentHandler() {
    }

    @SubscribeEvent
    public static void onDamageApplied(LivingDamageEvent.Post event) {
        if (event.getHealthDamage() <= 0.0F
                || !event.getSource().isDirect()
                || !(event.getSource().getEntity() instanceof ServerPlayer attacker)
                || !(attacker.level() instanceof ServerLevel level)) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        int echoLevel = getEnchantmentLevel(
                level,
                attacker.getItemBySlot(EquipmentSlot.HEAD),
                RESONANT_ECHO
        );
        if (echoLevel > 0) {
            revealNearbyEnemies(level, attacker);
        }

        if (event.getEntity() instanceof Warden warden) {
            int dissonanceLevel = getEnchantmentLevel(level, weapon, DISSONANCE);
            if (dissonanceLevel > 0) {
                disorientWarden(level, warden, attacker, dissonanceLevel);
            }
        }
    }

    @SubscribeEvent
    public static void onWardenChangesTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Warden warden)
                || !(warden.level() instanceof ServerLevel level)
                || event.getNewAboutToBeSetTarget() == null) {
            return;
        }

        DissonanceState state = DISORIENTED_WARDENS.get(warden.getUUID());
        SuppressedAttacker suppressedAttacker = state == null
                ? null
                : state.suppressedAttackers.get(event.getNewAboutToBeSetTarget().getUUID());
        if (suppressedAttacker != null && level.getGameTime() < suppressedAttacker.expiresAt) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        updateDissonance(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        DISORIENTED_WARDENS.clear();
    }

    private static void revealNearbyEnemies(ServerLevel level, ServerPlayer attacker) {
        double radius = ResonanceEnchantmentRules.ECHO_RADIUS;
        AABB area = attacker.getBoundingBox().inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                target -> target instanceof Enemy
                        && target.isAlive()
                        && target.distanceToSqr(attacker) <= radius * radius)) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING,
                    ResonanceEnchantmentRules.ECHO_DURATION_TICKS,
                    0,
                    false,
                    false,
                    false
            ));
        }

        emitEchoWave(level, attacker.position().add(0.0, 0.2, 0.0));
        level.playSound(null, attacker.blockPosition(), SoundEvents.SCULK_CLICKING,
                SoundSource.PLAYERS, 1.0F, 1.25F);
    }

    private static void emitEchoWave(ServerLevel level, Vec3 center) {
        for (int ring = 1; ring <= 3; ring++) {
            double radius = ring * 2.0;
            for (int point = 0; point < 24; point++) {
                double angle = Math.PI * 2.0 * point / 24.0;
                level.sendParticles(
                        ParticleTypes.SCULK_SOUL,
                        center.x + Math.cos(angle) * radius,
                        center.y,
                        center.z + Math.sin(angle) * radius,
                        1,
                        0.0,
                        0.02,
                        0.0,
                        0.0
                );
            }
        }
    }

    private static void disorientWarden(ServerLevel level, Warden warden, ServerPlayer attacker, int enchantmentLevel) {
        long now = level.getGameTime();
        long expiresAt = now + ResonanceEnchantmentRules.dissonanceDurationTicks(enchantmentLevel);
        DissonanceState state = DISORIENTED_WARDENS.compute(warden.getUUID(), (id, existing) -> {
            if (existing == null || !existing.dimension.equals(level.dimension())) {
                return new DissonanceState(level.dimension(), now, attacker.getUUID(), expiresAt);
            }
            existing.suppressedAttackers.compute(attacker.getUUID(), (attackerId, suppressed) -> {
                if (suppressed == null) {
                    return new SuppressedAttacker(expiresAt);
                }
                suppressed.expiresAt = Math.max(suppressed.expiresAt, expiresAt);
                return suppressed;
            });
            existing.nextRedirectAt = Math.min(existing.nextRedirectAt, now);
            return existing;
        });

        suppressTrackedAttackers(level, warden, state);
        level.sendParticles(ParticleTypes.SCULK_CHARGE_POP,
                warden.getX(), warden.getEyeY() + 0.45, warden.getZ(),
                18, 0.45, 0.25, 0.45, 0.02);
        level.playSound(null, warden.blockPosition(), SoundEvents.WARDEN_TENDRIL_CLICKS,
                SoundSource.HOSTILE, 1.4F, 1.6F);
    }

    private static void updateDissonance(MinecraftServer server) {
        Iterator<Map.Entry<UUID, DissonanceState>> iterator = DISORIENTED_WARDENS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, DissonanceState> entry = iterator.next();
            DissonanceState state = entry.getValue();
            ServerLevel level = server.getLevel(state.dimension);
            Entity entity = level == null ? null : level.getEntity(entry.getKey());
            if (!(entity instanceof Warden warden) || !warden.isAlive()) {
                iterator.remove();
                continue;
            }

            long now = level.getGameTime();
            restoreExpiredAnger(level, warden, state, now);
            if (state.suppressedAttackers.isEmpty()) {
                iterator.remove();
                continue;
            }

            suppressTrackedAttackers(level, warden, state);
            if (now >= state.nextRedirectAt && warden.getEntityAngryAt().isEmpty()) {
                int radius = ResonanceEnchantmentRules.DISSONANCE_REDIRECT_RADIUS;
                BlockPos randomSearchPosition = warden.blockPosition().offset(
                        warden.getRandom().nextInt(radius * 2 + 1) - radius,
                        0,
                        warden.getRandom().nextInt(radius * 2 + 1) - radius
                );
                WardenAi.setDisturbanceLocation(warden, randomSearchPosition);
                state.nextRedirectAt = now + ResonanceEnchantmentRules.DISSONANCE_REDIRECT_INTERVAL_TICKS;
            }
        }
    }

    private static void suppressTrackedAttackers(ServerLevel level, Warden warden, DissonanceState state) {
        for (Map.Entry<UUID, SuppressedAttacker> angerEntry : state.suppressedAttackers.entrySet()) {
            Entity attacker = level.getEntity(angerEntry.getKey());
            if (attacker == null) {
                continue;
            }

            int currentAnger = warden.getAngerManagement().getActiveAnger(attacker);
            if (currentAnger > 0) {
                angerEntry.getValue().anger = Math.max(angerEntry.getValue().anger, currentAnger);
                warden.clearAnger(attacker);
            }
        }

        boolean trackedAttacker = warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET)
                .map(target -> state.suppressedAttackers.containsKey(target.getUUID()))
                .orElse(false);
        boolean roarAttacker = warden.getBrain().getMemory(MemoryModuleType.ROAR_TARGET)
                .map(target -> state.suppressedAttackers.containsKey(target.getUUID()))
                .orElse(false);
        if (trackedAttacker || roarAttacker) {
            warden.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            warden.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
            warden.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            warden.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        }
    }

    private static void restoreExpiredAnger(ServerLevel level, Warden warden, DissonanceState state, long now) {
        Iterator<Map.Entry<UUID, SuppressedAttacker>> iterator = state.suppressedAttackers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, SuppressedAttacker> entry = iterator.next();
            SuppressedAttacker suppressed = entry.getValue();
            if (now < suppressed.expiresAt) {
                continue;
            }

            Entity attacker = level.getEntity(entry.getKey());
            if (attacker != null && suppressed.anger > 0) {
                warden.increaseAngerAt(attacker, suppressed.anger, false);
            }
            iterator.remove();
        }
    }

    private static int getEnchantmentLevel(ServerLevel level, ItemStack stack, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(key)
                .map(holder -> stack.getEnchantments().getLevel(holder))
                .orElse(0);
    }

    private static ResourceKey<Enchantment> enchantmentKey(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(AvoidMiner.MODID, path));
    }

    private static final class DissonanceState {
        private final ResourceKey<Level> dimension;
        private final Map<UUID, SuppressedAttacker> suppressedAttackers = new HashMap<>();
        private long nextRedirectAt;

        private DissonanceState(ResourceKey<Level> dimension, long nextRedirectAt, UUID attacker, long expiresAt) {
            this.dimension = dimension;
            this.nextRedirectAt = nextRedirectAt;
            this.suppressedAttackers.put(attacker, new SuppressedAttacker(expiresAt));
        }
    }

    private static final class SuppressedAttacker {
        private long expiresAt;
        private int anger;

        private SuppressedAttacker(long expiresAt) {
            this.expiresAt = expiresAt;
        }
    }
}
