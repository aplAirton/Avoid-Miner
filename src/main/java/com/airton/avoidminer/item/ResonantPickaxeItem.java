package com.airton.avoidminer.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public final class ResonantPickaxeItem extends Item {
    public ResonantPickaxeItem(Properties properties) {
        super(properties
                .pickaxe(ToolMaterial.NETHERITE, 1.0F, -2.8F)
                .durability(ResonantMiningRules.RESONANT_PICKAXE_DURABILITY)
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.resonant_pickaxe.power",
                        ResonantMiningRules.NATIVE_FORWARD_RANGE,
                        ResonantMiningRules.SIDE_RADIUS * 2 + 1,
                        ResonantMiningRules.SIDE_RADIUS * 2 + 1)
                .withStyle(ChatFormatting.DARK_AQUA));
        builder.accept(Component.translatable("tooltip.avoidminer.resonant_pickaxe.use")
                .withStyle(ChatFormatting.GRAY));
    }
}
