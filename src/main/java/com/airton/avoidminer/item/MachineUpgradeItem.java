package com.airton.avoidminer.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;
import java.util.function.Consumer;

/**
 * Item de melhoria das máquinas do mod. Além dos tooltips descritivos,
 * suporta aplicação direta: agachado + clique direito numa máquina insere a
 * melhoria no slot dedicado sem abrir a GUI (ver {@link UpgradeApplier}).
 */
public class MachineUpgradeItem extends Item {
    private final List<String> tooltipKeys;

    public MachineUpgradeItem(Properties properties, String... tooltipKeys) {
        super(properties.stacksTo(1));
        this.tooltipKeys = List.of(tooltipKeys);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        for (String key : tooltipKeys) {
            builder.accept(Component.translatable(key).withStyle(ChatFormatting.GRAY));
        }
        builder.accept(Component.translatable("tooltip.avoidminer.upgrade.apply_hint").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return UpgradeApplier.tryApply(context);
    }
}
