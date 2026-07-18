package com.airton.avoidminer.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.function.Consumer;

public class ScrollOfForgetfulnessItem extends Item {
    public ScrollOfForgetfulnessItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof Villager villager && !player.level().isClientSide()) {
            VillagerData data = villager.getVillagerData();
            villager.setVillagerData(data.withProfession(player.level().registryAccess(), VillagerProfession.NONE).withLevel(1));
            villager.overrideOffers(new MerchantOffers());
            stack.shrink(1);
            villager.level().playSound(null, villager.blockPosition(), SoundEvents.VILLAGER_HURT,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.scroll_of_forgetfulness.desc")
                .withStyle(ChatFormatting.RED));
    }
}
