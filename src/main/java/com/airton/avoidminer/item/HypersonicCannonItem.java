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
import java.util.List;
import java.util.function.Consumer;

public class HypersonicCannonItem extends Item {
    public static final int COOLDOWN_TICKS = HypersonicCannonRules.COOLDOWN_TICKS;
    public static final int CHARGE_TICKS = HypersonicCannonRules.CHARGE_TICKS;
    public static final double RANGE = HypersonicCannonRules.RANGE;
    public static final float DAMAGE = HypersonicCannonRules.DAMAGE;
    public static final double HORIZONTAL_KNOCKBACK = HypersonicCannonRules.HORIZONTAL_KNOCKBACK;
    public static final double VERTICAL_KNOCKBACK = HypersonicCannonRules.VERTICAL_KNOCKBACK;

    public HypersonicCannonItem(Properties properties) {
        super(properties.stacksTo(1));
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
            player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return stack;
    }

    public static void fire(ServerLevel level, Player shooter) {
        Vec3 direction = shooter.getLookAngle().normalize();
        Vec3 source = shooter.getEyePosition().add(direction.scale(0.4));
        Vec3 end = source.add(direction.scale(RANGE));

        for (Vec3 particlePos : particlePositions(source, direction, RANGE)) {
            level.sendParticles(ParticleTypes.SONIC_BOOM, particlePos.x, particlePos.y, particlePos.z,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 3.0F, 1.0F);

        AABB searchArea = shooter.getBoundingBox().expandTowards(direction.scale(RANGE)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                shooter,
                source,
                end,
                searchArea,
                HypersonicCannonItem::isValidTarget,
                RANGE * RANGE
        );
        if (hit != null && hit.getEntity() instanceof LivingEntity target) {
            damageAndKnockBack(level, shooter, target, direction);
        }
    }

    static boolean isValidTarget(Entity entity) {
        return entity instanceof LivingEntity living && living.isAlive() && !living.isSpectator();
    }

    static void damageAndKnockBack(ServerLevel level, Player shooter, LivingEntity target, Vec3 direction) {
        if (!target.hurtServer(level, level.damageSources().sonicBoom(shooter), DAMAGE)) {
            return;
        }

        double resistance = target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        double horizontal = HypersonicCannonRules.adjustedKnockback(HORIZONTAL_KNOCKBACK, resistance);
        double vertical = HypersonicCannonRules.adjustedKnockback(VERTICAL_KNOCKBACK, resistance);
        target.push(direction.x * horizontal, direction.y * vertical, direction.z * horizontal);
    }

    static List<Vec3> particlePositions(Vec3 source, Vec3 direction, double distance) {
        int count = HypersonicCannonRules.particleCount(distance);
        List<Vec3> positions = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            positions.add(source.add(direction.scale(i)));
        }
        return positions;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.hypersonic_cannon.attack")
                .withStyle(ChatFormatting.DARK_AQUA));
        builder.accept(Component.translatable("tooltip.avoidminer.hypersonic_cannon.cooldown")
                .withStyle(ChatFormatting.GRAY));
    }
}
