package com.airton.avoidminer.item;

import com.airton.avoidminer.SeismicBootsRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.function.Consumer;

public class SeismicPropulsionBootsItem extends Item {
    public SeismicPropulsionBootsItem(Properties properties) {
        super(properties.humanoidArmor(ArmorMaterials.NETHERITE, ArmorType.BOOTS)
                .fireResistant()
                .rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.seismic_boots.arm")
                .withStyle(ChatFormatting.DARK_AQUA));
        builder.accept(Component.translatable("tooltip.avoidminer.seismic_boots.impact",
                        (int) SeismicBootsRules.MIN_RADIUS, (int) SeismicBootsRules.MAX_RADIUS)
                .withStyle(ChatFormatting.GRAY));
    }
}
