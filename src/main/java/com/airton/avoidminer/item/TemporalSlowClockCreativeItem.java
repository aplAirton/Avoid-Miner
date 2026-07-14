package com.airton.avoidminer.item;

import com.airton.avoidminer.event.TemporalFreezeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

public class TemporalSlowClockCreativeItem extends Item {
    public TemporalSlowClockCreativeItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (serverLevel.tickRateManager().tickrate() < 20.0f) {
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.SCULK_CATALYST_BLOOM,
                    SoundSource.PLAYERS, 1.0F, 1.5F);
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, true, true,
                    player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                    40, 0.4, 0.5, 0.4, 0.02);
            player.removeEffect(MobEffects.SPEED);
            player.removeEffect(MobEffects.HASTE);
            TemporalFreezeManager.unSlow(serverLevel.getServer());
        } else {
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.SCULK_CATALYST_BLOOM,
                    SoundSource.PLAYERS, 1.5F, 0.6F);
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, true, true,
                    player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                    80, 0.5, 0.7, 0.5, 0.03);
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 72000, 9, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, 72000, 9, false, false, true));
            TemporalFreezeManager.slowPermanent(serverLevel.getServer(), TemporalSlowRules.SLOW_RATE);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.temporal_slow_clock_creative.effect")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        builder.accept(Component.translatable("tooltip.avoidminer.temporal_slow_clock_creative.toggle")
                .withStyle(ChatFormatting.GRAY));
    }
}
