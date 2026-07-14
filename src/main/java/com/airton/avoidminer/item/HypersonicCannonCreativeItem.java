package com.airton.avoidminer.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Consumer;

public class HypersonicCannonCreativeItem extends Item {
    private static final double HORIZONTAL_KNOCKBACK = 2.5;
    private static final double VERTICAL_KNOCKBACK = 0.5;
    private static final int CHARGE_TICKS = 10;
    private static final double RANGE = 26.0;
    private static final float MAX_DAMAGE = 18.0F;
    private static final float MIN_DAMAGE = 9.0F;
    private static final double CONE_DEGREES = 12.0;
    private static final int MAX_TARGETS = 8;
    private static final int RICOCHETS = 2;
    private static final double RICOCHET_RANGE = 8.0;

    public HypersonicCannonCreativeItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.PLAYERS, 3.0F, 1.0F);
            level.gameEvent(GameEvent.INSTRUMENT_PLAY, player.position(), GameEvent.Context.of(player));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return CHARGE_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.TOOT_HORN;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && level instanceof ServerLevel serverLevel) {
            fire(serverLevel, player);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return stack;
    }

    private void fire(ServerLevel level, Player shooter) {
        Vec3 direction = shooter.getLookAngle().normalize();
        Vec3 source = shooter.getEyePosition().add(direction.scale(0.4));
        sendSonicTrail(level, source, direction, RANGE, true);
        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 3.0F, 1.0F);

        List<LivingEntity> directTargets = findConeTargets(level, shooter, source, direction);
        Set<LivingEntity> hitTargets = new HashSet<>();
        for (LivingEntity target : directTargets) {
            float damage = damageAtDistance(source.distanceTo(target.getEyePosition()));
            damageAndKnockBack(level, shooter, target, direction, damage);
            hitTargets.add(target);
        }

        if (RICOCHETS > 0 && !directTargets.isEmpty()) {
            ricochet(level, shooter, directTargets.getLast(), hitTargets);
        }
    }

    private List<LivingEntity> findConeTargets(ServerLevel level, Player shooter, Vec3 source, Vec3 direction) {
        AABB searchArea = shooter.getBoundingBox().inflate(RANGE);
        return level.getEntitiesOfClass(LivingEntity.class, searchArea,
                        target -> target != shooter && isValidTarget(target)
                                && HypersonicCannonItem.isInsideCone(
                                        source, direction, target.getBoundingBox(), RANGE, CONE_DEGREES))
                .stream()
                .sorted(Comparator.comparingDouble(target -> source.distanceToSqr(target.getEyePosition())))
                .limit(MAX_TARGETS)
                .toList();
    }

    private void ricochet(ServerLevel level, Player shooter, LivingEntity first, Set<LivingEntity> hitTargets) {
        LivingEntity current = first;
        for (int bounce = 0; bounce < RICOCHETS; bounce++) {
            Vec3 origin = current.getEyePosition();
            LivingEntity next = level.getEntitiesOfClass(LivingEntity.class,
                            current.getBoundingBox().inflate(RICOCHET_RANGE),
                            target -> target != shooter && !hitTargets.contains(target) && isValidTarget(target))
                    .stream()
                    .min(Comparator.comparingDouble(target -> origin.distanceToSqr(target.getEyePosition())))
                    .orElse(null);
            if (next == null) return;

            Vec3 delta = next.getEyePosition().subtract(origin);
            Vec3 bounceDirection = delta.normalize();
            sendSonicTrail(level, origin, bounceDirection, delta.length(), false);
            level.playSound(null, next.getX(), next.getY(), next.getZ(),
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.4F, 1.15F + bounce * 0.1F);
            damageAndKnockBack(level, shooter, next, bounceDirection, MIN_DAMAGE * 0.65F);
            hitTargets.add(next);
            current = next;
        }
    }

    private static boolean isValidTarget(Entity entity) {
        return entity instanceof LivingEntity living && living.isAlive() && !living.isSpectator();
    }

    private static void damageAndKnockBack(ServerLevel level, Player shooter, LivingEntity target, Vec3 direction, float damage) {
        if (!target.hurtServer(level, level.damageSources().sonicBoom(shooter), damage)) return;
        double resistance = target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        double horizontal = HORIZONTAL_KNOCKBACK * (1.0 - resistance);
        double vertical = VERTICAL_KNOCKBACK * (1.0 - resistance);
        target.push(direction.x * horizontal, direction.y * vertical, direction.z * horizontal);
    }

    private float damageAtDistance(double distance) {
        double progress = Math.clamp(distance / RANGE, 0.0, 1.0);
        return (float) (MAX_DAMAGE + (MIN_DAMAGE - MAX_DAMAGE) * progress);
    }

    private static void sendSonicTrail(ServerLevel level, Vec3 source, Vec3 direction, double distance, boolean wardenTail) {
        int count = wardenTail ? (int) Math.floor(distance) + 6 : Math.max(1, (int) Math.ceil(distance));
        for (int i = 1; i <= count; i++) {
            Vec3 particlePos = source.add(direction.scale(i));
            level.sendParticles(ParticleTypes.SONIC_BOOM, particlePos.x, particlePos.y, particlePos.z,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.hypersonic_cannon_creative.effect")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        builder.accept(Component.translatable("tooltip.avoidminer.hypersonic_cannon_creative.stats",
                        RANGE)
                .withStyle(ChatFormatting.GRAY));
    }
}
