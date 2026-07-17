package com.airton.avoidminer.enchantment;

import com.airton.avoidminer.AvoidMiner;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = AvoidMiner.MODID)
public final class EnderEnchantmentHandler {
    public static final ResourceKey<Enchantment> ENDER = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(AvoidMiner.MODID, "ender"));

    private EnderEnchantmentHandler() {}

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        ItemStack sword = event.getItemStack();
        int level = event.getEntity().level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(ENDER)
                .map(holder -> sword.getEnchantments().getLevel(holder))
                .orElse(0);
        if (level <= 0) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(event.getLevel() instanceof ServerLevel serverLevel)
                || !(event.getEntity() instanceof ServerPlayer player)
                || player.getCooldowns().isOnCooldown(sword)) {
            return;
        }

        ItemStack pearl = new ItemStack(Items.ENDER_PEARL);
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_PEARL_THROW, SoundSource.NEUTRAL,
                0.5F, 0.4F / (serverLevel.getRandom().nextFloat() * 0.4F + 0.8F));
        Projectile.spawnProjectileFromRotation(
                (levelToSpawn, shooter, sourceStack) ->
                        new ThrownEnderpearl(levelToSpawn, shooter, sourceStack),
                serverLevel, pearl, player, 0.0F, 1.5F, 1.0F);
        player.getCooldowns().addCooldown(sword, EnderCooldownRules.cooldownTicks(level));
        player.awardStat(Stats.ITEM_USED.get(sword.getItem()));
    }
}
