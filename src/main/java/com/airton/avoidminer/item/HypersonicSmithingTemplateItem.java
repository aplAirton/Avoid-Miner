package com.airton.avoidminer.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;

public class HypersonicSmithingTemplateItem extends SmithingTemplateItem {
    public HypersonicSmithingTemplateItem(Item.Properties properties) {
        super(
                Component.translatable("item.avoidminer.hypersonic_smithing_template.applies_to")
                        .withStyle(ChatFormatting.BLUE),
                Component.translatable("item.avoidminer.hypersonic_smithing_template.ingredients")
                        .withStyle(ChatFormatting.BLUE),
                Component.translatable("item.avoidminer.hypersonic_smithing_template.base_slot_description"),
                Component.translatable("item.avoidminer.hypersonic_smithing_template.additions_slot_description"),
                List.of(
                        Identifier.withDefaultNamespace("container/slot/sword"),
                        Identifier.withDefaultNamespace("container/slot/boots")
                ),
                List.of(Identifier.withDefaultNamespace("container/slot/ingot")),
                properties.rarity(Rarity.RARE)
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        builder.accept(Component.translatable("tooltip.avoidminer.hypersonic_smithing_template.source")
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}
