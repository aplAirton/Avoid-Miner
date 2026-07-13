package com.airton.avoidminer.item;

import com.airton.avoidminer.event.GlassSwordAttackManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class GlassSwordItem extends Item {
    private static final DustParticleOptions WARDEN_CYAN = new DustParticleOptions(0x12D9D5, 1.05F);
    private static final DustParticleOptions WARDEN_TEAL = new DustParticleOptions(0x075A67, 0.9F);

    public GlassSwordItem(Properties properties) {
        super(properties.sword(ToolMaterial.NETHERITE, 12.0F, -2.4F)
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72_000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof Player player)) {
            return;
        }

        int heldTicks = getUseDuration(stack, entity) - ticksRemaining;
        if (heldTicks == GlassSwordRules.CHARGE_TICKS) {
            emitReadyBurst(serverLevel, player);
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.WARDEN_TENDRIL_CLICKS,
                    SoundSource.PLAYERS, 0.55F, 1.35F);
        } else if (heldTicks < GlassSwordRules.CHARGE_TICKS && heldTicks % 2 == 0) {
            emitChargingSpiral(serverLevel, player, heldTicks);
        } else if (heldTicks > GlassSwordRules.CHARGE_TICKS && heldTicks % 3 == 0) {
            emitChargedHalo(serverLevel, player, heldTicks);
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        int heldTicks = getUseDuration(stack, entity) - remainingTime;
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }

        if (GlassSwordRules.isCharged(heldTicks)) {
            GlassSwordAttackManager.launchPressureWave(serverLevel, player, stack.copy());
            completeSpecialAttack(player, stack);
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.BREEZE_SHOOT,
                    SoundSource.PLAYERS, 1.4F, 0.65F);
            return true;
        }
        return false;
    }

    private void completeSpecialAttack(Player player, ItemStack stack) {
        player.getCooldowns().addCooldown(stack, GlassSwordRules.SPECIAL_COOLDOWN_TICKS);
        stack.hurtAndBreak(1, player, player.getUsedItemHand());
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    private static void emitChargingSpiral(ServerLevel level, Player player, int heldTicks) {
        double progress = Math.min(1.0, heldTicks / (double) GlassSwordRules.CHARGE_TICKS);
        Vec3 focus = chargeFocus(player);
        Vec3 side = horizontalSide(player);
        double radius = 0.72 - progress * 0.48;

        for (int i = 0; i < 6; i++) {
            double angle = heldTicks * 0.32 + Math.PI * 2.0 * i / 6.0;
            Vec3 point = focus.add(side.scale(Math.cos(angle) * radius))
                    .add(0.0, Math.sin(angle) * radius, 0.0);
            level.sendParticles(i % 2 == 0 ? WARDEN_CYAN : WARDEN_TEAL, true, true,
                    point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static void emitReadyBurst(ServerLevel level, Player player) {
        Vec3 focus = chargeFocus(player);
        Vec3 side = horizontalSide(player);
        for (int i = 0; i < 18; i++) {
            double angle = Math.PI * 2.0 * i / 18.0;
            double radius = 0.2 + (i % 3) * 0.14;
            Vec3 point = focus.add(side.scale(Math.cos(angle) * radius))
                    .add(0.0, Math.sin(angle) * radius, 0.0);
            level.sendParticles(i % 2 == 0 ? WARDEN_CYAN : WARDEN_TEAL, true, true,
                    point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.sendParticles(ParticleTypes.SONIC_BOOM, true, true,
                focus.x, focus.y, focus.z, 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles(ParticleTypes.SCULK_SOUL, true, true,
                focus.x, focus.y, focus.z, 8, 0.25, 0.25, 0.25, 0.02);
    }

    private static void emitChargedHalo(ServerLevel level, Player player, int heldTicks) {
        Vec3 focus = chargeFocus(player);
        Vec3 side = horizontalSide(player);
        for (int i = 0; i < 8; i++) {
            double angle = -heldTicks * 0.24 + Math.PI * 2.0 * i / 8.0;
            Vec3 point = focus.add(side.scale(Math.cos(angle) * 0.28))
                    .add(0.0, Math.sin(angle) * 0.28, 0.0);
            level.sendParticles(i % 2 == 0 ? WARDEN_CYAN : WARDEN_TEAL, true, true,
                    point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static Vec3 chargeFocus(Player player) {
        return player.getEyePosition().add(player.getLookAngle().normalize().scale(0.95)).add(0.0, -0.32, 0.0);
    }

    private static Vec3 horizontalSide(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0.0, look.x);
        return side.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.glass_sword.wave", GlassSwordRules.WAVE_RANGE)
                .withStyle(ChatFormatting.AQUA));
        builder.accept(Component.translatable("tooltip.avoidminer.glass_sword.enchantments")
                .withStyle(ChatFormatting.GRAY));
    }
}
