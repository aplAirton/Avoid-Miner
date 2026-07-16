package com.airton.avoidminer.enchantment;

import com.airton.avoidminer.AvoidMiner;
import java.util.Comparator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

@EventBusSubscriber(modid = AvoidMiner.MODID)
public final class ProjectileDeflectionHandler {
    public static final ResourceKey<Enchantment> PROJECTILE_DEFLECTION = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(AvoidMiner.MODID, "projectile_deflection"));
    private static final double REDIRECT_RADIUS = 8.0D;

    private ProjectileDeflectionHandler() {}

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile().level() instanceof ServerLevel level)
                || !(event.getRayTraceResult() instanceof EntityHitResult hit)
                || !(hit.getEntity() instanceof Player player)
                || !hasDeflection(level, player.getItemBySlot(EquipmentSlot.CHEST))) {
            return;
        }

        Projectile projectile = event.getProjectile();
        LivingEntity target = findRedirectTarget(level, player, projectile);
        Vec3 direction = target != null
                ? target.getBoundingBox().getCenter().subtract(hit.getLocation()).normalize()
                : randomDirection(level, projectile.getDeltaMovement());
        double speed = Math.max(0.5D, projectile.getDeltaMovement().length());
        Vec3 redirectedVelocity = direction.scale(speed);

        event.setCanceled(true);
        projectile.setPos(hit.getLocation().add(direction.scale(0.75D)));
        projectile.deflect((redirected, deflector, random) ->
                        redirected.setDeltaMovement(redirectedVelocity),
                player, EntityReference.of(player), true);

        level.playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK.value(),
                SoundSource.PLAYERS, 0.8F, 1.55F);
    }

    private static boolean hasDeflection(ServerLevel level, ItemStack chest) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(PROJECTILE_DEFLECTION)
                .map(holder -> chest.getEnchantments().getLevel(holder) > 0)
                .orElse(false);
    }

    private static LivingEntity findRedirectTarget(ServerLevel level, Player player, Projectile projectile) {
        AABB area = player.getBoundingBox().inflate(REDIRECT_RADIUS);
        return level.getEntitiesOfClass(LivingEntity.class, area,
                        target -> target != player && target != projectile.getOwner()
                                && target.isAlive() && target.isAttackable() && !target.isSpectator())
                .stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }

    private static Vec3 randomDirection(ServerLevel level, Vec3 incoming) {
        Vec3 random = new Vec3(
                level.getRandom().nextDouble() * 2.0D - 1.0D,
                level.getRandom().nextDouble() * 1.5D - 0.5D,
                level.getRandom().nextDouble() * 2.0D - 1.0D);
        if (random.lengthSqr() < 1.0E-5D) random = incoming.scale(-1.0D);
        random = random.normalize();
        if (random.dot(incoming.normalize()) > 0.7D) random = random.scale(-1.0D);
        return random;
    }
}
