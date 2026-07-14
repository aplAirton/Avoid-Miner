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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class TemporalClockCreativeItem extends Item {
    public TemporalClockCreativeItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (serverLevel.tickRateManager().isFrozen()) {
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK,
                    SoundSource.PLAYERS, 1.0F, 1.2F);
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, true, true,
                    player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                    30, 0.4, 0.4, 0.4, 0.01);
            TemporalFreezeManager.unfreeze(serverLevel.getServer());
        } else {
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT,
                    SoundSource.PLAYERS, 2.0F, 0.7F);
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, true, true,
                    player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                    100, 0.6, 0.9, 0.6, 0.18);
            TemporalFreezeManager.freezePermanent(serverLevel.getServer());
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.temporal_clock_creative.effect")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        builder.accept(Component.translatable("tooltip.avoidminer.temporal_clock_creative.toggle")
                .withStyle(ChatFormatting.GRAY));
    }
}
