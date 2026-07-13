package com.airton.avoidminer.item;

import com.airton.avoidminer.ModDataComponents;
import com.airton.avoidminer.ResonantShieldRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class ResonantRetaliationShieldItem extends ShieldItem {
    private static final DustParticleOptions WARDEN_CYAN = new DustParticleOptions(0x12D9D5, 1.15F);
    private static final DustParticleOptions WARDEN_TEAL = new DustParticleOptions(0x075A67, 1.0F);
    private static final Map<UUID, Long> LAST_READY_USE = new HashMap<>();

    public ResonantRetaliationShieldItem(Properties properties) {
        super(properties
                .durability(336)
                .component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
                .repairable(ItemTags.WOODEN_TOOL_MATERIALS)
                .equippableUnswappable(EquipmentSlot.OFFHAND)
                .delayedComponent(DataComponents.BLOCKS_ATTACKS, context -> new BlocksAttacks(
                        0.25F,
                        1.0F,
                        List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
                        new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                        Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                        Optional.of(SoundEvents.SHIELD_BLOCK),
                        Optional.of(SoundEvents.SHIELD_BREAK)
                ))
                .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK)
                .rarity(Rarity.RARE));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel && isReady(stack)) {
            long now = serverLevel.getServer().getTickCount();
            long previous = LAST_READY_USE.getOrDefault(player.getUUID(), -1L);
            if (ResonantShieldRules.isDoubleUse(previous, now)) {
                LAST_READY_USE.remove(player.getUUID());
                player.stopUsingItem();
                setCharge(stack, 0);
                fireRetaliation(serverLevel, player);
                return InteractionResult.SUCCESS;
            }
            LAST_READY_USE.put(player.getUUID(), now);
        } else if (!level.isClientSide()) {
            LAST_READY_USE.remove(player.getUUID());
        }

        return super.use(level, player, hand);
    }

    public static int getCharge(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SHIELD_SONIC_CHARGE.get(), 0);
    }

    public static void setCharge(ItemStack stack, int charge) {
        stack.set(ModDataComponents.SHIELD_SONIC_CHARGE.get(),
                Math.clamp(charge, 0, ResonantShieldRules.MAX_CHARGE));
    }

    public static boolean isReady(ItemStack stack) {
        return ResonantShieldRules.isReady(getCharge(stack));
    }

    public static void emitReadyFlash(ServerLevel level, Player player) {
        Vec3 center = player.getEyePosition().add(player.getLookAngle().normalize().scale(0.65));
        Vec3 side = horizontalSide(player.getLookAngle());
        for (int i = 0; i < 24; i++) {
            double angle = Math.PI * 2.0 * i / 24.0;
            double radius = 0.35 + (i % 3) * 0.16;
            Vec3 point = center.add(side.scale(Math.cos(angle) * radius))
                    .add(0.0, Math.sin(angle) * radius, 0.0);
            level.sendParticles(i % 2 == 0 ? WARDEN_CYAN : WARDEN_TEAL, true, true,
                    point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.sendParticles(ParticleTypes.SONIC_BOOM, true, true,
                center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_TENDRIL_CLICKS,
                SoundSource.PLAYERS, 0.9F, 1.35F);
    }

    private static void fireRetaliation(ServerLevel level, Player player) {
        Vec3 direction = player.getLookAngle().normalize();
        Vec3 source = player.getEyePosition().add(direction.scale(0.45));
        double minimumDot = Math.cos(Math.toRadians(ResonantShieldRules.BLAST_HALF_ANGLE_DEGREES));
        AABB search = player.getBoundingBox().inflate(ResonantShieldRules.BLAST_RANGE);

        level.getEntitiesOfClass(LivingEntity.class, search,
                        target -> isValidTarget(player, target)
                                && isInsideCone(source, direction, target.getEyePosition(), minimumDot))
                .stream()
                .sorted(Comparator.comparingDouble(target -> source.distanceToSqr(target.getEyePosition())))
                .forEach(target -> retaliateAgainst(level, player, target, direction));

        emitRetaliationCone(level, source, direction);
        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.PLAYERS, 1.35F, 1.25F);
    }

    private static boolean isInsideCone(Vec3 source, Vec3 direction, Vec3 target, double minimumDot) {
        Vec3 delta = target.subtract(source);
        double distance = delta.length();
        return distance > 0.0 && distance <= ResonantShieldRules.BLAST_RANGE
                && delta.normalize().dot(direction) >= minimumDot;
    }

    private static boolean isValidTarget(Player player, LivingEntity target) {
        if (target == player || !target.isAlive() || !target.isAttackable()
                || target.skipAttackInteraction(player)) {
            return false;
        }
        return !(target instanceof Player targetPlayer) || player.canHarmPlayer(targetPlayer);
    }

    private static void retaliateAgainst(ServerLevel level, Player player, LivingEntity target, Vec3 direction) {
        target.hurtServer(level, level.damageSources().sonicBoom(player), ResonantShieldRules.BLAST_DAMAGE);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,
                ResonantShieldRules.STUN_TICKS, 6, false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                ResonantShieldRules.STUN_TICKS, 1, false, true, true));

        double resistance = target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        double strength = 1.8 * (1.0 - resistance);
        target.push(direction.x * strength, 0.35 * (1.0 - resistance), direction.z * strength);
        target.hurtMarked = true;
    }

    private static void emitRetaliationCone(ServerLevel level, Vec3 source, Vec3 direction) {
        Vec3 side = horizontalSide(direction);
        for (int distance = 1; distance <= (int) ResonantShieldRules.BLAST_RANGE; distance++) {
            Vec3 center = source.add(direction.scale(distance));
            double halfWidth = 0.2 + distance * 0.32;
            for (int lane = -2; lane <= 2; lane++) {
                Vec3 point = center.add(side.scale(lane * halfWidth / 2.0));
                level.sendParticles((lane & 1) == 0 ? WARDEN_CYAN : WARDEN_TEAL, true, true,
                        point.x, point.y, point.z, 1, 0.0, 0.02, 0.0, 0.01);
            }
            level.sendParticles(ParticleTypes.SONIC_BOOM, true, true,
                    center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static Vec3 horizontalSide(Vec3 direction) {
        Vec3 side = new Vec3(-direction.z, 0.0, direction.x);
        return side.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isReady(stack) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.resonant_shield.charge",
                        getCharge(stack), ResonantShieldRules.MAX_CHARGE)
                .withStyle(isReady(stack) ? ChatFormatting.AQUA : ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.avoidminer.resonant_shield.block")
                .withStyle(ChatFormatting.DARK_AQUA));
        builder.accept(Component.translatable("tooltip.avoidminer.resonant_shield.release")
                .withStyle(ChatFormatting.GRAY));
    }
}
