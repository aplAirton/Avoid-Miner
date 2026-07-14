package com.airton.avoidminer.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class TemporalCoreItem extends Item {
    public TemporalCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.temporal_core.desc")
                .withStyle(ChatFormatting.DARK_AQUA));
        builder.accept(Component.translatable("tooltip.avoidminer.temporal_core.recipe")
                .withStyle(ChatFormatting.GRAY));
    }
}
