package com.airton.avoidminer.item;

import com.airton.avoidminer.config.AvoidMinerServerConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
            player.getCooldowns().addCooldown(stack,
                    AvoidMinerServerConfig.weaponCooldown(cannonTier.cooldownTicks()));
            stack.hurtAndBreak(1, player, player.getUsedItemHand());
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return stack;
    }

    private void fire(ServerLevel level, Player shooter) {
        Vec3 direction = shooter.getLookAngle().normalize();
        Vec3 source = shooter.getEyePosition().add(direction.scale(0.4));
        double range = AvoidMinerServerConfig.weaponRange(cannonTier.range());
        sendSonicTrail(level, source, direction, range, true);
        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 3.0F, 1.0F);

        List<LivingEntity> directTargets = findConeTargets(level, shooter, source, direction);

        Set<LivingEntity> hitTargets = new HashSet<>();
        for (LivingEntity target : directTargets) {
            double progress = Math.clamp(source.distanceTo(target.getEyePosition()) / range, 0.0, 1.0);
            float baseDamage = (float) (cannonTier.maxDamage()
                    + (cannonTier.minDamage() - cannonTier.maxDamage()) * progress);
            float damage = AvoidMinerServerConfig.weaponDamage(baseDamage);
            damageAndKnockBack(level, shooter, target, direction, damage);
            applyTierEffects(target);
            hitTargets.add(target);
        }

        if (cannonTier.ricochets() > 0 && !directTargets.isEmpty()) {
            ricochet(level, shooter, directTargets.getLast(), hitTargets);
        }
    }

    private List<LivingEntity> findConeTargets(ServerLevel level, Player shooter, Vec3 source, Vec3 direction) {
        double range = AvoidMinerServerConfig.weaponRange(cannonTier.range());
        AABB searchArea = shooter.getBoundingBox().inflate(range);
        return level.getEntitiesOfClass(LivingEntity.class, searchArea,
                        target -> target != shooter && isValidTarget(target)
                                && isInsideCone(source, direction, target.getBoundingBox(), range,
                                        cannonTier.coneDegrees()))
                .stream()
                .sorted(Comparator.comparingDouble(target -> source.distanceToSqr(target.getEyePosition())))
                .limit(cannonTier.maxTargets())
                .toList();
    }

    static boolean isInsideCone(Vec3 source, Vec3 direction, AABB targetBounds, double range, double coneDegrees) {
        Vec3 end = source.add(direction.scale(range));
        if (targetBounds.contains(source) || targetBounds.clip(source, end).isPresent()) {
            return true;
        }

        Vec3 delta = targetBounds.getCenter().subtract(source);
        double forwardDistance = delta.dot(direction);
        double lateralDistance = Math.sqrt(Math.max(0.0,
                delta.lengthSqr() - forwardDistance * forwardDistance));
        double targetRadius = Math.sqrt(
                targetBounds.getXsize() * targetBounds.getXsize()
                        + targetBounds.getYsize() * targetBounds.getYsize()
                        + targetBounds.getZsize() * targetBounds.getZsize()
        ) * 0.5;
        return HypersonicCannonRules.coneIntersectsTarget(
                forwardDistance, lateralDistance, targetRadius, range, coneDegrees);
    }

    private void ricochet(ServerLevel level, Player shooter, LivingEntity first, Set<LivingEntity> hitTargets) {
        LivingEntity current = first;
        for (int bounce = 0; bounce < cannonTier.ricochets(); bounce++) {
            Vec3 origin = current.getEyePosition();
            LivingEntity next = level.getEntitiesOfClass(LivingEntity.class,
                            current.getBoundingBox().inflate(
                                    AvoidMinerServerConfig.weaponRange(cannonTier.ricochetRange())),
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
            damageAndKnockBack(level, shooter, next, bounceDirection,
                    AvoidMinerServerConfig.weaponDamage(cannonTier.minDamage() * 0.65F));
            applyTierEffects(next);
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

    private int tierAmplifier() {
        return switch (cannonTier.tier()) {
            case 2 -> 1;
            case 3 -> 2;
            default -> 0;
        };
    }

    private void applyTierEffects(LivingEntity target) {
        int amplifier = tierAmplifier();
        int duration = 60;
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, amplifier, false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, amplifier, false, true, true));
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
