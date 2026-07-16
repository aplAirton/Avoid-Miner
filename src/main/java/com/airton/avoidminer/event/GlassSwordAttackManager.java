package com.airton.avoidminer.event;

import com.airton.avoidminer.AvoidMiner;
import com.airton.avoidminer.config.AvoidMinerServerConfig;
import com.airton.avoidminer.item.GlassSwordRules;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = AvoidMiner.MODID)
public final class GlassSwordAttackManager {
    private static final DustParticleOptions WARDEN_CYAN = new DustParticleOptions(0x12D9D5, 1.15F);
    private static final DustParticleOptions WARDEN_TEAL = new DustParticleOptions(0x075A67, 1.0F);
    private static final Map<MinecraftServer, List<PressureWave>> ACTIVE_WAVES = new IdentityHashMap<>();

    private GlassSwordAttackManager() {
    }

    public static void launchPressureWave(ServerLevel level, Player player, ItemStack weapon) {
        Vec3 direction = player.getLookAngle().normalize();
        Vec3 origin = player.getEyePosition().add(direction.scale(0.4));
        ACTIVE_WAVES.computeIfAbsent(level.getServer(), ignored -> new ArrayList<>())
                .add(new PressureWave(level.dimension(), player.getUUID(), origin, direction, weapon));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        List<PressureWave> waves = ACTIVE_WAVES.get(event.getServer());
        if (waves == null) {
            return;
        }
        Iterator<PressureWave> iterator = waves.iterator();
        while (iterator.hasNext()) {
            PressureWave wave = iterator.next();
            if (!wave.advance(event.getServer())) {
                iterator.remove();
            }
        }
        if (waves.isEmpty()) {
            ACTIVE_WAVES.remove(event.getServer());
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ACTIVE_WAVES.remove(event.getServer());
    }

    private static void emitPressureFront(ServerLevel level, Vec3 center, Vec3 direction) {
        Vec3 side = sideVector(direction);
        Vec3 up = side.cross(direction).normalize();
        for (int horizontal = -3; horizontal <= 3; horizontal++) {
            for (int vertical = -1; vertical <= 2; vertical++) {
                Vec3 particle = center.add(side.scale(horizontal * 0.45))
                        .add(up.scale(vertical * 0.38));
                level.sendParticles((horizontal + vertical & 1) == 0 ? WARDEN_CYAN : WARDEN_TEAL,
                        true, true, particle.x, particle.y, particle.z, 1,
                        direction.x * 0.02, direction.y * 0.02, direction.z * 0.02, 0.01);
            }
        }
        level.sendParticles(ParticleTypes.SONIC_BOOM, true, true,
                center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles(ParticleTypes.SCULK_SOUL, true, true,
                center.x, center.y, center.z, 3, 0.35, 0.45, 0.35, 0.015);
    }

    private static boolean isValidTarget(Player player, LivingEntity target) {
        if (target == player || !target.isAlive() || !target.isAttackable()
                || target.skipAttackInteraction(player)
                || target instanceof ArmorStand armorStand && armorStand.isMarker()) {
            return false;
        }
        return !(target instanceof Player targetPlayer) || player.canHarmPlayer(targetPlayer);
    }

    private static void damageTarget(ServerLevel level, Player player, ItemStack weapon,
                                     LivingEntity target, Vec3 direction, float baseDamage, float baseKnockback) {
        DamageSource source = weapon.getDamageSource(player, () -> level.damageSources().playerAttack(player));
        float damage = EnchantmentHelper.modifyDamage(level, weapon, target, source, baseDamage);
        if (target.hurtServer(level, source, damage)) {
            float knockback = EnchantmentHelper.modifyKnockback(level, weapon, target, source, baseKnockback);
            if (knockback > 0.0F) {
                target.push(direction.x * knockback, direction.y * knockback, direction.z * knockback);
                target.hurtMarked = true;
            }
            EnchantmentHelper.doPostAttackEffectsWithItemSource(level, target, source, weapon);
        }
    }

    private static Vec3 sideVector(Vec3 direction) {
        Vec3 side = new Vec3(-direction.z, 0.0, direction.x);
        return side.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
    }

    private static final class PressureWave {
        private final ResourceKey<Level> dimension;
        private final UUID ownerId;
        private final Vec3 origin;
        private final Vec3 direction;
        private final ItemStack weapon;
        private final Set<UUID> hitTargets = new HashSet<>();
        private int step;
        private UUID lastHitUuid;

        private PressureWave(ResourceKey<Level> dimension, UUID ownerId, Vec3 origin,
                             Vec3 direction, ItemStack weapon) {
            this.dimension = dimension;
            this.ownerId = ownerId;
            this.origin = origin;
            this.direction = direction;
            this.weapon = weapon;
        }

        private boolean advance(MinecraftServer server) {
            ServerLevel level = server.getLevel(dimension);
            ServerPlayer owner = server.getPlayerList().getPlayer(ownerId);
            int waveRange = Math.max(1, (int) Math.round(
                    AvoidMinerServerConfig.weaponRange(GlassSwordRules.WAVE_RANGE)));
            if (level == null || owner == null || owner.level() != level || step >= waveRange) {
                if (level != null && owner != null && lastHitUuid != null) {
                    triggerRicochet(level, owner);
                }
                return false;
            }

            step++;
            Vec3 center = origin.add(direction.scale(step));
            emitPressureFront(level, center, direction);
            Vec3 side = sideVector(direction);
            Vec3 up = side.cross(direction).normalize();
            AABB search = new AABB(center.x - 2.5, center.y - 2.5, center.z - 2.5,
                    center.x + 2.5, center.y + 2.5, center.z + 2.5);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, search,
                    candidate -> isValidTarget(owner, candidate) && !hitTargets.contains(candidate.getUUID()))) {
                Vec3 relative = target.getBoundingBox().getCenter().subtract(center);
                if (Math.abs(relative.dot(side)) <= 1.8
                        && Math.abs(relative.dot(direction)) <= 0.9
                        && Math.abs(relative.dot(up)) <= 1.7) {
                    hitTargets.add(target.getUUID());
                    lastHitUuid = target.getUUID();
                    damageTarget(level, owner, weapon, target, direction,
                            AvoidMinerServerConfig.weaponDamage(GlassSwordRules.BASE_DAMAGE), 0.55F);
                }
            }
            return step < waveRange;
        }

        private void triggerRicochet(ServerLevel level, ServerPlayer owner) {
            for (int bounce = 0; bounce < 2; bounce++) {
                LivingEntity origin = (LivingEntity) level.getEntity(lastHitUuid);
                if (origin == null) return;

                Vec3 originPos = origin.getEyePosition();
                LivingEntity next = level.getEntitiesOfClass(LivingEntity.class,
                                origin.getBoundingBox().inflate(8.0),
                                target -> isValidTarget(owner, target) && !hitTargets.contains(target.getUUID()))
                        .stream()
                        .min(Comparator.comparingDouble(t -> originPos.distanceToSqr(t.getEyePosition())))
                        .orElse(null);
                if (next == null) return;

                Vec3 delta = next.getEyePosition().subtract(originPos);
                Vec3 bounceDirection = delta.normalize();
                damageTarget(level, owner, weapon, next, bounceDirection,
                        AvoidMinerServerConfig.weaponDamage(GlassSwordRules.BASE_DAMAGE * 0.5F), 0.55F);
                hitTargets.add(next.getUUID());
                lastHitUuid = next.getUUID();
            }
        }
    }
}
