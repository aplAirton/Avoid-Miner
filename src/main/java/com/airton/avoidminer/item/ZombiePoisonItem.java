package com.airton.avoidminer.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class ZombiePoisonItem extends Item {
    public ZombiePoisonItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof Villager villager && !player.level().isClientSide()
                && villager.isAlive() && !villager.isBaby()) {
            var serverLevel = (ServerLevel) player.level();
            var zombieVillager = EntityType.ZOMBIE_VILLAGER.create(serverLevel, EntitySpawnReason.CONVERSION);
            if (zombieVillager != null) {
                zombieVillager.copyPosition(villager);
                zombieVillager.setBaby(villager.isBaby());
                zombieVillager.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
                serverLevel.addFreshEntity(zombieVillager);
                villager.discard();
                stack.shrink(1);
                serverLevel.playSound(null, villager.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CURE,
                        SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.zombie_poison.desc")
                .withStyle(ChatFormatting.DARK_GREEN));
    }
}
