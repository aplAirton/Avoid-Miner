package com.airton.avoidminer.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class MobGrinderRangeCardItem extends Item {
    private final int range;

    public MobGrinderRangeCardItem(int range, Properties properties) {
        super(properties.stacksTo(1));
        this.range = range;
    }

    public int getRange() { return range; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        int size = range * 2 + 1;
        builder.accept(Component.translatable("tooltip.avoidminer.grinder_range_card.info", size, size)
                .withStyle(ChatFormatting.GOLD));
    }
}
