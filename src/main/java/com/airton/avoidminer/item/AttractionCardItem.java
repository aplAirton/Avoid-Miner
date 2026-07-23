package com.airton.avoidminer.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class AttractionCardItem extends Item {
    public enum Tier {
        TIER_1(50),
        TIER_2(80),
        TIER_3(100);

        public final int radius;

        Tier(int radius) { this.radius = radius; }
    }

    private final Tier tier;

    public AttractionCardItem(Tier tier, Properties properties) {
        super(properties.stacksTo(1));
        this.tier = tier;
    }

    public Tier getTier() { return tier; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.attraction_card.info", tier.radius)
                .withStyle(ChatFormatting.AQUA));
    }
}
