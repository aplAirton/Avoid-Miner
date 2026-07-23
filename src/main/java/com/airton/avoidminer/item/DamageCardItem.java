package com.airton.avoidminer.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class DamageCardItem extends Item {
    public enum Tier {
        TIER_1(10, 20),
        TIER_2(12, 12),
        TIER_3(16, 10);

        public final int damage;
        public final int tickInterval;

        Tier(int damage, int tickInterval) {
            this.damage = damage;
            this.tickInterval = tickInterval;
        }
    }

    private final Tier tier;

    public DamageCardItem(Tier tier, Properties properties) {
        super(properties.stacksTo(1));
        this.tier = tier;
    }

    public Tier getTier() { return tier; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.damage_card.info",
                tier.damage).withStyle(ChatFormatting.RED));
    }
}
