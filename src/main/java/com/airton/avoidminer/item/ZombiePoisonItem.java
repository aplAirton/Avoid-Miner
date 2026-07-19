package com.airton.avoidminer.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
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
            var params = ConversionParams.single(villager, true, true);
            var zombieVillager = villager.convertTo(EntityType.ZOMBIE_VILLAGER, params, zv -> {
                var difficulty = serverLevel.getCurrentDifficultyAt(zv.blockPosition());
                zv.finalizeSpawn(serverLevel, difficulty, EntitySpawnReason.CONVERSION,
                        new Zombie.ZombieGroupData(false, true));
                zv.setVillagerData(villager.getVillagerData());
                zv.setGossips(villager.getGossips().copy());
                zv.setTradeOffers(villager.getOffers().copy());
                zv.setVillagerXp(villager.getVillagerXp());
            });
            if (zombieVillager != null) {
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
