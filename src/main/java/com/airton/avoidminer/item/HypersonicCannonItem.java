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
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class HypersonicCannonItem extends Item {
    private static final double HORIZONTAL_KNOCKBACK = 2.5;
    private static final double VERTICAL_KNOCKBACK = 0.5;

    private final HypersonicCannonRules.CannonTier cannonTier;

    public HypersonicCannonItem(Properties properties) {
        this(properties, 1);
    }

    public HypersonicCannonItem(Properties properties, int tier) {
        super(properties.durability(HypersonicCannonRules.forTier(tier).durability()).enchantable(15));
        this.cannonTier = HypersonicCannonRules.forTier(tier);
    }

    public int getTier() {
        return cannonTier.tier();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

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
        return cannonTier.chargeTicks();
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.TOOT_HORN;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && level instanceof ServerLevel serverLevel) {
            fire(serverLevel, player);
            player.getCooldowns().addCooldown(stack, cannonTier.cooldownTicks());
            stack.hurtAndBreak(1, player, player.getUsedItemHand());
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return stack;
    }

    private void fire(ServerLevel level, Player shooter) {
        Vec3 direction = shooter.getLookAngle().normalize();
        Vec3 source = shooter.getEyePosition().add(direction.scale(0.4));
        sendSonicTrail(level, source, direction, cannonTier.range(), true);
        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 3.0F, 1.0F);

        List<LivingEntity> directTargets = cannonTier.tier() == 1
                ? findPrecisionTarget(level, shooter, source, direction)
                : findConeTargets(level, shooter, source, direction);

        Set<LivingEntity> hitTargets = new HashSet<>();
        for (LivingEntity target : directTargets) {
            float damage = HypersonicCannonRules.damageAtDistance(cannonTier, source.distanceTo(target.getEyePosition()));
            damageAndKnockBack(level, shooter, target, direction, damage);
            hitTargets.add(target);
        }

        if (cannonTier.ricochets() > 0 && !directTargets.isEmpty()) {
            ricochet(level, shooter, directTargets.getLast(), hitTargets);
        }
    }

    private List<LivingEntity> findPrecisionTarget(ServerLevel level, Player shooter, Vec3 source, Vec3 direction) {
        Vec3 end = source.add(direction.scale(cannonTier.range()));
        AABB searchArea = shooter.getBoundingBox().expandTowards(direction.scale(cannonTier.range())).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(shooter, source, end, searchArea,
                HypersonicCannonItem::isValidTarget, cannonTier.range() * cannonTier.range());
        return hit != null && hit.getEntity() instanceof LivingEntity target ? List.of(target) : List.of();
    }

    private List<LivingEntity> findConeTargets(ServerLevel level, Player shooter, Vec3 source, Vec3 direction) {
        double range = cannonTier.range();
        double minimumDot = Math.cos(Math.toRadians(cannonTier.coneDegrees()));
        AABB searchArea = shooter.getBoundingBox().inflate(range);
        return level.getEntitiesOfClass(LivingEntity.class, searchArea,
                        target -> target != shooter && isValidTarget(target)
                                && isInsideCone(source, direction, target.getEyePosition(), range, minimumDot))
                .stream()
                .sorted(Comparator.comparingDouble(target -> source.distanceToSqr(target.getEyePosition())))
                .limit(cannonTier.maxTargets())
                .toList();
    }

    static boolean isInsideCone(Vec3 source, Vec3 direction, Vec3 target, double range, double minimumDot) {
        Vec3 delta = target.subtract(source);
        double distance = delta.length();
        return distance > 0.0 && distance <= range && delta.normalize().dot(direction) >= minimumDot;
    }

    private void ricochet(ServerLevel level, Player shooter, LivingEntity first, Set<LivingEntity> hitTargets) {
        LivingEntity current = first;
        for (int bounce = 0; bounce < cannonTier.ricochets(); bounce++) {
            Vec3 origin = current.getEyePosition();
            LivingEntity next = level.getEntitiesOfClass(LivingEntity.class,
                            current.getBoundingBox().inflate(cannonTier.ricochetRange()),
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
            damageAndKnockBack(level, shooter, next, bounceDirection, cannonTier.minDamage() * 0.65F);
            hitTargets.add(next);
            current = next;
        }
    }

    static boolean isValidTarget(Entity entity) {
        return entity instanceof LivingEntity living && living.isAlive() && !living.isSpectator();
    }

    static void damageAndKnockBack(ServerLevel level, Player shooter, LivingEntity target, Vec3 direction, float damage) {
        if (!target.hurtServer(level, level.damageSources().sonicBoom(shooter), damage)) return;

        double resistance = target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        double horizontal = HypersonicCannonRules.adjustedKnockback(HORIZONTAL_KNOCKBACK, resistance);
        double vertical = HypersonicCannonRules.adjustedKnockback(VERTICAL_KNOCKBACK, resistance);
        target.push(direction.x * horizontal, direction.y * vertical, direction.z * horizontal);
    }

    private static void sendSonicTrail(ServerLevel level, Vec3 source, Vec3 direction, double distance, boolean wardenTail) {
        int count = wardenTail ? HypersonicCannonRules.particleCount(distance) : Math.max(1, (int) Math.ceil(distance));
        for (int i = 1; i <= count; i++) {
            Vec3 particlePos = source.add(direction.scale(i));
            level.sendParticles(ParticleTypes.SONIC_BOOM, particlePos.x, particlePos.y, particlePos.z,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.hypersonic_cannon.tier_" + cannonTier.tier())
                .withStyle(ChatFormatting.DARK_AQUA));
        builder.accept(Component.translatable("tooltip.avoidminer.hypersonic_cannon.stats",
                        cannonTier.range(), cannonTier.cooldownTicks() / 20.0F)
                .withStyle(ChatFormatting.GRAY));
    }
}
