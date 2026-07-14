package com.airton.avoidminer.item;

import com.airton.avoidminer.event.TemporalFreezeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class TemporalSlowClockItem extends Item {
    public TemporalSlowClockItem(Properties properties) {
        super(properties.durability(TemporalSlowRules.DURABILITY).enchantable(20));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        MinecraftServer server = serverLevel.getServer();

        // Toggle: if already slowed, deactivate
        if (TemporalFreezeManager.isSlowed(server)) {
            TemporalFreezeManager.unSlow(server);
            player.removeEffect(MobEffects.SPEED);
            player.removeEffect(MobEffects.HASTE);
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.SCULK_CATALYST_BLOOM,
                    SoundSource.PLAYERS, 1.0F, 1.5F);
            return InteractionResult.SUCCESS;
        }

        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (!TemporalFreezeManager.trySlow(server, TemporalSlowRules.SLOW_TICKS, TemporalSlowRules.SLOW_RATE)) {
            return InteractionResult.FAIL;
        }

        serverLevel.playSound(null, player.blockPosition(), SoundEvents.SCULK_CATALYST_BLOOM,
                SoundSource.PLAYERS, 1.5F, 0.6F);
        serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, true, true,
                player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                80, 0.5, 0.7, 0.5, 0.03);
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, TemporalSlowRules.SLOW_TICKS, 9, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.HASTE, TemporalSlowRules.SLOW_TICKS, 9, false, false, true));
        player.getCooldowns().addCooldown(stack, TemporalSlowRules.COOLDOWN_TICKS);
        stack.hurtAndBreak(1, player, hand);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.temporal_slow_clock.effect").withStyle(ChatFormatting.DARK_GREEN));
        builder.accept(Component.translatable("tooltip.avoidminer.temporal_slow_clock.balance").withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.avoidminer.temporal_clock.heart").withStyle(ChatFormatting.DARK_AQUA));
    }
}
