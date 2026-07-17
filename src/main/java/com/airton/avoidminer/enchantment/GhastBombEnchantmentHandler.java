package com.airton.avoidminer.enchantment;

import com.airton.avoidminer.AvoidMiner;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = AvoidMiner.MODID)
public final class GhastBombEnchantmentHandler {
    private static final int EXPLOSION_POWER = 1;
    private static final double SPEED_MULTIPLIER = 3.0D;
    public static final ResourceKey<Enchantment> GHAST_BOMB = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(AvoidMiner.MODID, "ghast_bomb"));

    private GhastBombEnchantmentHandler() {}

    @SubscribeEvent
    public static void onArrowSpawn(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk()
                || !(event.getLevel() instanceof ServerLevel serverLevel)
                || !(event.getEntity() instanceof AbstractArrow arrow)
                || !(arrow.getOwner() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack bow = arrow.getWeaponItem();
        int level = serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(GHAST_BOMB)
                .map(holder -> bow.getEnchantments().getLevel(holder))
                .orElse(0);
        if (level <= 0) return;

        Vec3 direction = arrow.getDeltaMovement();
        if (direction.lengthSqr() < 1.0E-6D) {
            direction = player.getLookAngle();
        } else {
            direction = direction.normalize();
        }

        LargeFireball fireball = new LargeFireball(
                serverLevel, player, direction, EXPLOSION_POWER);
        fireball.setPos(arrow.position());
        fireball.accelerationPower *= SPEED_MULTIPLIER;
        fireball.setDeltaMovement(fireball.getDeltaMovement().scale(SPEED_MULTIPLIER));

        event.setCanceled(true);
        serverLevel.addFreshEntity(fireball);
    }
}
