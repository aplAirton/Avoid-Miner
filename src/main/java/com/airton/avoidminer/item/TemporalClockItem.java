package com.airton.avoidminer.item;

import com.airton.avoidminer.event.TemporalFreezeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class TemporalClockItem extends Item {
    public TemporalClockItem(Properties properties) {
        super(properties.durability(TemporalFreezeRules.DURABILITY).enchantable(20));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        if (!TemporalFreezeManager.tryFreeze(serverLevel.getServer(), TemporalFreezeRules.FREEZE_TICKS)) {
            player.sendOverlayMessage(Component.translatable("message.avoidminer.temporal_clock.already_frozen"));
            return InteractionResult.FAIL;
        }

        serverLevel.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT,
                SoundSource.PLAYERS, 2.0F, 0.7F);
        serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, true, true,
                player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                100, 0.6, 0.9, 0.6, 0.18);
        player.getCooldowns().addCooldown(stack, TemporalFreezeRules.COOLDOWN_TICKS);
        stack.hurtAndBreak(1, player, hand);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.temporal_clock.effect").withStyle(ChatFormatting.AQUA));
        builder.accept(Component.translatable("tooltip.avoidminer.temporal_clock.balance").withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.avoidminer.temporal_clock.heart").withStyle(ChatFormatting.DARK_AQUA));
    }
}
