package com.airton.avoidminer.item;

import com.airton.avoidminer.event.ResonantMiningManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.HumanoidArm;
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

/**
 * Picareta Ressonante — a onda de mineração agora carrega como as demais
 * ferramentas de poder: segure o uso por 1s (partículas douradas discretas
 * junto à mão), solte para disparar; 2s de cooldown por onda.
 */
public final class ResonantPickaxeItem extends Item {
    private static final DustParticleOptions GOLD_SPARK = new DustParticleOptions(0xFFC838, 1.0F);
    private static final DustParticleOptions GOLD_PALE = new DustParticleOptions(0xFFE28A, 0.85F);

    public ResonantPickaxeItem(Properties properties) {
        super(properties
                .pickaxe(ToolMaterial.NETHERITE, 1.0F, -2.8F)
                .durability(ResonantMiningRules.RESONANT_PICKAXE_DURABILITY)
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
        if (heldTicks == ResonantMiningRules.CHARGE_TICKS) {
            emitReadyBurst(serverLevel, player);
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS, 0.8F, 1.3F);
        } else if (heldTicks < ResonantMiningRules.CHARGE_TICKS && heldTicks % 3 == 0) {
            emitChargingOrbit(serverLevel, player, heldTicks);
        } else if (heldTicks > ResonantMiningRules.CHARGE_TICKS && heldTicks % 5 == 0) {
            emitChargedHalo(serverLevel, player, heldTicks);
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        int heldTicks = getUseDuration(stack, entity) - remainingTime;
        if (!(level instanceof ServerLevel)) {
            return true;
        }
        if (heldTicks < ResonantMiningRules.CHARGE_TICKS || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        if (ResonantMiningManager.tryLaunch(serverPlayer, stack)) {
            player.getCooldowns().addCooldown(stack, ResonantMiningRules.COOLDOWN_TICKS);
            return true;
        }
        return false;
    }

    // Carga discreta: duas fagulhas douradas orbitando junto à mão da
    // picareta, fora da linha da mira
    private static void emitChargingOrbit(ServerLevel level, Player player, int heldTicks) {
        double progress = Math.min(1.0, heldTicks / (double) ResonantMiningRules.CHARGE_TICKS);
        Vec3 anchor = chargeAnchor(player);
        Vec3 side = horizontalSide(player);
        double radius = 0.28 - progress * 0.14;

        for (int i = 0; i < 2; i++) {
            double angle = heldTicks * 0.45 + i * Math.PI;
            Vec3 point = anchor.add(side.scale(Math.cos(angle) * radius))
                    .add(0.0, Math.sin(angle) * radius, 0.0);
            level.sendParticles(i % 2 == 0 ? GOLD_SPARK : GOLD_PALE, true, true,
                    point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static void emitReadyBurst(ServerLevel level, Player player) {
        Vec3 anchor = chargeAnchor(player);
        level.sendParticles(ParticleTypes.WAX_ON, true, true,
                anchor.x, anchor.y, anchor.z, 10, 0.2, 0.2, 0.2, 0.03);
    }

    // Mantida carregada: halo dourado pequeno e lento na altura da mão
    private static void emitChargedHalo(ServerLevel level, Player player, int heldTicks) {
        Vec3 anchor = chargeAnchor(player);
        Vec3 side = horizontalSide(player);
        for (int i = 0; i < 3; i++) {
            double angle = -heldTicks * 0.18 + Math.PI * 2.0 * i / 3.0;
            Vec3 point = anchor.add(side.scale(Math.cos(angle) * 0.18))
                    .add(0.0, Math.sin(angle) * 0.18, 0.0);
            level.sendParticles(i % 2 == 0 ? GOLD_SPARK : GOLD_PALE, true, true,
                    point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    // Posição da mão que segura a picareta, abaixo da linha do olhar
    private static Vec3 chargeAnchor(Player player) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 side = horizontalSide(player);
        boolean rightSide = (player.getMainArm() == HumanoidArm.RIGHT)
                == (player.getUsedItemHand() == InteractionHand.MAIN_HAND);
        return player.getEyePosition()
                .add(look.scale(0.55))
                .add(side.scale(rightSide ? 0.45 : -0.45))
                .add(0.0, -0.5, 0.0);
    }

    private static Vec3 horizontalSide(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0.0, look.x);
        return side.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.resonant_pickaxe.power",
                        ResonantMiningRules.NATIVE_FORWARD_RANGE,
                        ResonantMiningRules.SIDE_RADIUS * 2 + 1,
                        ResonantMiningRules.SIDE_RADIUS * 2 + 1)
                .withStyle(ChatFormatting.DARK_AQUA));
        builder.accept(Component.translatable("tooltip.avoidminer.resonant_pickaxe.use")
                .withStyle(ChatFormatting.GRAY));
    }
}
